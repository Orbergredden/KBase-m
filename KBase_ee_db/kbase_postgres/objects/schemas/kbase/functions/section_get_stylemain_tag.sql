-- FUNCTION: kbase.section_get_stylemain_tag(bigint)

-- DROP FUNCTION IF EXISTS kbase.section_get_stylemain_tag(bigint);

CREATE OR REPLACE FUNCTION kbase.section_get_stylemain_tag(
	v_sectionid bigint)
    RETURNS character varying
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
-- Повертає тег головного стиля для вказаного розділа
-- шукає з проходом до кореня (и по замовчанню з конфігу, якщо нічого не вказано)
DECLARE
	v_sectionRec sections%rowtype;
	v_tag        template_style.tag%type;
BEGIN
	-- выбираем запись с разделом 
	select s.*
	  into v_sectionRec
	  from sections s
	 where id = v_sectionId
	;
	-- якщо є тег , то одразу його забираємо
	if coalesce(v_sectionRec.template_main,'') <> '' then
		return v_sectionRec.template_main;
	end if;

	-- проходим вверх по дереву к корню
	while ((coalesce(v_sectionRec.template_main,'') = '') or 
	       ((coalesce(v_sectionRec.template_main,'') <> '') and 
	        (coalesce(v_sectionRec.template_main_tree,0) = 0))) and 
	      (coalesce(v_sectionRec.parent_id,0) > 0) loop
		select s.*
		  into v_sectionRec
		  from sections s
		 where id = v_sectionRec.parent_id
		;
	end loop;

	-- если тега нет в разделах, то берем по умолчанию
	if (coalesce(v_sectionRec.template_main,'') = '') or 
	   ((coalesce(v_sectionRec.template_main,'') <> '') and 
	    (coalesce(v_sectionRec.template_main_tree,0) = 0)) then
		select s.value
		  into v_tag
		  from settings s
		 where s.alias = 'SECTION_TEMPLATE_MAIN_DEFAULT'
		;
	else
		v_tag := v_sectionRec.template_main;
	end if;

	return v_tag;
END;
$BODY$;

ALTER FUNCTION kbase.section_get_stylemain_tag(bigint)
    OWNER TO postgres;

COMMENT ON FUNCTION kbase.section_get_stylemain_tag(bigint)
    IS 'Повертає тег головного стиля для вказаного розділа. Шукає з проходом до кореня (и по замовчанню з конфігу, якщо нічого не вказано)';
