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
	v_version_old varchar(50) := '2.02.01.030';
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
	set value = '2.03.00.031',
		descr = 'find info',
		date_modified = now(),
		user_modified = "current_user"()
where alias = 'VERSION_DB_NUMBER'
;
update settings
	set value = '12.06.2026',
		descr = '',
		date_modified = now(),
		user_modified = "current_user"()
where alias = 'VERSION_DB_END_DATE'
;
--######## create table sections_favorite ##################################
CREATE SEQUENCE kbase.seq_sections_favorite
	INCREMENT 1
	START 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	CACHE 1;
ALTER SEQUENCE kbase.seq_sections_favorite OWNER TO kbase;
GRANT ALL ON SEQUENCE kbase.seq_sections_favorite TO kbase;
GRANT ALL ON SEQUENCE kbase.seq_sections_favorite TO kbase_user;

CREATE TABLE kbase.sections_favorite
(
	id bigint NOT NULL DEFAULT nextval('seq_sections_favorite'::regclass),
	parent_id bigint,
	section_id bigint NOT NULL,
	"user" character varying(30) COLLATE pg_catalog."default" NOT NULL DEFAULT "current_user"(),
	date_created timestamp without time zone DEFAULT now(),
	CONSTRAINT pk_sections_favorite_id PRIMARY KEY (id),
	CONSTRAINT fk_sections_favorite_section_id FOREIGN KEY (section_id)
		REFERENCES kbase.sections (id) MATCH SIMPLE
		ON UPDATE NO ACTION
		ON DELETE NO ACTION
)
TABLESPACE pg_default;

ALTER TABLE IF EXISTS kbase.sections_favorite OWNER to kbase;
GRANT ALL ON TABLE kbase.sections_favorite TO kbase;
REVOKE ALL ON TABLE kbase.sections_favorite FROM kbase_user;
GRANT DELETE, UPDATE, INSERT, SELECT ON TABLE kbase.sections_favorite TO kbase_user;

CREATE UNIQUE INDEX ind_sections_favorite_user_section ON sections_favorite ("user", section_id);
CREATE INDEX ind_sections_favorite_user_parent ON sections_favorite ("user", parent_id);

--######## function kbase.find_info () ##################################
-- DROP FUNCTION IF EXISTS kbase.find_info(boolean, boolean, boolean, boolean, boolean, boolean, character varying, boolean);

CREATE OR REPLACE FUNCTION kbase.find_info(
	objsection boolean,
	objinfoheader boolean,
	objdictionary boolean,
	objtext boolean,
	objimage boolean,
	objfile boolean,
	search_text character varying,
	text_ignore_registr boolean)
RETURNS TABLE(
	texttype character varying,
	sectionid bigint,
	sectionname character varying,
	sectionpathname character varying,
	infoid bigint,
	infoname character varying,
	text character varying,
	date_created timestamp without time zone,
	date_modified timestamp without time zone,
	user_created character varying,
	user_modified character varying)
LANGUAGE 'plpgsql'
COST 100
VOLATILE SECURITY DEFINER PARALLEL UNSAFE
ROWS 1000

AS $BODY$

-- шукаємо інформацію по різних об'єктах
DECLARE
	l_i		record;
	r		RECORD;
	v_sql		text;

	v_search_text varchar := '%'||search_text||'%';
	q_search_text varchar := '';	-- якщо вказаний текст пошуку, то додаємо в умови
