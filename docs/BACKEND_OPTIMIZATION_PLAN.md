# Backend optimization roadmap

## Objective

Reduce accidental complexity while preserving hexagonal architecture, domain behavior,
transaction atomicity, optimistic locking and stable HTTP contracts.

## Mandatory architecture

```text
infrastructure -> application -> domain

order    -> shared
identity -> shared
vehicle  -> shared
driver   -> identity, vehicle, shared
delivery -> order, driver, shared
```

- Domain must not depend on Spring, JPA, application or infrastructure.
- Application must not depend on Spring, JPA or concrete adapters.
- MapStruct is preferred only for structural mapping.
- Aggregate reconstruction, value objects, validation and locking remain explicit.
- `ApiResponse<T>` belongs to shared HTTP infrastructure and is applied centrally.
- `204 No Content` remains bodyless.
- ArchUnit is the primary automated architecture verifier.
- UUIDs remain plain `UUID`; no accidental ID wrapper classes are introduced.

## Completed: Order

- Consolidated transaction policy using infrastructure proxies.
- Grouped cohesive query and workflow input ports.
- Simplified structural HTTP MapStruct mappings.
- Kept persistence aggregate reconstruction explicit.
- Converted suitable immutable DTOs to records.
- Moved the order-ready consumer to delivery and removed `order -> delivery`.

## Completed: Delivery

- Removed seven per-use-case transaction decorators.
- Added one infrastructure transaction proxy factory.
- Preserved `REQUIRED` commands, read-only queries and `REQUIRES_NEW` autoassignment.
- Consolidated two query ports into `AssignmentQueryUseCase`.
- Consolidated five lifecycle boundaries into `AssignmentWorkflowUseCase` while retaining
  small internal operations with focused responsibilities.
- Reduced controller use-case dependencies from nine to four.
- Converted `AssignmentResult` to a record.
- Removed redundant MapStruct Java expressions from the HTTP mapper.
- Replaced persistence MapStruct mapping with an explicit mapper because aggregate
  reconstitution and business-offset normalization are behaviorally relevant.
- Verified Lombok usage on the JPA entity and kept domain/application independent of it.
- Added transaction policy tests and functional-module ArchUnit rules.

## Deliberate remaining delivery decision

Delivery currently coordinates `Order` and `Driver` aggregates through their repository
ports inside one local transaction. Replacing those dependencies with inter-context ports
is not a mechanical cleanup: it changes contract ownership and potentially atomicity.
Design characterization tests and explicit order/driver integration contracts before
making that change. Do not move business transitions into infrastructure adapters.

## Next phases

1. Optimize driver/vehicle transactions and input-port cohesion.
2. Review persistence mappers and inter-context result assembly.
3. Optimize identity while preserving authentication and refresh-token behavior.
4. Finish shared HTTP error fields and OpenAPI envelope accuracy.
5. Run the full PostgreSQL/Testcontainers suite, remove empty packages and perform a final
global naming/dependency audit.

## In progress: Driver and vehicle

- Removed the `driver <-> vehicle` cycle.
- Added the vehicle-owned `VehicleAssignmentLookupPort`; its implementation lives in
  driver outbound infrastructure.
- Moved `VehicleAssignedToDriverException` to the vehicle domain, which owns deletion rules.
- Strengthened ArchUnit so vehicle cannot depend on driver again.
- Converted `DriverResult` and `VehicleResult` to records.
- Removed redundant Java expressions from both HTTP MapStruct mappers.
- Verified the cycle/mapping increment with 47 focused and architecture tests.
- Added the shared infrastructure `TransactionProxyFactory` with commit/rollback tests.
- Applied read-only transactions to driver and vehicle queries.
- Applied read-write transactions to every driver/vehicle command and driver provisioning.
- Removed the two remaining driver transaction decorator classes.
- Verified the transaction increment with 45 focused and architecture tests.
- Converted `DriverPersistenceMapper` and `VehiclePersistenceMapper` to explicit Spring
  components: aggregate reconstitution, business-time normalization, coordinate scale and
  optimistic-lock preservation are now visible instead of hidden in MapStruct expressions.
