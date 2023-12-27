-- liquibase formatted sql
-- changeset argus:7
alter table day
    drop constraint day_date_key;

alter table day
    add constraint check_bloody_rating
        check (bloody_rating < 11 AND bloody_rating > -1);

alter table day
    add constraint pimple_face_rating
        check (pimple_face_rating < 11 AND pimple_face_rating > -1);

alter table day
    add constraint pimple_booty_rating
        check (pimple_booty_rating < 11 AND pimple_booty_rating > -1);


