create database file_upload_db;
create table if not exists file_upload
(
    id integer not null
        constraint file_pk
            primary key,
    name_file varchar not null,
    emails varchar not null
);

alter table file_upload owner to postgres;

create unique index if not exists file_file_name_uindex
    on file_upload (name_file);
