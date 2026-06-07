--
-- PostgeSQL KBase update
--

--\connect kbase_test

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
--SET client_encoding = 'WIN1251';
SET lc_messages TO 'en_US.UTF-8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = kbase, public, pg_catalog;

-- ######## перевіряємо щоб БД була попередньої версії #############################
do $$ 
<<check_version>>
declare
	v_version_old  varchar(50) := '2.01.00.028';
	v_version      varchar(50);
begin
	select s.value 
	  into v_version
	  from settings s 
	 where s.alias = 'VERSION_DB_NUMBER'
	;
	if v_version_old <> v_version then
		--raise notice 'DB version too old, (% <> %)', v_version, v_version_old;
		RAISE EXCEPTION 'DB version too old, (% <> %)', v_version, v_version_old;
	end if;
end check_version $$;

-- ######## update table Settings for new version ##################################
update settings 
   set value = '2.02.00.029', 
       descr = 'delete Dictionaries',
       date_modified = now(),
       user_modified = "current_user"()
 where alias = 'VERSION_DB_NUMBER'
;
update settings 
   set value = '03.05.2024', 
       descr = '',
       date_modified = now(),
       user_modified = "current_user"()
 where alias = 'VERSION_DB_END_DATE'
;
--######## change section_delete () #######################################
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
                  select s.id, s.type_id
                    from sections s
                    join x on x.id = s.id' using p_sectionId
    loop
		if v_rec.type_id = 1 then      -- document
			delete from documents where section_id = v_rec.id;
			select info_delete(v_rec.id) into v_infoDeleteResult;
		end if;
		
		if v_rec.type_id = 2 then      -- dictionary
			delete from dict where section_id = v_rec.id;
		end if;
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
--<<