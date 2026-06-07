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
   set value = '1.02.00.019', 
       descr = 'create schema kbase; ',
       date_modified = now(),
       user_modified = "current_user"()
 where alias = 'VERSION_DB_NUMBER'
;
update settings 
   set value = '11.05.2020 15:17', 
       descr = '',
       date_modified = now(),
       user_modified = "current_user"()
 where alias = 'VERSION_DB_END_DATE'
;
-- ######## create schema ###################################################################
CREATE SCHEMA kbase;

COMMENT ON SCHEMA kbase IS 'kbase documents storage';

GRANT ALL ON SCHEMA kbase TO kbase;
GRANT ALL ON SCHEMA kbase TO kbase_user;
GRANT ALL ON SCHEMA kbase TO postgres;

-- ######## create functions ###############################################################
CREATE OR REPLACE FUNCTION kbase.section_getPathName(
	p_sectionId bigint, 
	p_delimiter character varying DEFAULT ' / '::character varying)
    RETURNS character varying
    LANGUAGE 'plpgsql'

    COST 100
    VOLATILE 
    
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
                 select * from SectionPath order by parent_id desc
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

ALTER FUNCTION kbase.section_getPathName(bigint, character varying) OWNER TO kbase;

COMMENT ON FUNCTION kbase.section_getPathName(bigint, character varying)
    IS 'get section''s tree path';
--------------------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION kbase.manual_search_doc_by_text(
	p_str character varying)
		
    RETURNS TABLE(TextType character varying,
				  sectionid bigint,
				  sectionName character varying,
				  sectionPathName character varying,
				  InfoHeaderId bigint,
				  InfoHeaderName character varying,
				  text character varying,
				  date_created timestamp without time zone,
                  date_modified timestamp without time zone,
                  user_created character varying,
                  user_modified character varying
				 ) 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE SECURITY DEFINER 
    ROWS 1000
AS $BODY$
  -- search documents with text p_str
DECLARE
	l_i         RECORD;
