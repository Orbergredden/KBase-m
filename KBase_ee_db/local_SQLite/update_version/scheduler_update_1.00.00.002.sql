-->>
-- ######## update table Settings for new version ##################################
update settings 
   set value = '1.00.00.002', 
       descr = 'Tasks',
       date_modified = '2024-06-07 14:50'
 where alias = 'VERSION_DB_NUMBER'
;
update settings 
   set value = '2024-06-07 14:50', 
       descr = '',
       date_modified = '2024-06-07 14:50'
 where alias = 'VERSION_DB_END_DATE'
;
--######## directories
-- 
CREATE TABLE directories (
	id NUMERIC NOT NULL,
	parent_id NUMERIC,
	name TEXT(50) NOT NULL,
	descr TEXT(200),
	date_created TEXT,
	date_modified TEXT,
	CONSTRAINT directories_pk PRIMARY KEY (id)
);
--######## tasks tables
-- 
create table tasks_type (
	id numeric not null,
	name TEXT(50) NOT null,
	descr TEXT(200),
	date_created TEXT,
	date_modified TEXT,
	CONSTRAINT tasks_type_pk PRIMARY KEY (id)
);
-- 
insert into tasks_type (id, name, date_created, date_modified)
values (1, 'Консольний додаток', '2024-07-10 17:30', '2024-07-10 17:30')
;

-- 
create table tasks (
	id numeric not null,
	dir_id numeric not null,
	name TEXT(50) NOT NULL,
	descr TEXT(200),
	date_created TEXT,
	date_modified TEXT,
	CONSTRAINT tasks_pk PRIMARY KEY (id)
);

--
create table tasks_control (
	id numeric not null,
	task_id numeric not null,
	type_id numeric not null,
	state numeric not null,
	start_hour numeric not null,
	start_minute numeric not null,
	interval numeric not null,
	date_created TEXT,
	date_modified TEXT,
	CONSTRAINT tasks_control_pk PRIMARY KEY (id)
);

--
create table tasks_control_app_console (
	id numeric not null,
	control_id numeric not null,
	app_path TEXT(1000) NOT NULL,
	is_home_dir  numeric not null,
	date_created TEXT,
	date_modified TEXT,
	CONSTRAINT tasks_control_app_console_pk PRIMARY KEY (id)
);






--<<