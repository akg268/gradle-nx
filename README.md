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

## How Nx is integrated with Gradle

Nx does not replace Gradle. Gradle still compiles Java, resolves dependencies,
runs JUnit, and packages the modules. Nx reads Gradle's project and task graph,
then decides which Gradle tasks need to run.

### 1. Initialize Nx in an existing Gradle repository

From the root of a Gradle repository, run:

```bash
npx nx@latest init --plugins=@nx/gradle
```

The initializer creates the Nx wrapper (`nx`, `nx.bat`, and `.nx/nxw.js`),
adds `nx.json`, installs `@nx/gradle`, and adds the companion Gradle plugin.
This example pins the generated Nx installation to version `23.3.0` in
`nx.json`.

### 2. Expose the Gradle project graph to Nx

The root [`build.gradle.kts`](build.gradle.kts) applies the Nx project-graph
plugin:

```kotlin
plugins {
    id("dev.nx.gradle.project-graph") version("0.1.25")
    base
}

allprojects {
    apply(plugin = "dev.nx.gradle.project-graph")
}
```

The companion plugin adds Gradle reporting tasks such as `nxProjectGraph` and
`nxProjectReport`. `@nx/gradle` invokes those tasks and converts their output
into the Nx project graph.

The existing Gradle modules remain declared normally in
[`settings.gradle.kts`](settings.gradle.kts):

```kotlin
include(
    ":libs:domain",
    ":services:payments",
    ":services:orders",
)
```

### 3. Let Gradle dependencies create Nx graph edges

The payment and order modules contain a normal Gradle project dependency:

```kotlin
dependencies {
    implementation(project(":libs:domain"))
}
```

Nx reads that relationship from Gradle. There is no second dependency map to
maintain in `nx.json`.

```text
:libs:domain
    ├──> :services:payments
    └──> :services:orders
```

That dependency is why a `domain` change affects both services, while a
`payments` change does not affect `orders`.

### 4. Map Nx targets to Gradle tasks

[`nx.json`](nx.json) enables the Gradle integration and names the inferred
targets:

```json
{
  "plugins": [
    {
      "plugin": "@nx/gradle",
      "options": {
        "testTargetName": "test",
        "classesTargetName": "classes",
        "buildTargetName": "build"
      }
    }
  ]
}
```

The resulting command mapping looks like this:

| Nx command | Gradle task executed |
| --- | --- |
| `./nx test ':services:payments'` | `./gradlew :services:payments:test` |
| `./nx build ':services:orders'` | `./gradlew :services:orders:build` |
| `./nx classes ':libs:domain'` | `./gradlew :libs:domain:classes` |

Nx schedules the selected target and its dependencies. Gradle remains the
executor and continues to honor its own task graph and build configuration.

### 5. Add affected selection above the Gradle graph

For `nx affected`, Nx combines two pieces of information:

1. Git identifies files changed between the base and head revisions.
2. The Gradle project graph identifies the owning modules and their downstream
   consumers.

Nx then invokes only the selected Gradle test tasks. This selection happens at
the module/task level, not at the individual JUnit-method level.

### Integration flow

```text
build.gradle.kts and settings.gradle.kts
                 ↓
dev.nx.gradle.project-graph
                 ↓
          Nx project graph
                 +
              Git diff
                 ↓
       Affected Nx test targets
                 ↓
       Corresponding Gradle tasks
```

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
