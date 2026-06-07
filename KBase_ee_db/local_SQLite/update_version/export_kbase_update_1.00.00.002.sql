-->>
-- ######## update table Settings for new version ##################################
update settings 
   set value = '1.00.00.002', 
       descr = 'begin',
       date_modified = '2025-03-14 15:45:00'
 where alias = 'VERSION_DB_NUMBER'
;
update settings 
   set value = '2025-03-14 15:46', 
       descr = '',
       date_modified = '2025-03-14 15:45:00'
 where alias = 'VERSION_DB_END_DATE'
;
--######################## sequences
-- 
CREATE TABLE sequences (
	id NUMERIC NOT NULL,
	table_name text(50),
	next_value numeric,
	step numeric,
	date_created text(19),
	CONSTRAINT pk_log PRIMARY KEY (id)
);
-- 
insert into sequences (id, table_name, next_value, step, date_created) values
	(1, 'settings', 4, 1, '2025-03-14 15:51')
;





--<<