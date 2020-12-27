create table organizations
(
    org_id text not null
        constraint organizations_pk
            primary key
);

create table users
(
    id text not null
        constraint users_pk
            primary key,
    organizations organizations[] default '{}'::organizations[] not null,
    hashed_password text
);
create index users_organizations_index
	on users (organizations);

create table workers
(
    id bigserial not null
        constraint workers_pk
            primary key,
    organization organizations not null,
    friendly_name text,
    hashed_password text,
);
create index workers_organizations_index
	on workers (organization);
create index workers_friendly_name_index
	on workers (friendly_name);