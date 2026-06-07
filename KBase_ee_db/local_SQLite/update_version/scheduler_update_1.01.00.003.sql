-->>
-- ######## update table Settings for new version ##################################
update settings 
   set value = '1.01.00.003', 
       descr = 'new types for Tasks',
       date_modified = '2025-08-25 16:48'
 where alias = 'VERSION_DB_NUMBER'
;
update settings 
   set value = '2025-08-25 16:48', 
       descr = '',
       date_modified = '2025-08-25 16:48'
 where alias = 'VERSION_DB_END_DATE'
;

--######## tasks
-- 
alter table tasks add column msg_start numeric default 0;
alter table tasks add column msg_finish numeric default 0;

--######## update tasks_type tables
-- 
insert into tasks_type (id, name, date_created, date_modified)
values (2, '(prg) Збереження стану програми', '2025-08-25 17:24', '2025-08-25 17:24')
;
insert into tasks_type (id, name, date_created, date_modified)
values (3, '(prg) Збереження усіх редагувань', '2025-08-25 17:24', '2025-08-25 17:24')
;
insert into tasks_type (id, name, date_created, date_modified)
values (4, '(msgBase) Показ повідомлень', '2025-08-25 17:24', '2025-08-25 17:24')
;

--######## table task_control_specific
-- 
create table tasks_control_specific (
	id numeric not null,
	control_id numeric not null,
	field_type TEXT(50) NOT NULL,
	value_num numeric,
	value_str text,
	date_created TEXT,
	date_modified TEXT,
	CONSTRAINT task_control_specific_pk PRIMARY KEY (id)
);
--<<

