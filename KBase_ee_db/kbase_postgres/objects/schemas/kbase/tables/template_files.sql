-- Table: kbase.template_files

-- DROP TABLE IF EXISTS kbase.template_files;

CREATE TABLE IF NOT EXISTS kbase.template_files
(
    id bigint NOT NULL DEFAULT nextval('kbase.seq_template_files'::regclass),
    parent_id bigint,
    theme_id bigint NOT NULL,
    type integer NOT NULL DEFAULT 0,
    file_type integer NOT NULL DEFAULT 0,
    descr character varying(25) COLLATE pg_catalog."default",
    body text COLLATE pg_catalog."default",
    body_bin bytea,
    date_created timestamp without time zone DEFAULT now(),
    date_modified timestamp without time zone DEFAULT now(),
    user_created character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    user_modified character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    file_name character varying(100) COLLATE pg_catalog."default",
    CONSTRAINT pk_template_files_id PRIMARY KEY (id),
    CONSTRAINT fk_template_files_theme_id FOREIGN KEY (theme_id)
        REFERENCES kbase.template_themes (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS kbase.template_files
    OWNER to kbase;

REVOKE ALL ON TABLE kbase.template_files FROM kbase_user;

GRANT ALL ON TABLE kbase.template_files TO kbase;

GRANT SELECT ON TABLE kbase.template_files TO kbase_user;

COMMENT ON TABLE kbase.template_files
    IS 'Необходимые файлы для отображения документов';

COMMENT ON COLUMN kbase.template_files.parent_id
    IS 'родительская директория';

COMMENT ON COLUMN kbase.template_files.type
    IS '0-файл, 1-директория ; (для необязательных файлов) 10 - файл, 11 - директория';

COMMENT ON COLUMN kbase.template_files.file_type
    IS 'Тип файла : 1 - текстовый ; 2 - картинка ; 3 - бинарный';

COMMENT ON COLUMN kbase.template_files.body_bin
    IS 'содержимое бинарных файлов';

COMMENT ON COLUMN kbase.template_files.user_created
    IS 'Тот, кто создал запись';

COMMENT ON COLUMN kbase.template_files.user_modified
    IS 'Тот, кто вносил последние изменения в запись';