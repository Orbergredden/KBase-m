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

SET search_path = kbase, public, pg_catalog;

-- ######## update table Settings for new version ##################################
update settings 
   set value = '1.02.00.020', 
       descr = 'create hand function',
       date_modified = now(),
       user_modified = "current_user"()
 where alias = 'VERSION_DB_NUMBER'
;
update settings 
   set value = '17.06.2020 20:20', 
       descr = '',
       date_modified = now(),
       user_modified = "current_user"()
 where alias = 'VERSION_DB_END_DATE'
;
-- ######## rename function ################################
DROP FUNCTION section_getpathname(bigint, character varying);

CREATE OR REPLACE FUNCTION section_get_pathname(
	p_sectionid bigint,
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

ALTER FUNCTION section_get_pathname(bigint, character varying)
    OWNER TO kbase;

COMMENT ON FUNCTION section_get_pathname(bigint, character varying)
    IS 'get section''s tree path';

-- ######## create function ################################
CREATE OR REPLACE FUNCTION section_get_list_id_subsections(
	p_sectionid bigint)
		
    RETURNS TABLE(id bigint) 
    LANGUAGE 'sql'
    COST 100
    VOLATILE SECURITY DEFINER 
    ROWS 1000
AS $BODY$
	-- return list of sections (id) from section with p_sectionid
	WITH RECURSIVE x(id) AS (
	                 SELECT id
                       FROM sections
                      WHERE id = $1        -- section id
                     UNION  ALL
                     SELECT a.id
                       FROM x
                       JOIN sections a ON a.parent_id = x.id
	)
    select s.id
      from sections s
      join x on x.id = s.id
$BODY$;

ALTER FUNCTION section_get_list_id_subsections(bigint) OWNER TO kbase;

COMMENT ON FUNCTION section_get_list_id_subsections(bigint)
    IS 'return list of sections (id) from section with p_sectionid';

----------------------------------------------------------------------
CREATE OR REPLACE FUNCTION manual_get_documents_from_section(
	p_sectionid bigint)
		
    RETURNS TABLE(sectionid bigint,
	              parent_id bigint,
				  name character varying,
				  sectionPathName character varying,
				  icon_id bigint,
				  descr character varying,
				  date_created timestamp without time zone,
                  date_modified timestamp without time zone,
                  user_created character varying,
                  user_modified character varying,
				  date_modified_info timestamp without time zone,
                  icon_id_root bigint,
                  icon_id_def bigint,
                  theme_id bigint,
                  cache_type integer
				 ) 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE SECURITY DEFINER 
    ROWS 1000
AS $BODY$
  -- return list of sections from section with p_sectionid
DECLARE
	v_sql_str character varying := 'select s.id
                        ,s.parent_id
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

ALTER FUNCTION manual_get_documents_from_section(bigint) OWNER TO kbase;

COMMENT ON FUNCTION manual_get_documents_from_section(bigint)
    IS 'return list of sections from section with p_sectionid';

--######## update function ###################################################
CREATE OR REPLACE FUNCTION manual_search_doc_by_icon(
	i_iconid bigint)
    RETURNS TABLE(icon_id bigint, sectionid bigint, sectionname character varying, sectionpathname character varying, date_created timestamp without time zone, date_modified timestamp without time zone, user_created character varying, user_modified character varying) 
    LANGUAGE 'sql'

    COST 100
    VOLATILE SECURITY DEFINER 
    ROWS 1000
    
AS $BODY$
	-- search documents with icon i_iconId
	select icon_id, 
		   id as sectionid, 
		   name as sectionname, 
		   section_get_pathname(id,' | ') as sectionpathname, 
		   date_created, 
		   date_modified, 
		   user_created, 
		   user_modified
	  from sections
	 where icon_id = $1
$BODY$;

ALTER FUNCTION manual_search_doc_by_icon(bigint)
    OWNER TO kbase;

COMMENT ON FUNCTION manual_search_doc_by_icon(bigint)
    IS 'search documents by icon';

--######## change function ########################################################
CREATE OR REPLACE FUNCTION manual_search_doc_by_text(
	p_str character varying)
    RETURNS TABLE(texttype character varying, sectionid bigint, sectionname character varying, sectionpathname character varying, infoheaderid bigint, infoheadername character varying, text character varying, date_created timestamp without time zone, date_modified timestamp without time zone, user_created character varying, user_modified character varying) 
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
				      ,section_get_pathname(i.sectionid,' | ') as sectionPathName
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
				      ,section_get_pathname(s.id,' | ') as sectionPathName
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

ALTER FUNCTION manual_search_doc_by_text(character varying)
    OWNER TO kbase;

COMMENT ON FUNCTION manual_search_doc_by_text(character varying)
    IS 'search documents by text';
--<<