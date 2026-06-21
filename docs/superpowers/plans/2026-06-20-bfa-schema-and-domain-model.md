# BFA Schema and Domain Model Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Provision the approved BFA Digital PostgreSQL schema in local Supabase and add validated Spring Boot JPA mappings for its persistence model.

**Architecture:** A single non-destructive Supabase baseline migration creates PostgreSQL types, extensions, schema, indexes, view and catalogs. Spring Boot uses the PostgreSQL driver and Hibernate schema validation only; entities are organized by domain.

**Tech Stack:** Supabase CLI 2.75, PostgreSQL, Spring Boot 4.1, Spring Data JPA, Hibernate ORM, JUnit 5, Java 21.

---

## File structure

- `supabase/migrations/<timestamp>_create_bfa_schema.sql`: schema, view and seed data.
- `psychoform/pom.xml`: PostgreSQL runtime driver.
- `psychoform/src/main/resources/application.properties`: environment-based datasource plus Hibernate validation.
- `psychoform/src/main/java/com/uam/psychoform/{security,academic,instrument,assessment,scoring,reporting,audit}/entity`: JPA entities grouped by domain.
- `psychoform/src/test/java/com/uam/psychoform/SchemaValidationIntegrationTest.java`: validation against local Supabase.

### Task 1: Create the Supabase baseline migration

**Files:**
- Create: `supabase/migrations/<timestamp>_create_bfa_schema.sql`
- Test: local Supabase migration and PostgreSQL catalog queries

- [ ] **Step 1: Write the failing schema test**

Create `supabase/tests/001_bfa_schema.sql` with pgTAP assertions that the `usuario` table, `estado_general` type and `detalle_resultado` view exist, and that seed counts equal 4 roles, 13 permissions and 7 strategies. Run `supabase test db`. Expected: FAIL because no migration has created them.

- [ ] **Step 2: Add the non-destructive baseline**

Translate the supplied script in dependency order: extensions; enums; security; academic catalogs; participant; strategies; instrument structure; multimedia; scoring configuration; norms; rubrics; assessment; answers; results; reporting; audit; indexes; view; seed records. Omit every `DROP` statement. Preserve all checks, unique constraints and foreign keys. Ensure `resultado` is created before `resultado_dimension`, and enable `btree_gist` before the range exclusion constraint.

- [ ] **Step 3: Apply and verify**

Run:

```powershell
supabase start
supabase db reset
supabase test db
```

Expected: migration and pgTAP test pass, including the three seed-count assertions.

- [ ] **Step 4: Commit**

```powershell
git add supabase/migrations supabase/tests
git commit -m "feat: add BFA baseline database schema"
```

### Task 2: Configure PostgreSQL validation

**Files:**
- Modify: `psychoform/pom.xml`
- Modify: `psychoform/src/main/resources/application.properties`
- Create: `psychoform/src/test/java/com/uam/psychoform/SchemaValidationIntegrationTest.java`

- [ ] **Step 1: Write the failing context test**

```java
@SpringBootTest
class SchemaValidationIntegrationTest {
    @Test
    void validatesTheExistingBfaSchema() { }
}
```

Run with local PostgreSQL environment variables. Expected: failure because PostgreSQL runtime support and datasource configuration are absent.

- [ ] **Step 2: Add configuration**

Add `org.postgresql:postgresql` as a runtime dependency. Configure `spring.datasource.url`, user and password from `BFA_DB_URL`, `BFA_DB_USERNAME`, `BFA_DB_PASSWORD`; set `spring.jpa.hibernate.ddl-auto=validate` and disable schema initialization. Do not commit credentials.

- [ ] **Step 3: Run test**

```powershell
Set-Location psychoform
$env:BFA_DB_URL='jdbc:postgresql://127.0.0.1:54322/postgres'
$env:BFA_DB_USERNAME='postgres'
$env:BFA_DB_PASSWORD='postgres'
.\mvnw.cmd test -Dtest=SchemaValidationIntegrationTest
```

Expected: reaches the database without emitting DDL.

### Task 3: Map security and academic domains

**Files:**
- Create: `psychoform/src/main/java/com/uam/psychoform/security/entity/{Usuario,Rol,Permiso,UsuarioRolId,UsuarioRol,RolPermisoId,RolPermiso,EstadoGeneral}.java`
- Create: `psychoform/src/main/java/com/uam/psychoform/academic/entity/{CatalogoSexo,Carrera,Cohorte,GrupoAcademico,Participante}.java`
- Test: `psychoform/src/test/java/com/uam/psychoform/security/entity/UsuarioMappingTest.java`

- [ ] **Step 1: Write failing mapping tests**

