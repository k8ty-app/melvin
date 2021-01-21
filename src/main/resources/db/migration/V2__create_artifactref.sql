CREATE TABLE artifact_ref
(
    id uuid default gen_random_uuid()
        constraint artifact_ref_pk
            primary key,
    org_id text not null ,
    package_id text not null,
    version text not null,
    file_name text not null
);
create index artifact_ref_org_id_index
    on artifact_ref (org_id);
create index artifact_ref_package_id_index
    on artifact_ref (package_id);