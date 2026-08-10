# Backend optimization roadmap

## Objective

Reduce accidental complexity while preserving hexagonal architecture, domain behavior,
transaction atomicity, optimistic locking and stable HTTP contracts.

## Mandatory architecture

```text
infrastructure -> application -> domain

order    -> shared
identity -> shared
vehicle  -> shared (temporary dependency on driver)
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

1. Remove the remaining `driver <-> vehicle` cycle and define ownership of assignment lookup.
2. Optimize driver/vehicle transactions, ports, mappers and DTOs.
3. Optimize identity while preserving authentication and refresh-token behavior.
4. Finish shared HTTP error fields and OpenAPI envelope accuracy.
5. Run the full PostgreSQL/Testcontainers suite, remove empty packages and perform a final
   global naming/dependency audit.

## Verification workflow

For every increment: characterize behavior, make one structural change, compile, run unit
and architecture tests, run available integration tests, inspect the diff, and update this
document.
