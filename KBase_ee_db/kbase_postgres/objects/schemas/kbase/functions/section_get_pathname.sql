-- FUNCTION: section_get_pathname(bigint, character varying)

-- DROP FUNCTION section_get_pathname(bigint, character varying);

-- SHOW search_path;
-- SET search_path TO kbase,public;

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