Assert `Usuario` maps table `usuario`, identifier `usuario_id` with UUID, named enum `estado_general`, and `UsuarioRol` uses composite `usuario_id` plus `rol_id`.

- [ ] **Step 2: Implement minimal mappings**

Use explicit `@Table`, `@Column`, `@Enumerated(EnumType.STRING)` plus `@JdbcTypeCode(SqlTypes.NAMED_ENUM)` for PostgreSQL enum values. Use `@Embeddable` identifiers for association tables and LAZY `@ManyToOne` relationships for nullable academic references.

- [ ] **Step 3: Run and commit**

```powershell
.\mvnw.cmd test -Dtest=UsuarioMappingTest
git add psychoform
git commit -m "feat: map security and academic entities"
```

Expected: test passes.

### Task 4: Map instrument configuration

**Files:**
- Create: `psychoform/src/main/java/com/uam/psychoform/instrument/entity/{EstrategiaCalificacion,TestPsicologico,VersionTest,Subtest,DimensionResultado,Item,RecursoMultimedia,ImagenItem,OpcionItem,ImagenOpcion,ReglaCalificacion,ClaveRespuesta,OpcionPuntajeDimension,Baremo,RangoBaremo,RubricaEvaluacion,CriterioRubrica,NivelCriterioRubrica}.java`
- Create: Java enums for every corresponding PostgreSQL enum.
- Test: `psychoform/src/test/java/com/uam/psychoform/instrument/entity/InstrumentMappingTest.java`

- [ ] **Step 1: Write failing metadata tests**

Verify the version/subtest composite reference, item/option composite answer-key reference, JSONB `parametros`, numeric score fields and range-norm table mapping.

- [ ] **Step 2: Implement mappings**

Use `BigDecimal` for decimals, `LocalDate` for dates, `LocalDateTime` for timestamps and `JsonNode` with `@JdbcTypeCode(SqlTypes.JSON)` for JSONB. Preserve generated identifiers and all relationship columns.

- [ ] **Step 3: Run and commit**

```powershell
.\mvnw.cmd test -Dtest=InstrumentMappingTest
git add psychoform
git commit -m "feat: map BFA instrument entities"
```

Expected: test passes.

### Task 5: Map assessment, scoring, reporting and audit domains

**Files:**
- Create: `psychoform/src/main/java/com/uam/psychoform/assessment/entity/{SesionAplicacion,SesionSubtest,AsignacionTest,IntentoTest,IntentoSubtest,RespuestaItem,OpcionSeleccionadaRespuestaId,OpcionSeleccionadaRespuesta}.java`
- Create: `psychoform/src/main/java/com/uam/psychoform/scoring/entity/{Resultado,ResultadoDimension,CalificacionRespuesta,RevisionManualRespuesta,RevisionRubricaRespuesta}.java`
- Create: `psychoform/src/main/java/com/uam/psychoform/reporting/entity/ReporteGenerado.java`
- Create: `psychoform/src/main/java/com/uam/psychoform/audit/entity/Auditoria.java`
- Test: `psychoform/src/test/java/com/uam/psychoform/assessment/entity/AssessmentMappingTest.java`

- [ ] **Step 1: Write failing tests**

Assert selected options use the answer/item composite key, a result uniquely references an attempt, and a dimension result references both `baremo_id` and `rango_baremo_id`.

- [ ] **Step 2: Implement remaining mappings**

Map every declared foreign key and unique relation. Keep `detalle_resultado` without an entity because it is a read-only compatibility projection. Map audit JSONB values as JSON, and use LAZY relationships.

- [ ] **Step 3: Run tests and commit**

```powershell
.\mvnw.cmd test -Dtest=AssessmentMappingTest
git add psychoform
git commit -m "feat: map BFA assessment and scoring entities"
```

Expected: test passes.

### Task 6: Validate the full model

**Files:**
- Modify: `psychoform/src/test/java/com/uam/psychoform/SchemaValidationIntegrationTest.java`

- [ ] **Step 1: Run the complete test suite**

```powershell
Set-Location psychoform
$env:BFA_DB_URL='jdbc:postgresql://127.0.0.1:54322/postgres'
$env:BFA_DB_USERNAME='postgres'
$env:BFA_DB_PASSWORD='postgres'
.\mvnw.cmd test
```

Expected: all tests pass and Hibernate validates without schema changes.

- [ ] **Step 2: Reconfirm migration lifecycle**

```powershell
Set-Location ..
supabase db reset
supabase migration list
```

Expected: reset succeeds and the baseline migration is recorded.

- [ ] **Step 3: Final commit**

```powershell
git add psychoform supabase
git commit -m "test: validate BFA persistence model"
```
