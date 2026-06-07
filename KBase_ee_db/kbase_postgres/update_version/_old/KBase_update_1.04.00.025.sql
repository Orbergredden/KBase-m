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
	v_version_old  varchar(50) := '1.03.01.025';
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
   set value = '1.04.00.025', 
       descr = 'new version support of template',
       date_modified = now(),
       user_modified = "current_user"()
 where alias = 'VERSION_DB_NUMBER'
;
update settings 
   set value = '24.08.2023', 
       descr = '',
       date_modified = now(),
       user_modified = "current_user"()
 where alias = 'VERSION_DB_END_DATE'
;
--######## move table settings #######################################

CREATE SEQUENCE kbase.seq_settings
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE kbase.seq_settings OWNER TO kbase;

do $$ 
<<seq_settings>>
declare
-- устанавливаем значение сиквенса
	v_maxId   bigint;
	v_result  bigint;
begin
	select max(id)
	  into v_maxId
	  from public.settings
	;
	
	v_maxId := v_maxId + 1;
	SELECT pg_catalog.setval('kbase.seq_settings', v_maxId, true) into v_result;
	raise notice 'v_result =  %', v_result;
end seq_settings $$;	

------------------------------------------------
CREATE TABLE kbase.settings
(
    id bigint NOT NULL DEFAULT nextval('kbase.seq_settings'::regclass),
    alias character varying(50) COLLATE pg_catalog."default" NOT NULL,
    section character varying(50) COLLATE pg_catalog."default",
    subject character varying(50) COLLATE pg_catalog."default",
    name character varying(50) COLLATE pg_catalog."default",
    value character varying(50) COLLATE pg_catalog."default",
    descr character varying(200) COLLATE pg_catalog."default",
    date_created timestamp without time zone DEFAULT now(),
    date_modified timestamp without time zone DEFAULT now(),
    user_created character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    user_modified character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    CONSTRAINT pk_settings_id PRIMARY KEY (id),
    CONSTRAINT k_settings_alias UNIQUE (alias)
) 
TABLESPACE pg_default;

ALTER TABLE kbase.settings OWNER to kbase;
GRANT ALL ON TABLE kbase.settings TO kbase;
GRANT SELECT ON TABLE kbase.settings TO kbase_user;

COMMENT ON TABLE kbase.settings IS 'Для хранения настроек программы на уровне БД.';
COMMENT ON COLUMN kbase.settings.alias IS 'Текстовый уникальный идентификатор';
COMMENT ON COLUMN kbase.settings.user_created IS 'Тот, кто создал запись';
COMMENT ON COLUMN kbase.settings.user_modified IS 'Тот, кто вносил последние изменения в запись';

