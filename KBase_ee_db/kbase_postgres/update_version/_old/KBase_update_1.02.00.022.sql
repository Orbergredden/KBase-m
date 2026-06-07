--
-- PostgeSQL KBase update
--

--\connect kbase_test

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
--SET client_encoding = 'WIN1251';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = kbase, public, pg_catalog;

-- ######## update table Settings for new version ##################################
update settings 
   set value = '1.02.01.022', 
       descr = 'create functions for grafana',
       date_modified = now(),
       user_modified = "current_user"()
 where alias = 'VERSION_DB_NUMBER'
;
update settings 
   set value = '19.03.2021 15:27', 
       descr = '',
       date_modified = now(),
       user_modified = "current_user"()
 where alias = 'VERSION_DB_END_DATE'
;
--######## create functions ############################################################
CREATE OR REPLACE FUNCTION kbase.report_documents_get_modified(
	p_date_begin timestamp without time zone,
	p_date_end timestamp without time zone)
    RETURNS TABLE(
		op character varying, 
		sectionid bigint, 
		infoid bigint,
		infotypeid bigint,
		name character varying, 
		sectionpath character varying, 
		date_op timestamp without time zone, 
		user_op character varying
		) 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE SECURITY DEFINER PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
-- список изменений разделов и инфоблоков за период
DECLARE
	l_i         RECORD;
BEGIN
	IF (p_date_begin IS NULL) or (p_date_end is null) THEN
    	RETURN;
  	END IF;
  
  	FOR l_i IN (with params as (
                select p_date_begin as DateBegin, p_date_end as DateEnd
                ),      -- params
                s as (
                select 'CS' as op, s.id as sectionid, 0 as infoid, 0 as infotypeid, s.name
                      ,kbase.section_get_pathname(s.id,' | ') as sectionPath
                      ,s.date_created as date_op
	                  ,s.user_created as user_op
                  from sections s
                 where s.date_created between (select DateBegin from params) and (select DateEnd from params)
                union all
                select 'MS' as op, s.id as sectionid, 0 as infoid, 0 as infotypeid, s.name
                      ,kbase.section_get_pathname(s.id,' | ') as sectionPath
                      ,s.date_modified as date_op
	                  ,s.user_modified as user_op
                  from sections s
                 where s.date_modified between (select DateBegin from params) and (select DateEnd from params)
                   and s.date_modified <> s.date_created
                union all
                select 'CI' as op, i.sectionid, i.id as infoid, i.infotypeid, i.name
                      ,kbase.section_get_pathname(i.sectionid,' | ') as sectionPath
                      ,i.date_created as date_op
	                  ,i.user_created as user_op
                  from info i
                 where i.date_created between (select DateBegin from params) and (select DateEnd from params)
                union all
                select 'MI' as op, i.sectionid, i.id as infoid, i.infotypeid, i.name
                      ,kbase.section_get_pathname(i.sectionid,' | ') as sectionPath
                      ,i.date_modified as date_op
	                  ,i.user_modified as user_op
                  from info i
                 where i.date_modified between (select DateBegin from params) and (select DateEnd from params)
                   and i.date_modified <> i.date_created
                )
                select s.op, s.sectionid, s.infoid, s.infotypeid, s.name, s.sectionPath, s.date_op, s.user_op
                  from s
                 order by s.date_op desc
			   ) 
	LOOP
		op := l_i.op;
		sectionid := l_i.SectionId;
		infoid := l_i.infoid;
		infotypeid := l_i.infotypeid;
		name := l_i.name;
		sectionpath := l_i.sectionpath;
		date_op := l_i.date_op;
		user_op := l_i.user_op;
	
		RETURN NEXT;
  	END LOOP;
END;
$BODY$;

ALTER FUNCTION kbase.report_documents_get_modified(timestamp without time zone,timestamp without time zone) OWNER TO kbase;

grant execute on function kbase.report_documents_get_modified(timestamp without time zone,timestamp without time zone) to kbase_view;

COMMENT ON FUNCTION kbase.report_documents_get_modified(timestamp without time zone,timestamp without time zone)
    IS 'список изменений разделов и инфоблоков за период';

--######## modify functions ####################################################################
CREATE OR REPLACE FUNCTION kbase.section_get_pathname(
	p_sectionid bigint,
	p_delimiter character varying DEFAULT ' / '::character varying)
    RETURNS character varying
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
-- get section's tree path
DECLARE
	l_i record;
	v_retVal character varying (10000);
	v_isFirst boolean := true;
BEGIN
	FOR l_i IN (WITH RECURSIVE SectionPath ( id, parent_id, name ) AS 
                (SELECT sc.id, sc.parent_id, sc.name 
                   FROM sections sc 
                  WHERE sc.id = p_sectionId
                  UNION 
                 SELECT sp.id, sp.parent_id, sp.name 
                   FROM sections sp 
                  INNER JOIN SectionPath ON (SectionPath.parent_id = sp.id) 
                 ) 
                 select * from SectionPath --order by parent_id desc
	           )
	LOOP
		IF v_isFirst THEN
			v_isFirst := false;
			v_retVal := l_i.name;
		ELSE
			v_retVal := l_i.name || p_delimiter || v_retVal;
		END IF;
	END LOOP;

    return v_retVal;
END;
$BODY$;

ALTER FUNCTION kbase.section_get_pathname(bigint, character varying)
    OWNER TO kbase;

COMMENT ON FUNCTION kbase.section_get_pathname(bigint, character varying)
    IS 'get section''s tree path';
--<<