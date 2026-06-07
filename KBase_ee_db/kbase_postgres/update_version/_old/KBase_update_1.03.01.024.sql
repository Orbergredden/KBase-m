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

-- ######## update table Settings for new version ##################################
update settings 
   set value = '1.03.01.024', 
       descr = 'fix',
       date_modified = now(),
       user_modified = "current_user"()
 where alias = 'VERSION_DB_NUMBER'
;
update settings 
   set value = '22.08.2023 16:08', 
       descr = '',
       date_modified = now(),
       user_modified = "current_user"()
 where alias = 'VERSION_DB_END_DATE'
;
--######## alter table template_files #######################################

ALTER TABLE template_files ADD COLUMN new_file_name VARCHAR(100);
UPDATE template_files SET new_file_name = file_name;
ALTER TABLE template_files DROP COLUMN file_name;
ALTER TABLE template_files RENAME COLUMN new_file_name TO file_name;

--<<