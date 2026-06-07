-- SEQUENCE: kbase.seq_settings

-- DROP SEQUENCE IF EXISTS kbase.seq_settings;

CREATE SEQUENCE IF NOT EXISTS kbase.seq_settings
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE kbase.seq_settings
    OWNER TO kbase;