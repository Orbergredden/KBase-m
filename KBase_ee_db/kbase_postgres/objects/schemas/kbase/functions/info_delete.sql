-- FUNCTION: kbase.info_delete(bigint)

-- DROP FUNCTION IF EXISTS kbase.info_delete(bigint);

CREATE OR REPLACE FUNCTION kbase.info_delete(
	p_sectionid bigint)
    RETURNS integer
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
-- удаление всех инфо блоков указанного раздела
DECLARE
	v_retVal  integer := 0;
	v_rec     record;
BEGIN
	-- delete info blocks
	for v_rec in (select id, infoTypeId, infoId
                    from info
                   where sectionId = p_sectionId)
	loop
		case v_rec.infoTypeId
        	when 1 then             -- Простой текст
				delete from info_text where id = v_rec.infoId;
			when 2 then             -- Изображение	
				delete from info_image where id = v_rec.infoId;
			when 3 then             -- Файл	
				delete from info_file where id = v_rec.infoId;
			else 
				raise exception 'info_delete : Not existing type of info block , infoTypeId = %', v_rec.infoTypeId;
		end case;
    end loop;

	-- delete info headers
	delete from info where sectionId = p_sectionId;

	return v_retVal;
END;
$BODY$;

ALTER FUNCTION kbase.info_delete(bigint)
    OWNER TO kbase;

COMMENT ON FUNCTION kbase.info_delete(bigint)
    IS 'удаление всех инфо блоков указанного раздела';