- Restored jMolecules package metadata removed during earlier cleanup; metadata-only
  `package-info.java` files are architectural code, not empty packages.
- Verified the persistence/metadata increment from a clean build with 55 focused and
  architecture tests.
- Next: consolidate only cohesive query/lifecycle input ports and reduce controller
  dependencies without creating oversized application services.
- Consolidated driver queries into `DriverQueryUseCase`, availability transitions into
  `DriverAvailabilityUseCase`, and vehicle assignment into `DriverVehicleUseCase`.
- Consolidated vehicle queries into `VehicleQueryUseCase` and status transitions into
  `VehicleLifecycleUseCase`.
- Retained focused internal operation services so workflows with different dependencies do
  not become oversized classes.
- Reduced `DriverController` functional dependencies from 9 to 6 and `VehicleController`
  dependencies from 8 to 5.
- Removed eleven fragmented input-port interfaces while preserving endpoint behavior and
  transaction boundaries.
- Verified the cohesion increment from a clean build with 57 focused and architecture tests.
- Completed the first identity optimization increment:
  - replaced three bespoke transaction decorators with the shared infrastructure proxy;
  - made authentication, user commands and lifecycle operations read-write, and user
    queries explicitly read-only;
  - consolidated authentication, user-query and user-lifecycle HTTP boundaries while
    retaining focused internal services;
  - reduced `AuthController` functional dependencies from three to one and
    `UserController` dependencies from six to four;
  - converted `UserResult` to a record and kept HTTP MapStruct mapping structural;
  - replaced expression-heavy persistence MapStruct mappers with explicit components so
    aggregate reconstruction, token hashes, business-time normalization and optimistic
    locking remain visible;
  - retained the identity-owned `DriverProfileProvisionerPort`: driver implements the
    outbound contract, so identity does not acquire an invalid dependency on driver;
  - removed the now-empty identity transaction package and seven fragmented input ports;
  - verified the increment from a clean compilation with 46 focused and architecture
    tests (15 application/domain tests plus 31 HTTP/transaction/architecture tests).
- Completed shared HTTP/OpenAPI consolidation:
  - retained the infrastructure-only `ApiResponse<T>` envelope because all functional
    REST adapters already expose it consistently through one `ResponseBodyAdvice`;
  - explicitly preserved bodyless `204 No Content` responses;
  - changed validation failures from one concatenated message to structured
    `{field, message}` error entries while domain/application errors remain fieldless;
  - added one OpenAPI customizer that represents every JSON `2xx` response as
    `{code, success, data, errors}`, preserving the endpoint payload schema under `data`;
  - excluded `204` from OpenAPI wrapping and documented the global response convention;
  - verified all functional REST adapters and architecture rules with 87 tests.
- Completed the final global audit:
  - moved the cross-module HTTP exception routing and security wiring from `shared` to the
    application composition root (`com.fleetbite.infrastructure`);
  - strengthened ArchUnit so every `shared` layer, including infrastructure, is forbidden
    from depending on order, delivery, driver, vehicle or identity;
  - confirmed that MapStruct remains only in the five purely structural HTTP mappers;
  - confirmed that persistence mappers with aggregate reconstruction, value conversion or
    optimistic locking remain explicit;
  - confirmed Lombok is used deliberately on JPA entities and configuration properties,
    not in domain/application;
  - confirmed UUID is used directly and no accidental ID wrapper classes exist;
  - confirmed there are no empty source packages;
  - ran the complete clean suite using Docker 28.3, PostgreSQL 17 and all 10 Flyway
    migrations: 304 tests passed, including Testcontainers integration and 17 ArchUnit
    rules.

## Final status

The planned backend optimization is complete. Future changes must keep the architecture
tests green and follow the mapping, transaction, HTTP-envelope and module-ownership
decisions recorded in this document.

## Verification workflow

For every increment: characterize behavior, make one structural change, compile, run unit
and architecture tests, run available integration tests, inspect the diff, and update this
document.
