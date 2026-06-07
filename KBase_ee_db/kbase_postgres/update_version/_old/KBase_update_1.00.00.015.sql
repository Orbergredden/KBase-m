--
-- PostgeSQL KBase update
--

--\connect kbase_j_test

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = public, pg_catalog;

-- ######## update table Settings for new version ##################################
update settings 
   set value = '1.00.00.015', 
       descr = 'function - info blocks copy',
       date_modified = now(),
       user_modified = "current_user"()
 where alias = 'VERSION_DB_NUMBER'
;
update settings 
   set value = '01.06.2018 15:37', 
       descr = '',
       date_modified = now(),
       user_modified = "current_user"()
 where alias = 'VERSION_DB_END_DATE'
;
-- ######## update ###################################################################

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
             else 
             	raise exception 'Not existing type of info block , infoTypeId = %', v_rec.infoTypeId;
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

ALTER FUNCTION public.section_copyinfoblocks(bigint, bigint)
    OWNER TO kbase;

COMMENT ON FUNCTION public.section_copyinfoblocks(bigint, bigint)
    IS 'копирование всех инфо блоков с одного раздела в другой';


-- -