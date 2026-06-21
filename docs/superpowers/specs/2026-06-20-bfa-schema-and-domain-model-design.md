# BFA schema and domain model design

## Goal

Create the complete BFA Digital PostgreSQL schema in local Supabase from the approved SQL model, and provide the initial Spring Boot JPA entity model that maps it without allowing Hibernate to alter it.

## Scope

The initial database contains every PostgreSQL extension, enum, table, constraint, index, compatibility view and base catalog record defined in `script_bfa_digital_extendido_normalizado.sql`. The migration is non-destructive and is intended to run once through Supabase CLI.

The Spring Boot work supplies database connectivity, schema validation and initial persistent entities. It does not include controllers, repositories, services, authentication endpoints, UI, report generation, or scoring execution.

## Decisions

- Internal authentication remains in the `usuario` table and role/permission join tables. Supabase Auth is not part of this scope.
- The database is the source of truth. Supabase migrations create and evolve the schema; Hibernate uses `ddl-auto=validate`.
- A single baseline migration creates the complete schema without `DROP` statements. The optional cleanup block from the supplied script is omitted.
- The SQL source is kept semantically equivalent. Only migration-safety corrections are allowed, including creation ordering and missing cleanup references that do not affect the resulting schema.
- Java persistence types preserve PostgreSQL semantics: UUID, numeric precision, timestamp, JSONB and database enums.
- Entities are grouped by domain: `security`, `academic`, `instrument`, `assessment`, `scoring`, `reporting` and `audit`.
- Join tables with only foreign keys use explicit embedded composite identifiers. Tables containing business attributes remain first-class entities.

## Architecture

```text
Supabase CLI migration
  -> PostgreSQL schema and seed catalogs
  -> Spring datasource
  -> Hibernate validation
  -> JPA entities grouped by domain
```

The migration enables `pgcrypto` and `btree_gist`, defines all enum types before dependent tables, and inserts the approved roles, permissions, sex catalog and grading strategies after their tables exist. It will live in `supabase/migrations` and execute through local Supabase lifecycle commands.

Spring Boot connects using the local Supabase PostgreSQL credentials supplied through environment variables. Hibernate verifies that mappings match the migration but never produces DDL. Entity relationships use LAZY loading by default to avoid accidental graph loading in later API work.

## Domain boundaries

- **security**: users, roles, permissions and their associations.
- **academic**: demographic and academic catalogs plus participants.
- **instrument**: strategies, tests, versions, subtests, dimensions, items, multimedia, options, rules, answer keys, score matrices, norms and rubrics.
- **assessment**: sessions, subtest sessions, assignments, attempts and raw answers.
- **scoring**: aggregate results, dimension results, per-answer scoring and manual/rubric reviews.
- **reporting**: generated report metadata and the `detalle_resultado` compatibility view.
- **audit**: immutable audit-event records.

## Integrity and error handling

Database constraints remain authoritative for foreign keys, uniqueness, state transitions, value ranges, approval requirements and non-overlapping norm ranges. Startup must fail if the schema is absent or entity mappings diverge, rather than silently creating an inconsistent schema. Sensitive values such as password hashes and access-token hashes remain persisted but are excluded from diagnostic string output.

## Verification

1. Reset local Supabase and apply the baseline migration.
2. Confirm required extensions, tables, enum types, indexes, view and base data exist.
3. Run a Spring Boot integration test against local PostgreSQL with `ddl-auto=validate`.
4. Confirm the context starts and Hibernate reports no mapping validation errors.
