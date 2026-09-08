--
-- PostgreSQL KBase update
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

-- ######## перевіряємо щоб БД була попередньої версії ############################
do $$
<<check_version>>
declare
	v_version_old varchar(50) := '2.03.00.031';
	v_version     varchar(50);
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

-- ######## update table Settings for new version ############################
update settings
	set value = '2.04.00.032',
		descr = 'clear db',
		date_modified = now(),
		user_modified = "current_user"()
where alias = 'VERSION_DB_NUMBER'
;
update settings
	set value = '07.09.2026',
		descr = '',
		date_modified = now(),
		user_modified = "current_user"()
where alias = 'VERSION_DB_END_DATE'
;

--######## create table access_type ##################################
CREATE TABLE kbase.access_type
(
	id bigint NOT NULL,
	name character varying(100),
	descr character varying(200),
	CONSTRAINT pk_access_type_id PRIMARY KEY (id)
);

ALTER TABLE IF EXISTS kbase.access_type OWNER to kbase;
GRANT ALL ON TABLE kbase.access_type TO kbase;
REVOKE ALL ON TABLE kbase.access_type FROM kbase_user;
GRANT SELECT ON TABLE kbase.access_type TO kbase_user;

insert into kbase.access_type (id, name, descr)
	values (1, 'clear db', '')
;

--######## create table access_user ##################################
CREATE TABLE kbase.access_user
(
	access_type_id bigint NOT NULL,
	user_name character varying(30) COLLATE pg_catalog."default" NOT NULL,
	CONSTRAINT fk_access_user_access_type_id FOREIGN KEY (access_type_id)
		REFERENCES kbase.access_type (id) MATCH SIMPLE
		ON UPDATE CASCADE
		ON DELETE CASCADE
)
TABLESPACE pg_default;

ALTER TABLE IF EXISTS kbase.access_user OWNER to kbase;
GRANT ALL ON TABLE kbase.access_user TO kbase;
REVOKE ALL ON TABLE kbase.access_user FROM kbase_user;
GRANT SELECT ON TABLE kbase.access_user TO kbase_user;

insert into kbase.access_user (access_type_id, user_name)
	values (1, 'kbase_admin')
;
--<<
