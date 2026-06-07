-- Table: kbase.sections

-- DROP TABLE IF EXISTS kbase.sections;

CREATE TABLE IF NOT EXISTS kbase.sections
(
    id bigint NOT NULL DEFAULT nextval('kbase.seq_sections'::regclass),
    parent_id bigint,
    name character varying(255) COLLATE pg_catalog."default",
    descr character varying(255) COLLATE pg_catalog."default",
    icon_id bigint,
    template_main character varying(50) COLLATE pg_catalog."default",
    icon_id_root bigint,
    icon_id_def bigint,
    theme_id bigint,
    cache_type integer,
    date_created timestamp without time zone DEFAULT now(),
    date_modified timestamp without time zone DEFAULT now(),
    user_created character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    user_modified character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    date_modified_info timestamp without time zone,
    CONSTRAINT pk_sections_id PRIMARY KEY (id),
    CONSTRAINT fk_sections_icon_id FOREIGN KEY (icon_id)
        REFERENCES public.icons (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS kbase.sections
    OWNER to kbase;

REVOKE ALL ON TABLE kbase.sections FROM kbase_user;
REVOKE ALL ON TABLE kbase.sections FROM kbase_view;

GRANT ALL ON TABLE kbase.sections TO kbase;

GRANT SELECT ON TABLE kbase.sections TO kbase_user;

GRANT SELECT ON TABLE kbase.sections TO kbase_view;

COMMENT ON TABLE kbase.sections
    IS 'Разделы Базы Знаний в виде дерева';

COMMENT ON COLUMN kbase.sections.icon_id
    IS 'Пиктограмма раздела';

COMMENT ON COLUMN kbase.sections.template_main
    IS 'Тег стилю головного шаблона';

COMMENT ON COLUMN kbase.sections.icon_id_root
    IS 'Корневая иконка поддерева для выбора иконок для данного и дочерних разделов';

COMMENT ON COLUMN kbase.sections.icon_id_def
    IS 'Иконка по умолчанию для данного и дочерних разделов';

COMMENT ON COLUMN kbase.sections.theme_id
    IS 'Тема шаблонов для показа документа.';

COMMENT ON COLUMN kbase.sections.cache_type
    IS 'Тип кеширования : 1 - документы кешируются на локальном диске; 2 - кешируются в БД; 3 - кешируются на диске только обязательные файлы';

COMMENT ON COLUMN kbase.sections.user_created
    IS 'Тот, кто создал запись';

COMMENT ON COLUMN kbase.sections.user_modified
    IS 'Тот, кто вносил последние изменения в запись';

COMMENT ON COLUMN kbase.sections.date_modified_info
    IS 'Последнее изменение инфоблоков';
-- Index: ind_sections_parent_id

-- DROP INDEX IF EXISTS kbase.ind_sections_parent_id;

CREATE INDEX IF NOT EXISTS ind_sections_parent_id
    ON kbase.sections USING btree
    (parent_id ASC NULLS LAST)
    TABLESPACE pg_default;