BEGIN
	IF p_str IS NULL THEN
    	RETURN;
  	END IF;
  
  	FOR l_i IN (with params as (
                select '%'||upper(p_str)||'%' as SearchMask
                ),  -- params
				texts as (
                select 'text,text' as TextType, i.id, t.text as text
                  from info_text t
                  join info i      on i.infoid = t.id
                                  and i.infotypeid = 1
                 where upper(t.text) like (select SearchMask from params)
                union all
                select 'text,title' as TextType, i.id, t.title as text
                  from info_text t
                  join info i      on i.infoid = t.id
                                   and i.infotypeid = 1
                 where upper(t.title) like (select SearchMask from params)
				union all
				select 'image,title' as TextType, i.id, t.title as text
                  from info_image t
                  join info i      on i.infoid = t.id
                                   and i.infotypeid = 2
                 where upper(t.title) like (select SearchMask from params)
				union all
				select 'image,descr' as TextType, i.id, t.descr as text
                  from info_image t
                  join info i      on i.infoid = t.id
                                   and i.infotypeid = 2
                 where upper(t.descr) like (select SearchMask from params)
				union all
				select 'image,text' as TextType, i.id, t.text as text
                  from info_image t
                  join info i      on i.infoid = t.id
                                   and i.infotypeid = 2
                 where upper(t.text) like (select SearchMask from params)
				union all
				select 'file,title' as TextType, i.id, t.title as text
                  from info_file t
                  join info i      on i.infoid = t.id
                                   and i.infotypeid = 3
                 where upper(t.title) like (select SearchMask from params)
				union all
				select 'file,file_name' as TextType, i.id, t.file_name as text
                  from info_file t
                  join info i      on i.infoid = t.id
                                   and i.infotypeid = 3
                 where upper(t.file_name) like (select SearchMask from params)
				union all
				select 'file,descr' as TextType, i.id, t.descr as text
                  from info_file t
                  join info i      on i.infoid = t.id
                                   and i.infotypeid = 3
                 where upper(t.descr) like (select SearchMask from params)
				union all
				select 'file,text' as TextType, i.id, t.text as text
                  from info_file t
                  join info i      on i.infoid = t.id
                                   and i.infotypeid = 3
                 where upper(t.text) like (select SearchMask from params)
				union all
				select 'info,name' as TextType, i.id, i.name as text
                  from info i
                 where upper(i.name) like (select SearchMask from params)
				union all
				select 'info,descr' as TextType, i.id, i.descr as text
                  from info i
                 where upper(i.descr) like (select SearchMask from params)
                ),   -- texts
				t_section as (
				select 'section,name' as TextType, s.id, s.name as text
				  from sections s
				 where upper(s.name) like (select SearchMask from params)
				union all
				select 'section,descr' as TextType, s.id, s.descr as text
				  from sections s
				 where upper(s.descr) like (select SearchMask from params)
				)    -- t_section
				select texts.TextType
				      ,i.sectionid
				      ,s.name as sectionName
				      ,kbase.section_getpathname(i.sectionid,' | ') as sectionPathName
				      ,texts.id as InfoHeaderId
				      ,i.name as InfoHeaderName
				      ,texts.text
				      ,i.date_created 
                      ,i.date_modified
                      ,i.user_created
                      ,i.user_modified
				  from texts
				  join info i     on i.id = texts.id
				  join sections s on s.id = i.sectionid
				union all
				select t_section.TextType
				      ,s.id as sectionid
				      ,s.name as sectionName
				      ,kbase.section_getpathname(s.id,' | ') as sectionPathName
				      ,null as InfoHeaderId
				      ,null as InfoHeaderName
				      ,t_section.text
				      ,s.date_created 
                      ,s.date_modified
                      ,s.user_created
                      ,s.user_modified
				  from t_section 
				  join sections s on s.id = t_section.id
			   ) 
	LOOP
		TextType := l_i.TextType;
		SectionId := l_i.SectionId;
		sectionName := l_i.sectionName;
		sectionPathName := l_i.sectionPathName;
		InfoHeaderId := l_i.InfoHeaderId;
		InfoHeaderName := l_i.InfoHeaderName;
		text := l_i.text;
    	date_created := l_i.date_created;
        date_modified := l_i.date_modified;
        user_created := l_i.user_created;
        user_modified := l_i.user_modified;
	
		RETURN NEXT;
  	END LOOP;
END;
$BODY$;
-- select * from kbase.manual_search_doc_by_text('trb');
-- DROP FUNCTION kbase.manual_search_doc_by_text(character varying);

ALTER FUNCTION kbase.manual_search_doc_by_text(character varying) OWNER TO kbase;

COMMENT ON FUNCTION kbase.manual_search_doc_by_text(character varying)
    IS 'search documents by text';
--------------------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION kbase.manual_search_doc_by_icon(
	i_iconId bigint)
    RETURNS TABLE(icon_id bigint, 
				  sectionid bigint, 
				  sectionname character varying, 
				  sectionpathname character varying, 
				  date_created timestamp without time zone, 
				  date_modified timestamp without time zone, 
				  user_created character varying, 
				  user_modified character varying) 
    LANGUAGE 'sql'

    COST 100
    VOLATILE SECURITY DEFINER 
    ROWS 1000
    
AS $BODY$
	-- search documents with icon i_iconId
	select icon_id, 
		   id as sectionid, 
		   name as sectionname, 
		   kbase.section_getpathname(id,' | ') as sectionpathname, 
		   date_created, 
		   date_modified, 
		   user_created, 
		   user_modified
	  from sections
	 where icon_id = $1
$BODY$;
-- select * from kbase.manual_search_doc_by_icon(150) order by SectionPathName;

ALTER FUNCTION kbase.manual_search_doc_by_icon(bigint) OWNER TO kbase;

COMMENT ON FUNCTION kbase.manual_search_doc_by_icon(bigint)
    IS 'search documents by icon';
--<<