-- SHOW search_path;
-- SET search_path TO kbase,public;

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