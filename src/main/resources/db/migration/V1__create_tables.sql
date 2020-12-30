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
    organizations organization[] default '{}'::organization[] not null,
    hashed_password text
);
create index account_organization_index
	on account (organizations);

create table worker
(
    id bigserial not null
        constraint worker_pk
            primary key,
    organization organization not null,
    friendly_name text,
    hashed_password text
);
create index worker_organization_index
	on worker (organization);
create index worker_friendly_name_index
	on worker (friendly_name);