BEGIN
	--################################################ create temp tables
	CREATE TEMP TABLE temp_find_set (
	table_name	VARCHAR,
	field_name	VARCHAR,
	infotypeid	bigint
	) ON COMMIT DROP;

	CREATE TEMP TABLE temp_find_result (
		t_texttype	VARCHAR,
		t_sectionid	BIGINT,
		t_sectionname	VARCHAR,
		t_sectionpathname	VARCHAR,
		t_infoid	BIGINT,
		t_infoname	VARCHAR,
		t_text		VARCHAR,
		t_date_created	TIMESTAMP WITHOUT TIME ZONE,
		t_date_modified	TIMESTAMP WITHOUT TIME ZONE,
		t_user_created	VARCHAR,
		t_user_modified	VARCHAR
	) ON COMMIT DROP;

	--################################################ prepare temp_find_set
	if objSection then
		INSERT INTO temp_find_set (table_name, field_name)
			VALUES ('sections', 'name'),
			       ('sections', 'descr')
		;
	end if;

	if objInfoHeader then
		INSERT INTO temp_find_set (table_name, field_name)
			VALUES ('info', 'name'),
			       ('info', 'descr')
		;
	end if;
	if objText then
		INSERT INTO temp_find_set (table_name, field_name, infotypeid)
			VALUES ('info_text', 'title', 1),
			       ('info_text', 'text', 1)
		;
	end if;
	if objImage then
		INSERT INTO temp_find_set (table_name, field_name, infotypeid)
			VALUES ('info_image', 'title', 2),
			       ('info_image', 'descr', 2),
			       ('info_image', 'text', 2)
		;
	end if;
	if objFile then
		INSERT INTO temp_find_set (table_name, field_name, infotypeid)
			VALUES ('info_file', 'title', 3),
			       ('info_file', 'file_name', 3),
			       ('info_file', 'descr', 3),
			       ('info_file', 'text', 3)
		;
	end if;

	if objDictionary then
		INSERT INTO temp_find_set (table_name, field_name)
			VALUES ('dict', 'name'),
			       ('dict', 'value'),
			       ('dict', 'descr')
		;
	end if;

	--################################################ get info
	for l_i in select table_name, field_name, infotypeid from temp_find_set
	loop
		------- похідні з умовами запиту
		if length(search_text) > 0 then
			if text_ignore_registr then
				q_search_text := ' and upper(t.'|| l_i.field_name ||') like upper($3) ';
			else
				q_search_text := ' and t.'|| l_i.field_name ||' like $3 ';
			end if;
		end if;

		------- варіанти запитів
		if l_i.table_name = 'sections' then
			v_sql := format($f$
				INSERT INTO temp_find_result (
					t_texttype, t_sectionid, t_sectionname, t_sectionpathname, t_infoid, t_infoname, t_text,
					t_date_created, t_date_modified, t_user_created, t_user_modified
				)
				select $1||','||$2 as TextType
					,t.id as sectionid
					,t.name as sectionName
					,section_get_pathname(t.id,' | ') as sectionPathName
					,null as InfoHeaderId
					,null as InfoHeaderName
					,t.%I as text
					,t.date_created
					,t.date_modified
					,t.user_created
					,t.user_modified
				from %I t
				where 1=1
					%s
			$f$,
			l_i.field_name,
			l_i.table_name,
			q_search_text
			);
		end if;

		if l_i.table_name in ('info') then
			v_sql := format($f$
				INSERT INTO temp_find_result (
					t_texttype, t_sectionid, t_sectionname, t_sectionpathname, t_infoid, t_infoname, t_text,
					t_date_created, t_date_modified, t_user_created, t_user_modified
				)
				select $1||','||$2 as TextType
					,s.id as sectionid
					,s.name as sectionName
					,section_get_pathname(s.id,' | ') as sectionPathName
					,t.id as InfoHeaderId
					,t.name as InfoHeaderName
					,t.%I as text
					,t.date_created
					,t.date_modified
					,t.user_created
					,t.user_modified
				from %I t
				join sections s on s.id = t.sectionid
				where 1=1
					%s
			$f$,
			l_i.field_name,
			l_i.table_name,
			q_search_text
			);
		end if;

		if l_i.table_name in ('info_text','info_image','info_file') then
			v_sql := format($f$
				INSERT INTO temp_find_result (
					t_texttype, t_sectionid, t_sectionname, t_sectionpathname, t_infoid, t_infoname, t_text,
					t_date_created, t_date_modified, t_user_created, t_user_modified
				)
				select $1||','||$2 as TextType
					,s.id as sectionid
					,s.name as sectionName
					,section_get_pathname(s.id,' | ') as sectionPathName
					,i.id as InfoHeaderId
					,i.name as InfoHeaderName
					,t.%I as text
					,i.date_created
					,i.date_modified
					,i.user_created
					,i.user_modified
				from %I t
				join info i	on i.infoid = t.id
						and i.infotypeid = %s
				join sections s on s.id = i.sectionid
				where 1=1
					%s
			$f$,
			l_i.field_name,
			l_i.table_name,
			l_i.infotypeid,
			q_search_text
			);
		end if;

		if l_i.table_name in ('dict') then
			v_sql := format($f$
				INSERT INTO temp_find_result (
					t_texttype, t_sectionid, t_sectionname, t_sectionpathname, t_infoid, t_infoname, t_text,
					t_date_created, t_date_modified, t_user_created, t_user_modified
				)
				select $1||','||$2 as TextType
					,s.id as sectionid
					,s.name as sectionName
					,section_get_pathname(s.id,' | ') as sectionPathName
					,t.id as InfoHeaderId
					,t.name as InfoHeaderName
					,t.%I as text
					,t.date_created
					,t.date_modified
					,t.user_created
					,t.user_modified
				from %I t
				join sections s on s.id = t.section_id
				where 1=1
					%s
			$f$,
			l_i.field_name,
			l_i.table_name,
			q_search_text
			);
		end if;

		------- виконання запиту
		if (length(search_text) > 0) or
		   ((length(search_text) = 0) and
		    ((l_i.table_name = 'sections' and l_i.field_name = 'name') or
		     (l_i.table_name = 'info' and l_i.field_name = 'name') or
		     (l_i.table_name = 'info_text' and l_i.field_name = 'title') or
		     (l_i.table_name = 'info_image' and l_i.field_name = 'title') or
		     (l_i.table_name = 'info_file' and l_i.field_name = 'title') or
		     (l_i.table_name = 'dict' and l_i.field_name = 'name')
		   )) then

			EXECUTE v_sql USING l_i.table_name, l_i.field_name, v_search_text;
		end if;
	end loop;

	--raise notice '%', v_sql;

	--################################################ return info
	FOR r IN
		SELECT
			t.t_texttype,
			t.t_sectionid,
			t.t_sectionname,
			t.t_sectionpathname,
			t.t_infoid,
			t.t_infoname,
			t.t_text,
			t.t_date_created,
			t.t_date_modified,
			t.t_user_created,
			t.t_user_modified
		FROM temp_find_result t
		order by t.t_date_created desc
	LOOP
		-- Ініціалізація вихідних змінних із запису таблиці
		texttype	:= r.t_texttype;
		sectionid	:= r.t_sectionid;
		sectionname	:= r.t_sectionname;
		sectionpathname	:= r.t_sectionpathname;
		infoid		:= r.t_infoid;
		infoname	:= r.t_infoname;
		text		:= r.t_text;
		date_created	:= r.t_date_created;
		date_modified	:= r.t_date_modified;
		user_created	:= r.t_user_created;
		user_modified	:= r.t_user_modified;

		-- Повертаємо поточний рядок
		RETURN NEXT;
	END LOOP;

END;
$BODY$;

ALTER FUNCTION kbase.find_info(boolean, boolean, boolean, boolean, boolean, boolean, character varying, boolean)
	OWNER TO kbase;

COMMENT ON FUNCTION kbase.find_info(boolean, boolean, boolean, boolean, boolean, boolean, character varying, boolean)
	IS 'шукаємо інформацію по різних об''єктах';

--<<