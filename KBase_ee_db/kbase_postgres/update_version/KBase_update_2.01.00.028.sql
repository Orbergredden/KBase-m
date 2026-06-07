--
-- PostgeSQL KBase update
--

--\connect kbase_test

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
--SET client_encoding = 'WIN1251';
SET lc_messages TO 'en_US.UTF-8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = kbase, public, pg_catalog;

-- ######## перевіряємо щоб БД була попередньої версії #############################
do $$ 
<<check_version>>
declare
	v_version_old  varchar(50) := '2.00.00.027';
	v_version      varchar(50);
begin
	select s.value 
	  into v_version
	  from settings s 
	 where s.alias = 'VERSION_DB_NUMBER'
	;
	if v_version_old <> v_version then
		--raise notice 'DB version too old, (% <> %)', v_version, v_version_old;
		RAISE EXCEPTION 'DB version too old, (% <> %)', v_version, v_version_old;
	end if;
end check_version $$;

-- ######## update table Settings for new version ##################################
update settings 
   set value = '2.01.00.028', 
       descr = 'renew object Dictionaries',
       date_modified = now(),
       user_modified = "current_user"()
 where alias = 'VERSION_DB_NUMBER'
;
update settings 
   set value = '08.04.2024', 
       descr = '',
       date_modified = now(),
       user_modified = "current_user"()
 where alias = 'VERSION_DB_END_DATE'
;
--######## drop old tables #######################################
drop function kbase.dictionary_get_item_for_learn;
drop table kbase.dict_current;
drop table kbase.dict_texts;
drop table kbase.dict_phrases;
drop table kbase.dict_words;
drop table kbase.dict_theme;
drop table kbase.dict_source;
drop table kbase.dict_categories;
DROP SEQUENCE kbase.seq_dict_current;
DROP SEQUENCE kbase.seq_dict_texts;
DROP SEQUENCE kbase.seq_dict_phrases;
DROP SEQUENCE kbase.seq_dict_words;
DROP SEQUENCE kbase.seq_dict_theme;
DROP SEQUENCE kbase.seq_dict_source;
DROP SEQUENCE kbase.seq_dict_categories;

--######## add field Type to Section #############################
alter table kbase.sections add type_id bigint not null default 1;
COMMENT ON COLUMN kbase.sections.type_id IS 'тип інформації розділу : 1 - документ, 2 - словник';

--######## create table dict_texts #######################################
CREATE SEQUENCE kbase.seq_dict
    INCREMENT 1
    START 2
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE kbase.seq_dict OWNER TO kbase;
--------------------------------------------------------
CREATE TABLE kbase.dict
(
    id bigint NOT NULL DEFAULT nextval('seq_dict'::regclass),
	section_id bigint NOT NULL,
	type_id bigint NOT NULL,
	name text,
	value text,
    descr text,
	rating integer,
	reverse integer,
	status integer,
	date_viewed timestamp without time zone DEFAULT now(),
    date_created timestamp without time zone DEFAULT now(),
    date_modified timestamp without time zone DEFAULT now(),
    user_created character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    user_modified character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    CONSTRAINT pk_dict_id PRIMARY KEY (id),
    CONSTRAINT fk_dict_section_id FOREIGN KEY (section_id)
        REFERENCES kbase.sections (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)
TABLESPACE pg_default;

ALTER TABLE kbase.dict OWNER to kbase;
GRANT ALL ON TABLE kbase.dict TO kbase;
GRANT SELECT ON TABLE kbase.dict TO kbase_user;
GRANT SELECT ON TABLE kbase.dict TO kbase_view;

COMMENT ON TABLE kbase.dict IS 'Словники';
COMMENT ON COLUMN kbase.dict.type_id IS 'тип інформації : 1 - word, 2 - phrase, 3 - text';
COMMENT ON COLUMN kbase.dict.rating IS 'це типу як часто його показувати при навчанні';
COMMENT ON COLUMN kbase.dict.reverse IS '1 - показувати при навчанні в зворотньому напрямку';
COMMENT ON COLUMN kbase.dict.status IS '0 - не включаємо в навчання, 1 - включаємо';
COMMENT ON COLUMN kbase.dict.date_viewed IS 'дата останього показу';
COMMENT ON COLUMN kbase.dict.user_created IS 'Той, хто створив запис';
COMMENT ON COLUMN kbase.dict.user_modified IS 'Той, хто змінював запис в останнє';

--######## update manual_get_documents_from_section() ##################################
DROP FUNCTION kbase.manual_get_documents_from_section(int8);

CREATE OR REPLACE FUNCTION kbase.manual_get_documents_from_section(
	p_sectionid bigint)
    RETURNS TABLE(sectionid bigint, 
	              parent_id bigint, 
				  type_id bigint,
				  name character varying, sectionpathname character varying, icon_id bigint, descr character varying, date_created timestamp without time zone, date_modified timestamp without time zone, user_created character varying, user_modified character varying, date_modified_info timestamp without time zone, icon_id_root bigint, icon_id_def bigint, theme_id bigint, cache_type integer) 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE SECURITY DEFINER PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
  -- return list of sections from section with p_sectionid
DECLARE
	v_sql_str character varying := 'select s.id
                        ,s.parent_id
						,s.type_id
                        ,s.name
                        ,section_get_pathname(s.id,'' | '') as sectionPathName
                        ,s.icon_id
                        ,s.descr
                        ,s.date_created
                        ,s.date_modified
                        ,s.user_created
                        ,s.user_modified
                        ,s.date_modified_info
                        ,s.icon_id_root
                        ,s.icon_id_def
                        ,s.theme_id
                        ,s.cache_type 
                    from sections s
                    join section_get_list_id_subsections($1) l  on l.id = s.id 
	             ';
	v_rec              record;
BEGIN
	for v_rec in execute v_sql_str using p_sectionId
    loop
		sectionid          := v_rec.id;
	    parent_id          := v_rec.parent_id;
		type_id            := v_rec.type_id;
		name               := v_rec.name;
		sectionPathName    := v_rec.sectionPathName;
		icon_id            := v_rec.icon_id;
		descr              := v_rec.descr;
		date_created       := v_rec.date_created;
        date_modified      := v_rec.date_modified;
        user_created       := v_rec.user_created;
        user_modified      := v_rec.user_modified;
		date_modified_info := v_rec.date_modified_info;
        icon_id_root       := v_rec.icon_id_root;
        icon_id_def        := v_rec.icon_id_def;
        theme_id           := v_rec.theme_id;
        cache_type         := v_rec.cache_type;
	
		RETURN NEXT;
	end loop;
END; 
$BODY$;

ALTER FUNCTION kbase.manual_get_documents_from_section(bigint)
    OWNER TO kbase;

COMMENT ON FUNCTION kbase.manual_get_documents_from_section(bigint)
    IS 'return list of sections from section with p_sectionid';

        

--<<