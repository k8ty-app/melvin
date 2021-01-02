create table organization
(
    org_id text not null
        constraint organization_pk
            primary key
);

create table account
(
    id text not null
        constraint account_pk
            primary key,
    organizations text[] default '{}' not null,
    hashed_password text
);
create index account_organization_index
	on account (organizations);

create table worker
(
    id uuid default gen_random_uuid()
        constraint worker_pk
            primary key,
    organization text,
    name text,
    secret text
);
create index worker_organization_index
	on worker (organization);
create index worker_name_index
	on worker (name);