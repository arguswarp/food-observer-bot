-- liquibase formatted sql
-- changeset argus:6
alter table bot_user
    add column today_mode bool not null default true;

alter table day
    alter column bloody_rating set not null;

alter table day
    alter column pimple_face_rating set not null;

alter table day
    alter column pimple_booty_rating set not null;


