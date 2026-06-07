-- SHOW search_path;
-- SET search_path TO kbase,public;

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