CREATE UNIQUE INDEX ind_settings_alias
    ON kbase.settings USING btree
    (alias COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;	

---------------------------------------
-- копируем информацию
insert into kbase.settings
select * from public.settings
;
------------------------------ 
-- удаляем старые обьекты
DROP TABLE public.settings;
DROP SEQUENCE public.seq_settings;

--######## insert table settings #######################################
insert into kbase.settings (alias, section, subject, name, value, descr)
values ('SECTION_TEMPLATE_MAIN_DEFAULT', 'Section', 'Template', 'Main template default', 'KBASE_MAIN_HEADER_EXT',
        'Стиль головного шаблона за замовчуванням. Коли в ієрархії розділів не вказано.')
;

--######### DROP old SEQUENCE public.templates #############################
DROP SEQUENCE public.seq_templates;

--######## table sections ##################################################
CREATE SEQUENCE kbase.seq_sections
    INCREMENT 1
    START 2
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE kbase.seq_sections OWNER TO kbase;

do $$ 
<<seq_sections>>
declare
-- устанавливаем значение сиквенса
	v_maxId   bigint;
	v_result  bigint;
begin
	select max(id)
	  into v_maxId
	  from public.sections
	;
	
	v_maxId := v_maxId + 1;
	SELECT pg_catalog.setval('kbase.seq_sections', v_maxId, true) into v_result;
	raise notice 'v_result =  %', v_result;
end seq_sections $$;

--------------------------------------------------------
CREATE TABLE kbase.sections
(
    id bigint NOT NULL DEFAULT nextval('seq_sections'::regclass),
    parent_id bigint,
    name character varying(255) COLLATE pg_catalog."default",
	descr character varying(255) COLLATE pg_catalog."default",
	icon_id bigint,
	template_main character varying(50) COLLATE pg_catalog."default",
	template_main_tree integer,
	template_main_root bigint,
    icon_id_root bigint,
    icon_id_def bigint,
    theme_id bigint,
    cache_type integer,
    date_created timestamp without time zone DEFAULT now(),
    date_modified timestamp without time zone DEFAULT now(),
    user_created character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    user_modified character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    date_modified_info timestamp without time zone,
    CONSTRAINT pk_sections_id PRIMARY KEY (id),
    CONSTRAINT fk_sections_icon_id FOREIGN KEY (icon_id)
        REFERENCES public.icons (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)
TABLESPACE pg_default;

ALTER TABLE kbase.sections OWNER to kbase;
GRANT ALL ON TABLE kbase.sections TO kbase;
GRANT SELECT ON TABLE kbase.sections TO kbase_user;
GRANT SELECT ON TABLE kbase.sections TO kbase_view;

COMMENT ON TABLE kbase.sections IS 'Разделы Базы Знаний в виде дерева';
COMMENT ON COLUMN kbase.sections.icon_id IS 'Пиктограмма раздела';
COMMENT ON COLUMN kbase.sections.template_main IS 'Тег стилю головного шаблона';
COMMENT ON COLUMN kbase.sections.template_main_tree IS '0 - стиль діє лише на ций розділ, 1 - розповсюджується також на всі підрозділи';
COMMENT ON COLUMN kbase.sections.template_main_root IS 'Коренева директорія головних стилів';
COMMENT ON COLUMN kbase.sections.user_created IS 'Тот, кто создал запись';
COMMENT ON COLUMN kbase.sections.user_modified IS 'Тот, кто вносил последние изменения в запись';
COMMENT ON COLUMN kbase.sections.date_modified_info IS 'Последнее изменение инфоблоков';
COMMENT ON COLUMN kbase.sections.icon_id_root IS 'Корневая иконка поддерева для выбора иконок для данного и дочерних разделов';
COMMENT ON COLUMN kbase.sections.icon_id_def IS 'Иконка по умолчанию для данного и дочерних разделов';
COMMENT ON COLUMN kbase.sections.theme_id IS 'Тема шаблонов для показа документа.';
COMMENT ON COLUMN kbase.sections.cache_type
    IS 'Тип кеширования : 1 - документы кешируются на локальном диске; 2 - кешируются в БД; 3 - кешируются на диске только обязательные файлы';

CREATE INDEX ind_sections_parent_id
    ON kbase.sections USING btree
    (parent_id ASC NULLS LAST)
    TABLESPACE pg_default;

----------------------------------------------------------
insert into kbase.sections (id,parent_id,name,descr,icon_id,icon_id_root,icon_id_def,theme_id,cache_type,
                            date_created,date_modified,user_created,user_modified,date_modified_info)
select id,parent_id,name,descr,icon_id,icon_id_root,icon_id_def,theme_id,cache_type,
       date_created,date_modified,user_created,user_modified,date_modified_info
  from public.sections
;

---------------------------------------------------------
ALTER TABLE public.documents DROP CONSTRAINT fk_documents_sectionid;

ALTER TABLE public.documents
    ADD CONSTRAINT fk_documents_sectionid FOREIGN KEY (section_id)
    REFERENCES kbase.sections (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION;

ALTER TABLE kbase.info DROP CONSTRAINT fk_info_sectionid;

ALTER TABLE IF EXISTS kbase.info
    ADD CONSTRAINT fk_info_sectionid FOREIGN KEY (sectionid)
    REFERENCES kbase.sections (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION;

------------------------------ 
-- вилучаємо старі об'єкти
DROP TABLE public.sections;
DROP SEQUENCE public.seq_sections;

--######## move function section_geticoniddefault() #################################
DROP FUNCTION public.section_geticoniddefault(bigint);

CREATE OR REPLACE FUNCTION kbase.section_geticoniddefault(
	p_sectionid bigint)
    RETURNS bigint
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$

DECLARE
	v_sectionRec sections%rowtype;
	v_iconIdDef  bigint := 0;
BEGIN
	-- выбираем запись с разделом
	select s.*
	  into v_sectionRec
	  from sections s
	 where id = p_sectionId
	;
	-- проходим вверх по дереву к корню
	while (coalesce(v_sectionRec.icon_id_def,0) = 0) and (coalesce(v_sectionRec.parent_id,0) > 0) loop
		select s.*
		  into v_sectionRec
		  from sections s
		 where id = v_sectionRec.parent_id
		;
	end loop;

	-- если дефолтной иконки нет в разделах, то берем по умолчанию
	if coalesce(v_sectionRec.icon_id_def,0) = 0 then
		select cast(s.value as bigint)
		  into v_iconIdDef
		  from settings s
		 where s.alias = 'SECTION_ICON_DEFAULT'
		;
	else
		v_iconIdDef := v_sectionRec.icon_id_def;
	end if;

	return v_iconIdDef;
END;
$BODY$;

ALTER FUNCTION kbase.section_geticoniddefault(bigint) OWNER TO kbase;

--######## move function section_getthemeid() ###########################
DROP FUNCTION public.section_getthemeid(bigint);

CREATE OR REPLACE FUNCTION kbase.section_getthemeid(
	v_sectionid bigint)
    RETURNS bigint
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$

DECLARE
	v_sectionRec sections%rowtype;
	v_themeId    bigint := 0;
BEGIN
	-- выбираем запись с разделом 
	select s.*
	  into v_sectionRec
	  from sections s
	 where id = v_sectionId
	;
	-- проходим вверх по дереву к корню
	while (coalesce(v_sectionRec.theme_id,0) = 0) and (coalesce(v_sectionRec.parent_id,0) > 0) loop
		select s.*
		  into v_sectionRec
		  from sections s
		 where id = v_sectionRec.parent_id
		;
	end loop;

	-- если темы нет в разделах, то берем по умолчанию
	if coalesce(v_sectionRec.theme_id,0) = 0 then
		select cast(s.value as bigint)
		  into v_themeId
		  from settings s
		 where s.alias = 'SECTION_THEME_DEFAULT'
		;
	else
		v_themeId := v_sectionRec.theme_id;
	end if;

	return v_themeId;
END;
$BODY$;

ALTER FUNCTION kbase.section_getthemeid(bigint) OWNER TO kbase;

--######## move info_delete1()############################################
DROP FUNCTION public.info_delete1(bigint);

CREATE OR REPLACE FUNCTION kbase.info_delete1(
	p_id bigint)
    RETURNS integer
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
-- удаление одного инфоблока
DECLARE
	v_infoTypeId   bigint;
	v_infoId       bigint;
BEGIN
	-- delete info block
    select infoTypeId, infoId
      into v_infoTypeId, v_infoId
      from info
     where id = p_id
    ;
    case v_infoTypeId
        when 1 then             -- Простой текст
			delete from info_text where id = v_infoId;
		when 2 then             -- Изображение	
			delete from info_image where id = v_infoId;
		when 3 then             -- Файл	
			delete from info_file where id = v_infoId;
		else 
             raise exception 'info_delete1 : Not existing type of info block , infoTypeId = %', v_infoTypeId;
	end case;

	-- delete info header
	delete from info where id = p_id;

    return 0;
END;
$BODY$;

ALTER FUNCTION kbase.info_delete1(bigint) OWNER TO kbase;

COMMENT ON FUNCTION kbase.info_delete1(bigint) IS 'удаление одного инфоблока';
--######## move info_delete() ############################################
DROP FUNCTION public.info_delete(bigint);

CREATE OR REPLACE FUNCTION kbase.info_delete(
	p_sectionid bigint)
    RETURNS integer
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
-- удаление всех инфо блоков указанного раздела
DECLARE
	v_retVal  integer := 0;
	v_rec     record;
BEGIN
	-- delete info blocks
	for v_rec in (select id, infoTypeId, infoId
                    from info
                   where sectionId = p_sectionId)
	loop
		case v_rec.infoTypeId
        	when 1 then             -- Простой текст
				delete from info_text where id = v_rec.infoId;
			when 2 then             -- Изображение	
				delete from info_image where id = v_rec.infoId;
			when 3 then             -- Файл	
				delete from info_file where id = v_rec.infoId;
			else 
				raise exception 'info_delete : Not existing type of info block , infoTypeId = %', v_rec.infoTypeId;
		end case;
    end loop;

	-- delete info headers
	delete from info where sectionId = p_sectionId;

	return v_retVal;
END;
$BODY$;

ALTER FUNCTION kbase.info_delete(bigint) OWNER TO kbase;

COMMENT ON FUNCTION kbase.info_delete(bigint)
    IS 'удаление всех инфо блоков указанного раздела';
--######### move infopositioncheckandrenumber () ###########################
DROP FUNCTION public.infopositioncheckandrenumber(bigint, bigint);

CREATE OR REPLACE FUNCTION kbase.infopositioncheckandrenumber(
	p_sectionid bigint,
	p_newposition bigint)
    RETURNS bigint
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$

DECLARE
	v_CounterPos bigint := 10;

	v_countR INTEGER;
	v_retVal bigint;
	v_rec    RECORD;
BEGIN
	v_retVal := p_newPosition;

	-- проверяем, есть ли такая позиция уже в БД
	select count(*)
      into v_countR
      from info
     where sectionId = p_sectionId
       and position = p_newPosition
     ;
    -- если есть , делаем перенумерацию
    if v_countR > 0 then
		for v_rec in (select id
                       from info
                      where sectionId = p_sectionId
                        and position < p_newPosition
                      order by position)
        loop
			update info
               set position = v_CounterPos
             where id = v_rec.id
			;
			v_CounterPos := v_CounterPos + 10;
        end loop;

		v_retVal := v_CounterPos;
        v_CounterPos := v_CounterPos + 10;

		for v_rec in (select id
                       from info
                      where sectionId = p_sectionId
                        and position >= p_newPosition
                      order by position)
        loop
			update info
               set position = v_CounterPos
             where id = v_rec.id
			;
			v_CounterPos := v_CounterPos + 10;
        end loop;
	end if;

	return v_retVal;
END;
$BODY$;

ALTER FUNCTION kbase.infopositioncheckandrenumber(bigint, bigint) OWNER TO kbase;
--########## move section_delete() #######################################
DROP FUNCTION public.section_delete(bigint);

CREATE OR REPLACE FUNCTION kbase.section_delete(
	p_sectionid bigint)
    RETURNS integer
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$

DECLARE
	v_rec              record;
    v_infoDeleteResult integer;
    v_retVal           integer := 0;
BEGIN
	-- delete documents and info blocks
	for v_rec in execute 'WITH RECURSIVE x(id) AS (
	                 SELECT id
                       FROM sections
                      WHERE id = $1
                     UNION  ALL
                     SELECT a.id
                       FROM x
                       JOIN sections a ON a.parent_id = x.id)
                  select s.id
                    from sections s
                    join x on x.id = s.id' using p_sectionId
    loop
		delete from documents where section_id = v_rec.id;
		select info_delete(v_rec.id) into v_infoDeleteResult;
    end loop;

	-- delete sections
    WITH RECURSIVE x AS (
		SELECT id
	      FROM sections
	     WHERE id = p_sectionId
	    UNION  ALL
	    SELECT a.id
	      FROM x
	      JOIN sections a ON a.parent_id = x.id
	)
	DELETE FROM sections a
	 USING  x
	 WHERE a.id = x.id
	;

	return v_retVal;
END;
$BODY$;

ALTER FUNCTION kbase.section_delete(bigint) OWNER TO kbase;

--######## create function section_get_stylemain_tag ########################################
CREATE OR REPLACE FUNCTION kbase.section_get_stylemain_tag (v_sectionid bigint)
    RETURNS character varying
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
-- Повертає тег головного стиля для вказаного розділа
-- шукає з проходом до кореня (и по замовчанню з конфігу, якщо нічого не вказано)
DECLARE
	v_sectionRec sections%rowtype;
	v_tag        template_style.tag%type;
BEGIN
	-- выбираем запись с разделом 
	select s.*
	  into v_sectionRec
	  from sections s
	 where id = v_sectionId
	;
	-- якщо є тег , то одразу його забираємо
	if coalesce(v_sectionRec.template_main,'') <> '' then
		return v_sectionRec.template_main;
	end if;

	-- проходим вверх по дереву к корню
	while ((coalesce(v_sectionRec.template_main,'') = '') or 
	       ((coalesce(v_sectionRec.template_main,'') <> '') and 
	        (coalesce(v_sectionRec.template_main_tree,0) = 0))) and 
	      (coalesce(v_sectionRec.parent_id,0) > 0) loop
		select s.*
		  into v_sectionRec
		  from sections s
		 where id = v_sectionRec.parent_id
		;
	end loop;

	-- если тега нет в разделах, то берем по умолчанию
	if (coalesce(v_sectionRec.template_main,'') = '') or 
	   ((coalesce(v_sectionRec.template_main,'') <> '') and 
	    (coalesce(v_sectionRec.template_main_tree,0) = 0)) then
		select s.value
		  into v_tag
		  from settings s
		 where s.alias = 'SECTION_TEMPLATE_MAIN_DEFAULT'
		;
	else
		v_tag := v_sectionRec.template_main;
	end if;

	return v_tag;
END;
$BODY$;

ALTER FUNCTION kbase.section_get_stylemain_tag(bigint) OWNER TO postgres;
COMMENT ON FUNCTION kbase.section_get_stylemain_tag(bigint)
    IS 'Повертає тег головного стиля для вказаного розділа. Шукає з проходом до кореня (и по замовчанню з конфігу, якщо нічого не вказано)';
--<<