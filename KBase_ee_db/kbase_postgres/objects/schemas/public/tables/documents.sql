-- Table: public.documents

-- DROP TABLE IF EXISTS public.documents;

CREATE TABLE IF NOT EXISTS public.documents
(
    id bigint NOT NULL DEFAULT nextval('seq_documents'::regclass),
    section_id bigint NOT NULL,
    text text COLLATE pg_catalog."default",
    type integer,
    date_created timestamp without time zone DEFAULT now(),
    date_modified timestamp without time zone DEFAULT now(),
    user_created character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    user_modified character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    CONSTRAINT pk_documents_id PRIMARY KEY (id),
    CONSTRAINT unique_documents_section_id UNIQUE (section_id),
    CONSTRAINT fk_documents_sectionid FOREIGN KEY (section_id)
        REFERENCES kbase.sections (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.documents
    OWNER to kbase;

REVOKE ALL ON TABLE public.documents FROM kbase_user;

GRANT ALL ON TABLE public.documents TO kbase;

GRANT DELETE, UPDATE, INSERT, SELECT ON TABLE public.documents TO kbase_user;

COMMENT ON TABLE public.documents
    IS 'Скомпилированные документы.';

COMMENT ON COLUMN public.documents.type
    IS '1 - документы кешируются на локальном диске; 2 - кешируются в БД';
-- Index: ind_documents_section_id

-- DROP INDEX IF EXISTS public.ind_documents_section_id;

CREATE INDEX IF NOT EXISTS ind_documents_section_id
    ON public.documents USING btree
    (section_id ASC NULLS LAST)
    TABLESPACE pg_default;