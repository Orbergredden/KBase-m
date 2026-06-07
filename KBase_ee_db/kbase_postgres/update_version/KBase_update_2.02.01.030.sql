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
	v_version_old  varchar(50) := '2.02.00.029';
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
   set value = '2.02.01.030', 
       descr = 'remove icons.file_name',
       date_modified = now(),
       user_modified = "current_user"()
 where alias = 'VERSION_DB_NUMBER'
;
update settings 
   set value = '06.11.2024', 
       descr = '',
       date_modified = now(),
       user_modified = "current_user"()
 where alias = 'VERSION_DB_END_DATE'
;
--######## move table icons #######################################
CREATE SEQUENCE IF NOT EXISTS kbase.seq_icons
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE kbase.seq_icons OWNER TO kbase;

do $$ 
<<seq_icons>>
declare
-- встановлюєм значення сіквенсу
	v_maxId   bigint;
	v_result  bigint;
begin
	select max(id)
	  into v_maxId
	  from public.icons
	;
	
	v_maxId := v_maxId + 1;
	SELECT pg_catalog.setval('kbase.seq_icons', v_maxId, true) into v_result;
	raise notice 'v_result =  %', v_result;
end seq_icons $$;
------------------------------------------------------
CREATE TABLE kbase.icons
(
    id bigint NOT NULL DEFAULT nextval('seq_icons'::regclass),
    parent_id bigint NOT NULL,
    name character varying(50) COLLATE pg_catalog."default" NOT NULL,
    descr character varying(50) COLLATE pg_catalog."default",
    image bytea,
    date_created timestamp without time zone DEFAULT now(),
    date_modified timestamp without time zone DEFAULT now(),
    user_created character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    user_modified character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    CONSTRAINT pk_icons_id PRIMARY KEY (id)
)
TABLESPACE pg_default;

ALTER TABLE kbase.icons OWNER to kbase;
GRANT ALL ON TABLE kbase.icons TO kbase;
GRANT SELECT ON TABLE kbase.icons TO kbase_user;
GRANT SELECT ON TABLE kbase.icons TO kbase_view;

CREATE INDEX ind_icons_parent_id
    ON kbase.icons USING btree
    (parent_id ASC NULLS LAST)
    TABLESPACE pg_default;
----------------------------------------------------------
insert into kbase.icons (id,parent_id,name,descr,image,date_created,date_modified,user_created,user_modified)
select id,parent_id,name,descr,image,date_created,date_modified,user_created,user_modified
  from public.icons
;
-----------------------------------------------------
ALTER TABLE public.current_icon DROP CONSTRAINT fk_current_icon_icon_id;
ALTER TABLE public.current_icon
    ADD CONSTRAINT fk_current_icon_icon_id FOREIGN KEY (icon_id)
    REFERENCES kbase.icons (id) MATCH SIMPLE
    ON UPDATE CASCADE
    ON DELETE CASCADE;

