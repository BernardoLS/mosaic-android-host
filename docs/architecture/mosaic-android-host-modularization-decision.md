# Android Host Modularization Decision

**Project:** Mosaic\
**Status:** Accepted\
**Scope:** `mosaic-android-host`\
**Decision:** Adopt a pragmatic three-module structure: `:app`, `:core`,
and `:feature:profile`.

## 1. Context

Mosaic's Android Host coordinates native Android functionality and
integrates multiple mobile runtimes. As the project evolves, it will
need infrastructure for module discovery, lifecycle management,
navigation, events, shared context, and error handling.

Keeping every responsibility inside the application module could couple
the application entry point to infrastructure concerns. Conversely,
creating a separate Gradle module for every technical component now
would introduce unnecessary configuration and architectural overhead.

The project needs a boundary between the executable Android application,
shared Host infrastructure, and native product features---without
prematurely fragmenting the codebase.

## 2. Decision

The Android Host will initially use this Gradle module structure:

``` text
:app
:core
:feature:profile
```

### `:app` --- Application and composition root

The `:app` module is the executable Android application and entry point
of the Host.

Responsibilities:

-   `MosaicApplication` and application startup.
-   `MainActivity` and Android application entry points.
-   Host UI composition and application-level navigation integration.
-   `HostBootstrap` and composition of infrastructure and feature
    modules.
-   Application-specific build, packaging, and distribution
    configuration.

The `:app` module may depend on `:core` and feature modules to assemble
the application.

### `:core` --- Host infrastructure

The `:core` module is an Android library containing shared Host
infrastructure, its internal abstractions, and concrete implementations
where appropriate.

Planned responsibility areas:

-   Module Registry.
-   Module Lifecycle.
-   Navigation.
-   Event communication.
-   Shared Context.
-   Error handling.

The module may define interfaces and provide their default
implementations together when a real architectural boundary justifies
the abstraction. A separate implementation module will not be created by
default.

The `:core` module must not depend on `:app` or feature modules.

### `:feature:profile` --- Native Profile feature

The `:feature:profile` module is an Android library implementing the
native Profile feature using Kotlin and Jetpack Compose.

It owns the Profile feature's UI and feature-specific behavior. It may
depend on `:core` when it needs Host-provided contracts or capabilities,
but it must not depend on `:app`.

## 3. Dependency direction

The intended dependency graph is:

``` text
:app ─────────> :core
  |
  └───────────> :feature:profile

:feature:profile ──> :core
```

Constraints:

-   `:core` does not know about `:app`.
-   `:core` does not depend on feature modules.
-   Feature modules do not depend on `:app`.
-   The application module composes Host infrastructure and features.
-   Circular dependencies are not allowed.

## 4. Relationship with `mosaic-contracts`

The `mosaic-contracts` repository remains the source of truth for
runtime-agnostic contracts and specifications, including JSON Schemas
and shared message formats.

The Android `:core` module implements or adapts those contracts for the
Android Host. It must not become the owner of cross-runtime
specifications or introduce Kotlin-only requirements into contracts
consumed by Flutter and React Native.

## 5. Architectural principles

1.  **Separate by responsibility:** modules represent meaningful
    architectural boundaries, not merely technical folders.
2.  **Keep the composition root explicit:** application startup and
    dependency composition belong in `:app`.
3.  **Protect dependency direction:** infrastructure and features must
    not depend on the application entry point.
4.  **Abstract selectively:** introduce interfaces when they establish a
    real boundary, enable meaningful testing, or support a legitimate
    variation.
5.  **Avoid premature modularization:** do not create additional Gradle
    modules without a concrete need.
6.  **Keep domain ownership with features:** the Host provides
    cross-cutting infrastructure; it does not absorb feature-specific
    business rules or state.

## 6. Consequences and trade-offs

### Benefits

-   Clear separation between application startup, Host infrastructure,
    and native feature implementation.
-   Infrastructure can be tested independently from the Activity and
    application UI.
-   Explicit dependency direction reduces coupling.
-   The structure leaves room to add React Native and Flutter runtime
    integration without turning `MainActivity` into the owner of every
    concern.
-   Fewer modules and less setup than a fully fragmented infrastructure
    architecture.

### Costs and risks

-   Gradle module configuration and dependency wiring become more
    involved than a single-module application.
-   `:core` could become too broad if unrelated responsibilities are
    placed there without boundaries.
-   Abstractions can add indirection if introduced without a concrete
    use case.
-   The team must preserve dependency rules as the project grows.

These costs are accepted because the boundaries support the
multi-runtime architecture while keeping the initial implementation
pragmatic.

## 7. Explicitly out of scope for now

The following modules will not be created at this stage:

-   Separate Gradle modules for Registry, Events, Navigation, Lifecycle,
    Context, or Errors.
-   A dedicated `:infrastructure` implementation module.
-   Separate `:core:contracts`, `:core:registry`, `:core:runtime`, or
    `:core:communication` modules.

These may be considered later if implementation complexity, independent
reuse, build performance, ownership, or testing needs provide a concrete
reason.

## 8. Implementation sequence

1.  Create `:core` as an Android Library.
2.  Move or introduce only code that genuinely belongs to shared Host
    infrastructure.
3.  Create `:feature:profile` as an Android Library with Jetpack
    Compose.
4.  Configure and verify Gradle dependency directions.
5.  Validate the build and relevant tests.
6.  Implement Host infrastructure capabilities incrementally, starting
    with the needs of the first integrated feature.

Existing Hilt configuration and its successful validation must be
preserved during the transition.

## 9. Revisit criteria

Reconsider this structure when one or more of the following occurs:

-   A core responsibility needs independent ownership, release, or
    reuse.
-   `:core` becomes difficult to navigate, test, or evolve as a single
    module.
-   Runtime adapters introduce dependencies that should be isolated from
    the rest of the Host.
-   Build times or dependency graphs show a measurable benefit from
    further modularization.
-   A feature requires a boundary that cannot be represented cleanly
    with the current modules.

Until then, the three-module structure remains the baseline for the
Android Host.
