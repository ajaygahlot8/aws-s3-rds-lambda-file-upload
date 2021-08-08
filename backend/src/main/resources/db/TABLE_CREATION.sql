create database filedetail_db;
create table if not exists filedetail
(
    id integer not null
        constraint file_pk
            primary key,
    file_name varchar not null,
    emails varchar not null
);

alter table filedetail owner to postgres;

create unique index if not exists file_file_name_uindex
    on filedetail (file_name);
