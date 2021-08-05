create database file_db;
create table if not exists upload
(
    id integer not null
        constraint file_pk
            primary key,
    file_name varchar not null,
    emails varchar not null
);

alter table upload owner to postgres;

create unique index if not exists file_file_name_uindex
    on upload (file_name);
