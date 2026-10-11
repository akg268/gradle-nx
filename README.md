# Gradle + Nx affected-testing example

This repository demonstrates how Nx can sit on top of a Gradle multi-project
build and run tests only for the affected modules.

```text
libs:domain
    ├──> services:payments
    └──> services:orders
```

Changing `payments` affects only `payments`. Changing the shared `domain`
library affects `domain`, `payments`, and `orders`.

## Prerequisites

- Java 17 or newer (the example uses Java 21)
- Node.js 20 or newer
- An internet connection for the first run

You do not need a system Gradle installation. The Gradle wrapper is included.
The Nx wrapper downloads the Nx version pinned in `nx.json` on its first run.

## Explore the workspace

List the projects Nx inferred from Gradle:

```bash
./nx show projects
```

Open the dependency graph:

```bash
./nx graph
```

Inspect one project and its inferred Gradle targets:

```bash
./nx show project ':services:payments'
```

## Run tests

Run every module's tests:

```bash
./nx run-many -t test
```

Run only the payment service tests:

```bash
./nx test ':services:payments'
```

Nx invokes Gradle and also schedules the Gradle tasks needed to build
`libs:domain`.

## Demonstrate affected selection without making a Git commit

Pretend that only `PaymentService.java` changed:

```bash
./nx show projects --affected \
  --files=services/payments/src/main/java/dev/example/payments/PaymentService.java
```

The affected test target is only `:services:payments`.

Pretend that the shared `Money.java` class changed:

```bash
./nx show projects --affected \
  --files=libs/domain/src/main/java/dev/example/domain/Money.java
```

Nx includes both services because both depend on `domain`. Run that affected
test set with:

```bash
./nx affected -t test \
  --files=libs/domain/src/main/java/dev/example/domain/Money.java
```

## Use it in a real Git branch or pull request

After the repository has Git history, compare the branch with `main`:

```bash
git fetch origin main

./nx affected -t test \
  --base=origin/main \
  --head=HEAD
```

Your CI checkout must contain the base revision. A shallow checkout that does
not contain `origin/main` cannot calculate the correct affected set.

## What each tool is doing

```text
Git diff
   ↓
Nx maps changed files to Gradle projects
   ↓
Nx follows downstream project dependencies
   ↓
Nx asks Gradle to execute the selected test tasks
   ↓
Nx and Gradle caches reuse unchanged outputs
```

Nx performs project/task-level selection here. It does not select individual
JUnit methods inside an affected module. Gradle remains responsible for
compilation, dependency resolution, and test execution.

## Important files

- `settings.gradle.kts` declares the Gradle modules.
- `build.gradle.kts` applies the Nx Gradle project-graph plugin.
- `nx.json` configures `@nx/gradle` and Nx caching.
- `gradle.properties` enables the Gradle build and configuration caches.
