-- liquibase formatted sql
-- changeset argus:3
alter table day
    add column bloody_rating smallint;

alter table day
    add column pimple_rating smallint;

alter table day
    drop column is_bloody;

alter table day
    drop column is_pimple;

