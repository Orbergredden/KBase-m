CREATE OR REPLACE FUNCTION kbase.section_get_list_id_subsections(
	p_sectionid bigint)
		
    RETURNS TABLE(id bigint) 
    LANGUAGE 'sql'
    COST 100
    VOLATILE SECURITY DEFINER 
    ROWS 1000
AS $BODY$
	-- return list of sections from section with p_sectionid
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

-- drop FUNCTION kbase.section_get_list_id_subsections(p_sectionid bigint);