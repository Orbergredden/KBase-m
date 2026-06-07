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
	v_version_old  varchar(50) := '1.04.00.025';
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
   set value = '1.04.01.026', 
       descr = 'dynamic load section tree',
       date_modified = now(),
       user_modified = "current_user"()
 where alias = 'VERSION_DB_NUMBER'
;
update settings 
   set value = '20.10.2023', 
       descr = '',
       date_modified = now(),
       user_modified = "current_user"()
 where alias = 'VERSION_DB_END_DATE'
;
--######## insert table settings #######################################
insert into kbase.settings (alias, section, subject, name, value, descr)
values ('SECTION_TREE_DYNAMIC_LOAD', 'Section', 'Tree', 'isDynamicLoad', '1',
        '0 - дерево розділів завантажується у додаток повністю, 1 - динамічно, тільки розгорнуті гілки')
;
--<<