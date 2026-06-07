-- FUNCTION: kbase.info_delete1(bigint)

-- DROP FUNCTION IF EXISTS kbase.info_delete1(bigint);

CREATE OR REPLACE FUNCTION kbase.info_delete1(
	p_id bigint)
    RETURNS integer
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
-- удаление одного инфоблока
DECLARE
	v_infoTypeId   bigint;
	v_infoId       bigint;
BEGIN
	-- delete info block
    select infoTypeId, infoId
      into v_infoTypeId, v_infoId
      from info
     where id = p_id
    ;
    case v_infoTypeId
        when 1 then             -- Простой текст
			delete from info_text where id = v_infoId;
		when 2 then             -- Изображение	
			delete from info_image where id = v_infoId;
		when 3 then             -- Файл	
			delete from info_file where id = v_infoId;
		else 
             raise exception 'info_delete1 : Not existing type of info block , infoTypeId = %', v_infoTypeId;
	end case;

	-- delete info header
	delete from info where id = p_id;

    return 0;
END;
$BODY$;

ALTER FUNCTION kbase.info_delete1(bigint)
    OWNER TO kbase;

COMMENT ON FUNCTION kbase.info_delete1(bigint)
    IS 'удаление одного инфоблока';
