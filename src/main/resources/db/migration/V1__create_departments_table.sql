CREATE TABLE departments (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,

    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_departments
        PRIMARY KEY (id),

    CONSTRAINT uq_departments_code
        UNIQUE (code),

    CONSTRAINT ck_departments_code_format
        CHECK (code ~ '^[A-Z][A-Z0-9_-]{0,49}$'),

    CONSTRAINT ck_departments_name_not_blank
        CHECK (name = btrim(name) AND name <> ''),

    CONSTRAINT ck_departments_timestamp_order
        CHECK (updated_at >= created_at),

    CONSTRAINT ck_departments_deleted_state
        CHECK (
            (
                is_deleted = FALSE
                AND deleted_at IS NULL
            )
            OR
            (
                is_deleted = TRUE
                AND deleted_at IS NOT NULL
            )
        ),

    CONSTRAINT ck_departments_deleted_not_active
        CHECK (is_deleted = FALSE OR active = FALSE),

    CONSTRAINT ck_departments_deleted_at_order
        CHECK (
            deleted_at IS NULL
            OR (
                deleted_at >= created_at
                AND updated_at >= deleted_at
            )
        )
);