ALTER TABLE kbase.sections DROP CONSTRAINT fk_sections_icon_id;
ALTER TABLE kbase.sections
    ADD CONSTRAINT fk_sections_icon_id FOREIGN KEY (icon_id)
    REFERENCES kbase.icons (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION;
------------------------------ 
-- вилучаємо старі об'єкти
DROP TABLE public.icons;
DROP SEQUENCE public.seq_icons;

--######## move table current_icon #######################################
CREATE SEQUENCE kbase.seq_current_icon
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;
ALTER SEQUENCE kbase.seq_current_icon OWNER TO kbase;
GRANT ALL ON SEQUENCE kbase.seq_current_icon TO kbase;
GRANT ALL ON SEQUENCE kbase.seq_current_icon TO kbase_user;

do $$ 
<<seq_current_icon>>
declare
-- встановлюєм значення сіквенсу
	v_maxId   bigint;
	v_result  bigint;
begin
	select max(id)
	  into v_maxId
	  from public.current_icon
	;
	
	v_maxId := v_maxId + 1;
	SELECT pg_catalog.setval('kbase.seq_current_icon', v_maxId, true) into v_result;
	raise notice 'v_result =  %', v_result;
end seq_current_icon $$;
------------------------------------------------------
CREATE TABLE kbase.current_icon
(
    id bigint NOT NULL DEFAULT nextval('seq_current_icon'::regclass),
    "user" character varying(30) COLLATE pg_catalog."default" NOT NULL DEFAULT "current_user"(),
    icon_id bigint NOT NULL,
    date_created timestamp without time zone DEFAULT now(),
    date_modified timestamp without time zone DEFAULT now(),
    CONSTRAINT pk_current_icon_id PRIMARY KEY (id),
    CONSTRAINT fk_current_icon_icon_id FOREIGN KEY (icon_id)
        REFERENCES kbase.icons (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
)
TABLESPACE pg_default;

ALTER TABLE IF EXISTS kbase.current_icon OWNER to kbase;
GRANT ALL ON TABLE kbase.current_icon TO kbase;
REVOKE ALL ON TABLE kbase.current_icon FROM kbase_user;
GRANT DELETE, UPDATE, INSERT, SELECT ON TABLE kbase.current_icon TO kbase_user;

CREATE INDEX ind_current_icon_icon_id
    ON kbase.current_icon USING btree
    (icon_id ASC NULLS LAST)
    TABLESPACE pg_default;
----------------------------------------------------------
insert into kbase.current_icon (id,"user",icon_id,date_created,date_modified)
select id,"user",icon_id,date_created,date_modified
  from public.current_icon
;
------------------------------ 
-- вилучаємо старі об'єкти
DROP TABLE public.current_icon;
DROP SEQUENCE public.seq_current_icon;

--######## create index ##################################################
CREATE INDEX ind_sections_icon_id ON kbase.sections USING btree (icon_id);

--######## update infotype ###############################################
update infotype 
   set "name" = 'не визначений',
       descr  = 'доданий, щоб не спрацьовували констрейнти'
 where id = 0
;
update infotype 
   set "name" = 'Простий текст' 
 where id = 1
;
update infotype 
   set "name" = 'Зображення',
       descr = 'для не дуже важких картинок'
 where id = 2
;
update infotype 
   set "name" = 'Файл',
       descr  = 'для не дуже важких файлів'
 where id = 3
;

--######## table documents ##################################################
CREATE SEQUENCE kbase.seq_documents
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE kbase.seq_documents OWNER TO kbase;

do $$ 
<<seq_documents>>
declare
-- устанавливаем значение сиквенса
	v_maxId   bigint;
	v_result  bigint;
begin
	select max(id)
	  into v_maxId
	  from public.documents
	;
	
	v_maxId := v_maxId + 1;
	SELECT pg_catalog.setval('kbase.seq_documents', v_maxId, true) into v_result;
	raise notice 'v_result =  %', v_result;
end seq_documents $$;

--------------------------------------------------------
CREATE TABLE kbase.documents
(
    id bigint NOT NULL DEFAULT nextval('seq_documents'::regclass),
    section_id bigint NOT NULL,
    text text,
    type integer,
    date_created timestamp without time zone DEFAULT now(),
    date_modified timestamp without time zone DEFAULT now(),
    user_created character varying(30) DEFAULT "current_user"(),
    user_modified character varying(30) DEFAULT "current_user"(),
    CONSTRAINT pk_documents_id PRIMARY KEY (id),
    CONSTRAINT unique_documents_section_id UNIQUE (section_id),
    CONSTRAINT fk_documents_sectionid FOREIGN KEY (section_id)
        REFERENCES kbase.sections (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)
TABLESPACE pg_default;

ALTER TABLE kbase.documents OWNER to kbase;
GRANT ALL ON TABLE kbase.documents TO kbase;
REVOKE ALL ON TABLE kbase.documents FROM kbase_user;
GRANT DELETE, UPDATE, INSERT, SELECT ON TABLE kbase.documents TO kbase_user;

COMMENT ON TABLE kbase.documents IS 'Скомпилированные документы.';
COMMENT ON COLUMN kbase.documents.type
    IS '1 - документы кешируются на локальном диске; 2 - кешируются в БД';

CREATE INDEX ind_documents_section_id
    ON kbase.documents USING btree
    (section_id ASC NULLS LAST)
    TABLESPACE pg_default;

----------------------------------------------------------
insert into kbase.documents (id,section_id,text,type,date_created,date_modified,user_created,user_modified)
select id,section_id,text,type,date_created,date_modified,user_created,user_modified
  from public.documents
;

------------------------------ 
-- вилучаємо старі об'єкти
DROP TABLE public.documents;
DROP SEQUENCE public.seq_documents;

--######## move table info_file ##################################################
CREATE SEQUENCE kbase.seq_info_file
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE kbase.seq_info_file OWNER TO kbase;

do $$ 
<<seq_info_file>>
declare
-- устанавливаем значение сиквенса
	v_maxId   bigint;
	v_result  bigint;
begin
	select max(id)
	  into v_maxId
	  from public.info_file
	;
	
	v_maxId := v_maxId + 1;
	SELECT pg_catalog.setval('kbase.seq_info_file', v_maxId, true) into v_result;
	raise notice 'v_result =  %', v_result;
end seq_info_file $$;

--------------------------------------------------------
CREATE TABLE kbase.info_file
(
    id bigint NOT NULL DEFAULT nextval('seq_info_file'::regclass),
    title character varying(255),
    file_body bytea,
    file_name character varying(255),
    icon_id bigint,
    descr character varying(255),
    text text,
    isshowtitle integer,
    isshowdescr integer,
    isshowtext integer,
    CONSTRAINT pk_info_file PRIMARY KEY (id)
)
TABLESPACE pg_default;

ALTER TABLE kbase.info_file OWNER to kbase;
GRANT ALL ON TABLE kbase.info_file TO kbase;
REVOKE ALL ON TABLE kbase.info_file FROM kbase_user;
GRANT SELECT ON TABLE kbase.info_file TO kbase_user;

COMMENT ON TABLE kbase.info_file
    IS 'Инфо блоки "Файл"';
COMMENT ON COLUMN kbase.info_file.isshowtitle
    IS '1 - показывать заголовок ; 0 или NULL - не показывать';
COMMENT ON COLUMN kbase.info_file.isshowdescr
    IS '1 - показывать описание ; 0 или NULL - не показывать';
COMMENT ON COLUMN kbase.info_file.isshowtext
    IS '1 - показывать текст ; 0 или NULL - не показывать';

ALTER TABLE kbase.info_file
    ADD CONSTRAINT fk_info_file_icon_id FOREIGN KEY (icon_id)
    REFERENCES kbase.icons (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION;

----------------------------------------------------------
insert into kbase.info_file (id,title,file_body,file_name,icon_id,descr,text,isshowtitle,isshowdescr,isshowtext)
select id,title,file_body,file_name,icon_id,descr,text,isshowtitle,isshowdescr,isshowtext
  from public.info_file
;

select 'public.info_file', count(*) cnt from public.info_file
union all
select 'kbase.info_file', count(*) cnt from kbase.info_file
;

------------------------------ 
-- вилучаємо старі об'єкти
DROP TABLE public.info_file;
DROP SEQUENCE public.seq_info_file;

--######## move table info_image ##################################################
CREATE SEQUENCE kbase.seq_info_image
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE kbase.seq_info_image OWNER TO kbase;

do $$ 
<<seq_info_image>>
declare
-- устанавливаем значение сиквенса
	v_maxId   bigint;
	v_result  bigint;
begin
	select max(id)
	  into v_maxId
	  from public.info_image
	;
	
	v_maxId := v_maxId + 1;
	SELECT pg_catalog.setval('kbase.seq_info_image', v_maxId, true) into v_result;
	raise notice 'v_result =  %', v_result;
end seq_info_image $$;

select * from kbase.seq_info_image;
select max(id) from public.info_image;

--------------------------------------------------------
CREATE TABLE kbase.info_image
(
    id bigint NOT NULL DEFAULT nextval('seq_info_image'::regclass),
    title character varying(255),
    image bytea,
    width integer,
    height integer,
    descr character varying(255),
    text text,
    isshowtitle integer,
    isshowdescr integer,
    isshowtext integer,
    CONSTRAINT pk_info_image PRIMARY KEY (id)
)
TABLESPACE pg_default;

ALTER TABLE kbase.info_image OWNER to kbase;
GRANT ALL ON TABLE kbase.info_image TO kbase;
REVOKE ALL ON TABLE kbase.info_image FROM kbase_user;
GRANT SELECT ON TABLE kbase.info_image TO kbase_user;

COMMENT ON TABLE kbase.info_image
    IS 'Инфо блоки "Изображение"';
COMMENT ON COLUMN kbase.info_image.width
    IS 'если не указана, то оригинальная';
COMMENT ON COLUMN kbase.info_image.height
    IS 'если не указана, то оригинальная';
COMMENT ON COLUMN kbase.info_image.isshowtitle
    IS '1 - показывать заголовок ; 0 или NULL - не показывать';
COMMENT ON COLUMN kbase.info_image.isshowdescr
    IS '1 - показывать описание ; 0 или NULL - не показывать';
COMMENT ON COLUMN kbase.info_image.isshowtext
    IS '1 - показывать текст ; 0 или NULL - не показывать';

----------------------------------------------------------
insert into kbase.info_image (id,title,image,width,height,descr,text,isshowtitle,isshowdescr,isshowtext)
select id,title,image,width,height,descr,text,isshowtitle,isshowdescr,isshowtext
  from public.info_image
;

select 'public.info_image' table_name, count(*) cnt from public.info_image
union all
select 'kbase.info_image', count(*) cnt from kbase.info_image
;

------------------------------ 
-- вилучаємо старі об'єкти
DROP TABLE public.info_image;
DROP SEQUENCE public.seq_info_image;

--######## move table info_text ##################################################
CREATE SEQUENCE kbase.seq_info_text
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE kbase.seq_info_text OWNER TO kbase;

do $$ 
<<seq_info_text>>
declare
-- устанавливаем значение сиквенса
	v_maxId   bigint;
	v_result  bigint;
begin
	select max(id)
	  into v_maxId
	  from public.info_text
	;
	
	v_maxId := v_maxId + 1;
	SELECT pg_catalog.setval('kbase.seq_info_text', v_maxId, true) into v_result;
	raise notice 'v_result =  %', v_result;
end seq_info_text $$;

select * from kbase.seq_info_text;
select max(id) from public.info_text;

--------------------------------------------------------
CREATE TABLE kbase.info_text
(
    id bigint NOT NULL DEFAULT nextval('seq_info_text'::regclass),
    title character varying(255),
    text text,
    isshowtitle integer,
    CONSTRAINT pk_info_text PRIMARY KEY (id)
)
TABLESPACE pg_default;

ALTER TABLE kbase.info_text OWNER to kbase;
GRANT ALL ON TABLE kbase.info_text TO kbase;
REVOKE ALL ON TABLE kbase.info_text FROM kbase_user;
GRANT SELECT ON TABLE kbase.info_text TO kbase_user;

COMMENT ON TABLE kbase.info_text
    IS 'Инфо блоки "Простой текст"';
COMMENT ON COLUMN kbase.info_text.isshowtitle
    IS '1 - показывать заголовок ; 0 или NULL - не показывать';

----------------------------------------------------------
insert into kbase.info_text (id,title,text,isshowtitle)
select id,title,text,isshowtitle
  from public.info_text
;

select 'public.info_text' table_name, count(*) cnt from public.info_text
union all
select 'kbase.info_text', count(*) cnt from kbase.info_text
;

------------------------------ 
-- вилучаємо старі об'єкти
DROP TABLE public.info_text;
DROP SEQUENCE public.seq_info_text;



--<<