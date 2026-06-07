--
-- PostgeSQL KBase update
--

--\connect kbase_j_test2

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = public, pg_catalog;

-- ######## update table Settings for new version ##################################
update settings 
   set value = '1.01.00.016', 
       descr = 'add Image type of info blocks',
       date_modified = now(),
       user_modified = "current_user"()
 where alias = 'VERSION_DB_NUMBER'
;
update settings 
   set value = '06.11.2019 17:15', 
       descr = '',
       date_modified = now(),
       user_modified = "current_user"()
 where alias = 'VERSION_DB_END_DATE'
;
-- ######## INSERT infotype ###################################################################
insert into infotype (id,name,table_name,descr)
	values (2,'Изображение','info_image','для не слишком тяжелых картинок')
;

-- ######## CREATE info_image ############################################################
CREATE SEQUENCE public.seq_info_image;
ALTER SEQUENCE public.seq_info_image OWNER TO kbase;

CREATE TABLE public.info_image
(
    id bigint NOT NULL DEFAULT nextval('seq_info_image'::regclass),
    title character varying(255) COLLATE pg_catalog."default",
    image bytea,
    width integer,
    height integer,
    descr character varying(255) COLLATE pg_catalog."default",
    text text COLLATE pg_catalog."default",
    isshowtitle integer,
    isshowdescr integer,
    isshowtext integer,
    CONSTRAINT pk_info_image PRIMARY KEY (id)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public.info_image
    OWNER to kbase;

GRANT ALL ON TABLE public.info_image TO kbase;

GRANT SELECT ON TABLE public.info_image TO kbase_user;

COMMENT ON TABLE public.info_image
    IS 'Инфо блоки "Изображение"';

COMMENT ON COLUMN public.info_image.width
    IS 'если не указана, то оригинальная';

COMMENT ON COLUMN public.info_image.height
    IS 'если не указана, то оригинальная';

COMMENT ON COLUMN public.info_image.isshowtitle
    IS '1 - показывать заголовок ; 0 или NULL - не показывать';

COMMENT ON COLUMN public.info_image.isshowdescr
    IS '1 - показывать описание ; 0 или NULL - не показывать';

COMMENT ON COLUMN public.info_image.isshowtext
    IS '1 - показывать текст ; 0 или NULL - не показывать';
-- -