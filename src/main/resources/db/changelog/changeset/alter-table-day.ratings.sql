-- liquibase formatted sql
-- changeset argus:3
alter table day
    rename column is_bloody to bloody_rating;

alter table day
    alter column bloody_rating type smallint using bloody_rating::smallint;

alter table day
    rename column is_pimple to pimple_rating;

alter table day
    alter column pimple_rating type smallint using pimple_rating::smallint;