# AGENTS.md

## Project Overview

This is the **Camunda 8 Process Solution Template**, a Spring Boot application that serves as a
starting point for building process solutions on [Camunda Platform 8](https://camunda.com/).
It is maintained under the `camunda-community-hub` GitHub organization.

- **Repository:** `camunda-community-hub/camunda-8-process-solution-template`
- **Java:** 21
- **Spring Boot:** 3.5.3
- **Camunda SDK:** 8.8.21 (`camunda-spring-boot-starter`)
- **Build tool:** Maven (use `./mvnw`, the checked-in Maven wrapper)
- **BPMN Process ID:** `camunda-process` (defined in `ProcessConstants.java`)

## Build & Run

```bash
# Compile (includes downloading dependencies)
./mvnw compile

# Run the application
./mvnw spring-boot:run

# Run tests (requires Docker for Testcontainers)
./mvnw test
```

Swagger UI is available at `http://localhost:8080/swagger-ui.html` when running.

## Project Structure

```
src/main/java/org/example/camunda/process/solution/
├── ProcessApplication.java      # @SpringBootApplication + @Deployment (deploys BPMN models)
├── ProcessConstants.java        # BPMN process ID constant ("camunda-process")
├── ProcessVariables.java        # Lombok-based POJO (businessKey, result)
├── facade/
│   └── ProcessController.java   # REST controller: POST /process/start, /process/message/{name}/{key}
├── service/
│   └── MyService.java           # Business logic service (called by workers)
└── worker/
    └── MyWorker.java            # @JobWorker for "invokeMyService" task type

src/main/resources/
├── application.properties       # Active config (SaaS mode with camunda.client.* properties)
├── application.local.properties # Legacy local self-managed config (old zeebe.client.* format)
├── application.saas.properties  # Legacy SaaS config template (old zeebe.client.* format)
└── models/
    └── camunda-process.bpmn     # The BPMN process model

src/test/java/org/example/camunda/process/solution/
└── ProcessUnitTest.java         # Spring Boot test using @CamundaSpringProcessTest + Testcontainers
```

## Key Dependencies & Artifact Names (Camunda 8.8+)

| Purpose | Artifact |
|---|---|
| Spring Boot Starter | `io.camunda:camunda-spring-boot-starter` |
| Test (Spring) | `io.camunda:camunda-process-test-spring` |
| BOM | `io.camunda:zeebe-bom` |

**Note:** Prior to 8.8, these were called `spring-boot-starter-camunda-sdk` and
`spring-boot-starter-camunda-test`. The old names are no longer published.

## Import Packages (Camunda 8.8+)

The SDK underwent a major package rename in 8.8. The old `io.camunda.zeebe.*` packages are
deprecated (with `forRemoval = true`). Use the new imports:

| Class / Annotation | Import |
|---|---|
| `CamundaClient` | `io.camunda.client.CamundaClient` |
| `@Deployment` | `io.camunda.client.annotation.Deployment` |
| `@JobWorker` | `io.camunda.client.annotation.JobWorker` |
| `@VariablesAsType` | `io.camunda.client.annotation.VariablesAsType` |
| `@Variable` | `io.camunda.client.annotation.Variable` |
| `ProcessInstanceEvent` | `io.camunda.client.api.response.ProcessInstanceEvent` |
| `ActivatedJob` | `io.camunda.client.api.response.ActivatedJob` |
| `JobClient` | `io.camunda.client.api.worker.JobClient` |

**Old (deprecated):** `io.camunda.zeebe.client.ZeebeClient`,
`io.camunda.zeebe.spring.client.annotation.*` — still exist in 8.8 jars but are marked for removal.

## Testing (Camunda 8.8+)

Tests use the **Camunda Process Test (CPT)** framework, replacing the old Zeebe Process Test (ZPT).

| Old (ZPT) | New (CPT) |
|---|---|
| `@ZeebeSpringTest` | `@CamundaSpringProcessTest` |
| `ZeebeTestEngine` | `CamundaProcessTestContext` |
| `BpmnAssert.assertThat()` | `CamundaAssert.assertThat()` |
| `InspectionUtility.findProcessInstances()` | Use `CamundaAssert.assertThatProcessInstance()` with selectors |
| `ZeebeTestThreadSupport.waitFor*()` | Not needed — CPT assertions use Awaitility internally |
| `.isStarted()` | `.isCreated()` |
| `.hasPassedElement("X")` | `.hasCompletedElements("X")` |
| `.hasVariableWithValue("k", v)` | `.hasVariable("k", v)` |

Key test imports:
```java
import io.camunda.process.test.api.CamundaAssert;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
```

**Prerequisite:** Tests require **Docker** running locally (CPT uses Testcontainers to spin up
a Camunda runtime).

## Configuration Profiles

The active config is `application.properties` which uses Camunda 8.8 SaaS mode:

```properties
camunda.client.mode=saas
camunda.client.auth.client-id=...
camunda.client.auth.client-secret=...
camunda.client.cloud.cluster-id=...
camunda.client.cloud.region=fra-1
```

For local self-managed development (Docker Compose), set:

```properties
camunda.client.mode=self-managed
camunda.client.zeebe.grpc-address=http://localhost:26500
camunda.client.zeebe.rest-address=http://localhost:8088
```

**Legacy files** `application.local.properties` and `application.saas.properties` use the old
`zeebe.client.*` property namespace and are not active. They can be removed or updated.

## Coding Conventions

- **Lombok** is used for POJOs (`@Getter`, `@Setter`, `@Accessors(chain = true)`)
- **Spotless** is configured for code formatting (`spotless-maven-plugin`)
- **Google Java Format** is the formatter
- JSON serialization uses Jackson with `@JsonInclude(Include.NON_NULL)`
- Constructor injection is preferred (no `@Autowired` on fields in production code)

## Common Pitfalls

1. **Version mismatch:** The Camunda SDK version must match your cluster version. A SaaS cluster
   running 8.8 requires SDK 8.8.x — using an older SDK (e.g., 8.6) causes startup failures like
   `grpcAddress must be an absolute URI`.

2. **ProcessController returns void:** The `startProcessInstance()` method fires and forgets.
   In tests, use `CamundaClient` directly with `.send().join()` to get a `ProcessInstanceEvent`
   that can be passed to assertions.

3. **Docker required for tests:** The `camunda-process-test-spring` artifact uses Testcontainers.
   Tests will fail if Docker is not available.
