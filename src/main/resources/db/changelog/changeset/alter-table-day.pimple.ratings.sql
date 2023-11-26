-- liquibase formatted sql
-- changeset argus:5
alter table day
    rename column pimple_rating to pimple_face_rating;

alter table day
    add column pimple_booty_rating smallint;

alter table day
    alter column pimple_face_rating set default 0;

alter table day
    alter column pimple_booty_rating set default 0;

