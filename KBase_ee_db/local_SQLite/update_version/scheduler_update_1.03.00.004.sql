-->>
-- ######## update table Settings for new version ##################################
update settings 
   set value = '1.03.00.004', 
       descr = 'new type for Tasks : Plugin',
       date_modified = '2025-10-22 17:37'
 where alias = 'VERSION_DB_NUMBER'
;
update settings 
   set value = '2025-10-22 17:37:00', 
       descr = '',
       date_modified = '2025-10-22 17:37'
 where alias = 'VERSION_DB_END_DATE'
;
--######## update tasks_type tables
-- 
insert into tasks_type (id, name, date_created, date_modified)
values (5, 'JAR-plugin', '2025-10-22 17:45', '2025-10-22 17:45')
;
--<<

