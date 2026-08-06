-->>
-- ######## update table Settings for new version ##################################
update settings
	set value = '2.03.00.003',
		descr = 'Favorite',
		date_modified = '2026-06-12 15:34:00'
where alias = 'VERSION_DB_NUMBER'
;
update settings
	set value = '2026-06-12 15:34:00',
		descr = '',
		date_modified = '2026-06-12 15:34:00'
where alias = 'VERSION_DB_END_DATE'
;
--######## create table sections_favorite ##################################
insert into sequences (id, sequence_name, next_value, step, date_created, date_modified)
	values (18, 'seq_sections_favorite', 1, 1, '2026-06-12 15:42:00', '2026-06-12 15:42:00')
;
--
create table sections_favorite (
	id numeric not null,
	parent_id numeric,
	section_id numeric not null,
	user TEXT(50) not null,
	date_created TEXT,
	CONSTRAINT sections_favorite_pk PRIMARY KEY (id)
);

CREATE UNIQUE INDEX ind_sections_favorite_user_section ON sections_favorite ("user", section_id);
CREATE INDEX ind_sections_favorite_user_parent ON sections_favorite ("user", parent_id);

--<<

