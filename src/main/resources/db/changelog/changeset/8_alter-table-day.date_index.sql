-- liquibase formatted sql
-- changeset argus:8
create index day_date_index
    on day (date);

