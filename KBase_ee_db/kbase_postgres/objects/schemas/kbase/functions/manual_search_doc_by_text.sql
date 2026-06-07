-- SHOW search_path;
-- SET search_path TO kbase,public; 

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
