-- FUNCTION: kbase.section_getthemeid(bigint)

-- DROP FUNCTION IF EXISTS kbase.section_getthemeid(bigint);

CREATE OR REPLACE FUNCTION kbase.section_getthemeid(
	v_sectionid bigint)
    RETURNS bigint
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$

DECLARE
	v_sectionRec sections%rowtype;
	v_themeId    bigint := 0;
BEGIN
	-- выбираем запись с разделом 
	select s.*
	  into v_sectionRec
	  from sections s
	 where id = v_sectionId
	;
	-- проходим вверх по дереву к корню
	while (coalesce(v_sectionRec.theme_id,0) = 0) and (coalesce(v_sectionRec.parent_id,0) > 0) loop
		select s.*
		  into v_sectionRec
		  from sections s
		 where id = v_sectionRec.parent_id
		;
	end loop;

	-- если темы нет в разделах, то берем по умолчанию
	if coalesce(v_sectionRec.theme_id,0) = 0 then
		select cast(s.value as bigint)
		  into v_themeId
		  from settings s
		 where s.alias = 'SECTION_THEME_DEFAULT'
		;
	else
		v_themeId := v_sectionRec.theme_id;
	end if;

	return v_themeId;
END;
$BODY$;

ALTER FUNCTION kbase.section_getthemeid(bigint)
    OWNER TO kbase;
