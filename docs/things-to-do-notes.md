# Things to do — backend notes

Inspection of this repo before the catalogue schema. Where the feature brief and the code disagree, the code wins. Those choices are listed here.

## Stack

- Java 17, Spring Boot 3.2.0, Maven multi-module, base package `com.seaandtea`.
- Modules: `seaandtea-common` (exceptions), `seaandtea-domain` (entities and repositories), `seaandtea-security` (JWT and the filter chain), `seaandtea-identity`, `seaandtea-media` (Cloudinary), `seaandtea-guide`, `seaandtea-tour`, `seaandtea-shop`, `seaandtea-engagement` (news, reviews, messages), `seaandtea-application` (boot entry, Liquibase, file-upload controller).
- New code lives in a `seaandtea-thingstodo` module (controllers, services, DTOs, seeder). Entities and repositories stay in `seaandtea-domain`, same as `NewsPost` and `Tour`. The placeholder package `com.seaandtea.thingstodo` is not used.
- `SeaAndTeaApplication` scans `com.seaandtea`, scans entities in `com.seaandtea.entity`, and repositories in `com.seaandtea.repository`.
- Controllers map `/api/v1/...`. Roles are checked with `@PreAuthorize("hasRole('ADMIN')")` and `User.UserRole`. The signed-in user is `Authentication.getName()` (email), then `UserRepository.findByEmail`.
- DTOs are Lombok classes built in services. Enums use `@Enumerated(EnumType.STRING)`. Timestamps are `LocalDateTime` set in `@PrePersist` / `@PreUpdate`, not `Instant`.
- Errors go through `GlobalExceptionHandler`: `IllegalArgumentException` is 400, `IllegalStateException` is 409, `ResourceNotFoundException` is 404, `AccessDeniedException` is 403. A new `UnauthorizedException` is 401 for admin catalogue calls with no token. The login handler still says "Invalid email or password".
- `SecurityConfig` permits every route and relies on method security. An anonymous caller fails `@PreAuthorize` as 403. Catalogue admin paths therefore check for a real principal in a handler interceptor before method security runs, and answer 401 when there is no token.

## Database

- PostgreSQL in deployed config (Supabase pooler). Liquibase changelog `db/changelog/db.changelog-master.xml`. `spring.jpa.hibernate.ddl-auto` is `update` in `application.yml` and `none` in the local profile.
- Schema changes are Liquibase changesets with `preConditions`, same as `004-user-oauth.xml`. The catalogue changeset is `005-things-to-do.xml`. It does not use Postgres arrays or GIN indexes, because `application-test.yml` runs Liquibase on H2.
- Season months are rows in `activity_season_month` (`month`, `in_season`, `peak`). A month can be peak without being the season window (Udawalawe is year-round with peak months). An activity is in season for month `m` when `year_round` is true or a row has `month = m` and `in_season = true`.
- Filtered multi-value fields (`tourCategories`, `cuisines`, `dietary`) are `@ElementCollection` join tables. `guideSpecialtyKeywords` and `goodFor` are JSON (`VARCHAR(2000)`, the changelog's `${json.type}`), same as `Tour.highlights`.
- Sources are `activity_sources`, `event_sources`, and `restaurant_sources`. Galleries are `activity_images`, `event_images`, and `restaurant_images`. Tour links are `tour_thing_links` (`tour_id`, `thing_type`, `thing_id`).
- `Tour.destination` is a string limited to the 12 names in `Destinations`. This pass does not widen that list. Automatic tour matching compares an item's destination name (and nearby names) to `tour.destination`, case-insensitive. Arugam Bay is not a tour destination, so its tour list is empty until a later change.
- Tour `pricePerPerson` is USD. `formatUsd` prints it as USD and `formatLkr` multiplies by 300. `fromPriceUsd` is the lowest matching tour price, not that price divided by 300.
- `reservationsEmail` is stored for admin use only. Public JSON does not include it, so a public body never has an `email` field.

## Seeder

`ThingsToDoSeeder` is an `ApplicationRunner`. It reads `classpath:seed/things-to-do-seed.json` and upserts by slug. Destinations first, then activities, then a second pass for `seasonalAlternativeSlug`, then events (with occurrences) and restaurants. Source `accessedAt` is `2026-11-26`. Status is copied from the JSON. `DRAFT` stays `DRAFT`.

It skips a row when `updatedAt` is after `createdAt`, so an admin edit is left alone. Seed writes set a transient flag so they do not move `updatedAt`.

```yaml
things-to-do:
  seed:
    enabled: true
  deploy-hook-url: ${THINGS_TO_DO_DEPLOY_HOOK_URL:}
```

The test profile sets `things-to-do.seed.enabled` to `false` so the existing context test does not load the catalogue. To seed locally, leave the property true (the default) and start the API:

```bash
mvn -pl seaandtea-application -am spring-boot:run
```

Set `THINGS_TO_DO_DEPLOY_HOOK_URL` to a Vercel deploy hook if publishing should rebuild the prerendered pages. Empty means no call. A failed hook does not fail the admin request.

## Not in this pass

Guide suggestions, the tour-form link picker, and widening `Destinations.ALL` are a later step. The columns `submitted_by_user_id`, `moderation_note`, and status `PENDING_REVIEW` exist so that step does not need another migration.
