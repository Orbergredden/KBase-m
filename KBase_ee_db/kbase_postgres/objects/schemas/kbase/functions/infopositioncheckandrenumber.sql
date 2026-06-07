-- FUNCTION: kbase.infopositioncheckandrenumber(bigint, bigint)

-- DROP FUNCTION IF EXISTS kbase.infopositioncheckandrenumber(bigint, bigint);

CREATE OR REPLACE FUNCTION kbase.infopositioncheckandrenumber(
	p_sectionid bigint,
	p_newposition bigint)
    RETURNS bigint
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$

DECLARE
	v_CounterPos bigint := 10;

	v_countR INTEGER;
	v_retVal bigint;
	v_rec    RECORD;
BEGIN
	v_retVal := p_newPosition;

	-- проверяем, есть ли такая позиция уже в БД
	select count(*)
      into v_countR
      from info
     where sectionId = p_sectionId
       and position = p_newPosition
     ;
    -- если есть , делаем перенумерацию
    if v_countR > 0 then
		for v_rec in (select id
                       from info
                      where sectionId = p_sectionId
                        and position < p_newPosition
                      order by position)
        loop
			update info
               set position = v_CounterPos
             where id = v_rec.id
			;
			v_CounterPos := v_CounterPos + 10;
        end loop;

		v_retVal := v_CounterPos;
        v_CounterPos := v_CounterPos + 10;

		for v_rec in (select id
                       from info
                      where sectionId = p_sectionId
                        and position >= p_newPosition
                      order by position)
        loop
			update info
               set position = v_CounterPos
             where id = v_rec.id
			;
			v_CounterPos := v_CounterPos + 10;
        end loop;
	end if;

	return v_retVal;
END;
$BODY$;

ALTER FUNCTION kbase.infopositioncheckandrenumber(bigint, bigint)
    OWNER TO kbase;
