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
   set value = '1.02.00.018', 
       descr = 'add File type of info blocks',
       date_modified = now(),
       user_modified = "current_user"()
 where alias = 'VERSION_DB_NUMBER'
;
update settings 
   set value = '13.01.2020 17:08', 
       descr = '',
       date_modified = now(),
       user_modified = "current_user"()
 where alias = 'VERSION_DB_END_DATE'
;
-- ######## INSERT infotype ###################################################################
insert into infotype (id,name,table_name,descr)
	values (3,'Файл','info_file','для не слишком тяжелых файлов')
;

-- ######## CREATE info_image ############################################################
CREATE SEQUENCE public.seq_info_file;
ALTER SEQUENCE public.seq_info_file OWNER TO kbase;

CREATE TABLE public.info_file
(
    id bigint NOT NULL DEFAULT nextval('seq_info_file'::regclass),
    title character varying(255) COLLATE pg_catalog."default",
    file_body bytea,
	file_name character varying(255) COLLATE pg_catalog."default",
	icon_id bigint,
    descr character varying(255) COLLATE pg_catalog."default",
    text text COLLATE pg_catalog."default",
    isshowtitle integer,
    isshowdescr integer,
    isshowtext integer,
    CONSTRAINT pk_info_file PRIMARY KEY (id)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public.info_file
    OWNER to kbase;

GRANT ALL ON TABLE public.info_file TO kbase;

GRANT SELECT ON TABLE public.info_file TO kbase_user;

COMMENT ON TABLE public.info_file
    IS 'Инфо блоки "Файл"';

COMMENT ON COLUMN public.info_file.isshowtitle
    IS '1 - показывать заголовок ; 0 или NULL - не показывать';

COMMENT ON COLUMN public.info_file.isshowdescr
    IS '1 - показывать описание ; 0 или NULL - не показывать';

COMMENT ON COLUMN public.info_file.isshowtext
    IS '1 - показывать текст ; 0 или NULL - не показывать';

-- ######## update ###################################################################
CREATE OR REPLACE FUNCTION public.info_delete1(
	p_id bigint)
    RETURNS integer
    LANGUAGE 'plpgsql'

    COST 100
    VOLATILE 
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

ALTER FUNCTION public.info_delete1(bigint)
    OWNER TO kbase;

COMMENT ON FUNCTION public.info_delete1(bigint)
    IS 'удаление одного инфоблока';
--------------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.info_delete(
	p_sectionid bigint)
    RETURNS integer
    LANGUAGE 'plpgsql'

    COST 100
    VOLATILE 
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

ALTER FUNCTION public.info_delete(bigint)
    OWNER TO kbase;

COMMENT ON FUNCTION public.info_delete(bigint)
    IS 'удаление всех инфо блоков указанного раздела';
--------------------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.section_copyinfoblocks(
	p_sectionsrcid bigint,
	p_sectiontrgid bigint)
    RETURNS integer
    LANGUAGE 'plpgsql'

    COST 100
    VOLATILE 
AS $BODY$

declare
	v_rec        record;
    v_infoId_new bigint;
begin
	for v_rec in (select id, sectionid, infotypeid, infoid, infotypestyleid,
                         position, name, descr,
                         date_created, date_modified, user_created, user_modified
                    from info
                   where sectionId = p_sectionSrcId
                   order by position)
    loop
    	-------- copy info block
		case when v_rec.infoTypeId = 1        -- Простой текст
			 then 
             	select nextval('seq_info_text') into v_infoId_new;
             
             	insert into info_text (id, title, text, isshowtitle)
				select v_infoId_new, title, text, isShowTitle
				  from info_text
				 where id = v_rec.infoId
				;
			 when v_rec.infoTypeId = 2        -- Изображение
			 then 
             	select nextval('seq_info_image') into v_infoId_new;
             
             	insert into info_image (id, title, image, width, height, descr, text, isshowtitle, isshowdescr, isshowtext)
				select v_infoId_new, title, image, width, height, descr, text, isshowtitle, isshowdescr, isshowtext
				  from info_Image
				 where id = v_rec.infoId
				;
			 when v_rec.infoTypeId = 3        -- Файл
			 then 
             	select nextval('seq_info_file') into v_infoId_new;
             
             	insert into info_file (id, title, file_body, file_name, icon_id, descr, text, isshowtitle, isshowdescr, isshowtext)
				select v_infoId_new, title, file_body, file_name, icon_id, descr, text, isshowtitle, isshowdescr, isshowtext
				  from info_file
				 where id = v_rec.infoId
				;	
             else 
             	raise exception 'section_copyinfoblocks : Not existing type of info block , infoTypeId = %', v_rec.infoTypeId;
		end case;

		-------- copy info header
		insert into info (id, sectionid, infotypeid, infoid, infotypestyleid,
                          position, name, descr,
                          date_created, date_modified, user_created, user_modified)
        	values (nextval('seq_info'), p_sectionTrgId, v_rec.infoTypeId, v_infoId_new, v_rec.infoTypeStyleId,
                    v_rec.position, v_rec.name, v_rec.descr,
                    v_rec.date_created, v_rec.date_modified, v_rec.user_created, v_rec.user_modified)
        ;
    end loop;

	return 0;
end;
$BODY$;
-- -