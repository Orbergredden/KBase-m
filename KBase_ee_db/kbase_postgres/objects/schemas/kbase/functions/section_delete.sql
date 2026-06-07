-- FUNCTION: kbase.section_delete(bigint)

-- DROP FUNCTION IF EXISTS kbase.section_delete(bigint);

CREATE OR REPLACE FUNCTION kbase.section_delete(
	p_sectionid bigint)
    RETURNS integer
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$

DECLARE
	v_rec              record;
    v_infoDeleteResult integer;
    v_retVal           integer := 0;
BEGIN
	-- delete documents and info blocks
	for v_rec in execute 'WITH RECURSIVE x(id) AS (
	                 SELECT id
                       FROM sections
                      WHERE id = $1
                     UNION  ALL
                     SELECT a.id
                       FROM x
                       JOIN sections a ON a.parent_id = x.id)
                  select s.id
                    from sections s
                    join x on x.id = s.id' using p_sectionId
    loop
		delete from documents where section_id = v_rec.id;
		select info_delete(v_rec.id) into v_infoDeleteResult;
    end loop;

	-- delete sections
    WITH RECURSIVE x AS (
		SELECT id
	      FROM sections
	     WHERE id = p_sectionId
	    UNION  ALL
	    SELECT a.id
	      FROM x
	      JOIN sections a ON a.parent_id = x.id
	)
	DELETE FROM sections a
	 USING  x
	 WHERE a.id = x.id
	;

	return v_retVal;
END;
$BODY$;

ALTER FUNCTION kbase.section_delete(bigint)
    OWNER TO kbase;
