CREATE TABLE artifactref
(
    id uuid default gen_random_uuid()
        constraint artifactref_pk
            primary key,
    org_id text not null ,
    package_id text not null,
    version text not null,
    file_name text not null
);
create index artifactref_org_id_index
    on artifactref (org_id);
create index artifactref_package_id_index
    on artifactref (package_id);