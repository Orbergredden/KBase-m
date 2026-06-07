-- FUNCTION: kbase.section_geticoniddefault(bigint)

-- DROP FUNCTION IF EXISTS kbase.section_geticoniddefault(bigint);

CREATE OR REPLACE FUNCTION kbase.section_geticoniddefault(
	p_sectionid bigint)
    RETURNS bigint
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$

DECLARE
	v_sectionRec sections%rowtype;
	v_iconIdDef  bigint := 0;
BEGIN
	-- выбираем запись с разделом
	select s.*
	  into v_sectionRec
	  from sections s
	 where id = p_sectionId
	;
	-- проходим вверх по дереву к корню
	while (coalesce(v_sectionRec.icon_id_def,0) = 0) and (coalesce(v_sectionRec.parent_id,0) > 0) loop
		select s.*
		  into v_sectionRec
		  from sections s
		 where id = v_sectionRec.parent_id
		;
	end loop;

	-- если дефолтной иконки нет в разделах, то берем по умолчанию
	if coalesce(v_sectionRec.icon_id_def,0) = 0 then
		select cast(s.value as bigint)
		  into v_iconIdDef
		  from settings s
		 where s.alias = 'SECTION_ICON_DEFAULT'
		;
	else
		v_iconIdDef := v_sectionRec.icon_id_def;
	end if;

	return v_iconIdDef;
END;
$BODY$;

ALTER FUNCTION kbase.section_geticoniddefault(bigint)
    OWNER TO kbase;
