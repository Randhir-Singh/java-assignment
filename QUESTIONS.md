# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
Yes, I would recommend standardizing the data access patterns across the codebase for consistency and maintainability.

Currently, the codebase uses multiple approaches:
1. **PanacheRepository Pattern** (Product, Store): Uses Quarkus Panache which provides a simplified ORM abstraction, inheriting from PanacheRepository and PanacheEntity. This provides ActiveRecord-style methods and is simple for basic CRUD operations.

2. **Direct Repository Pattern** (Warehouse): Uses a custom WarehouseRepository with explicit CRUD methods, more explicit control over queries, and better separation of concerns.

3. **Manual Collections** (Location): Uses an in-memory static collection within a Gateway, simulating a data store.

**Recommended Refactoring:**
- **Standardize on Repository Pattern**: Use explicit repositories for all entities (Product, Store, Warehouse, Location). This provides:
  * Better separation of concerns between domain and persistence layers
  * Easier testing (repositories can be mocked)
  * More explicit and discoverable query methods
  * Better support for complex queries and filtering

- **Gradual Migration**: 
  * Product and Store could be refactored from PanacheEntity inheritance to proper domain entities with a separate PanacheRepository implementation
  * Location could move to a proper LocationRepository instead of a static gateway

- **Benefits**:
  * Consistent patterns across the team
  * Easier to switch ORM frameworks in future if needed
  * Better testability through dependency injection
  * Clearer domain model separation from persistence concerns
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
**OpenAPI-First Approach (Warehouse):**
Pros:
- Contract-first development: API contract is defined before implementation
- Automatic code generation reduces boilerplate and implementation errors
- Self-documenting: API documentation is always in sync with code
- Easy to generate client SDKs for multiple languages
- Single source of truth for API structure
- Better for teams: Clear API contracts prevent miscommunication
- Tool ecosystem: Can generate tests, mocks, and documentation automatically

Cons:
- Additional tooling and build complexity
- Learning curve for OpenAPI specification
- Code generation can be inflexible for custom logic
- Generated code might require post-processing
- Tight coupling to generated code structure

**Direct Implementation Approach (Product, Store):**
Pros:
- Faster initial development for simple CRUD operations
- Full control over implementation details
- Simpler for small teams or prototyping
- Easier to debug since you wrote all the code
- No code generation overhead

Cons:
- Documentation can get out of sync with code
- Manual documentation maintenance
- No contract enforcement between teams
- Harder to generate client SDKs
- Code duplication across similar endpoints
- Higher risk of inconsistent API design

**My Choice:**
I would standardize on the **OpenAPI-First approach** for the entire codebase. Here's why:

1. **Consistency**: All endpoints follow the same pattern and structure
2. **Scalability**: As the codebase grows, having a clear contract prevents chaos
3. **Team Collaboration**: Clear API contracts improve communication and reduce bugs
4. **Maintenance**: Generated code provides consistency and reduces manual errors
5. **Client Integration**: Easier for frontend and external consumers to work with

However, the implementation approach matters:
- Use OpenAPI generation as a foundation but allow custom business logic in service/use case layers
- Keep generated code in adapters/restapi layer separate from domain logic
- Generated models and interfaces should be clear guides, not restrictions
- Document why code deviates from the spec if needed

For existing Product and Store endpoints, consider gradually migrating them to OpenAPI specifications to maintain consistency with the Warehouse API.
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
**Testing Strategy - Pyramid Approach:**

I would implement a balanced testing strategy following the testing pyramid principle:

**1. Unit Tests (Foundation - 70%)**
Focus on:
- Domain layer logic and business rules (CreateWarehouseUseCase, ArchiveWarehouseUseCase, etc.)
- Gateway implementations (LocationGateway)
- Validation logic and constraints
- Edge cases and error scenarios

Benefits: Fast execution, easy to maintain, catch bugs early
Example: CreateWarehouseUseCaseTest validates location, capacity, stock constraints

**2. Integration Tests (Middle - 20%)**
Focus on:
- Repository and persistence layer
- Transaction boundaries and commit logic
- Database interactions and data integrity
- Use case flows with real repositories

Priority: Event propagation (Store/LegacyStoreManagerGateway synchronization)

**3. End-to-End/API Tests (Top - 10%)**
Focus on:
- Critical API endpoints
- Warehouse CRUD operations
- Product and Store management
- Success and error paths

Tools: RestAssured for REST API testing (already used in ProductEndpointTest)

**Implementation Approach:**

Phase 1 (Immediate):
- Expand unit tests for domain logic (use cases)
- Add integration tests for warehouse persistence
- Minimum 70% code coverage on domain logic

Phase 2 (Next):
- Add E2E tests for critical workflows
- Test Store event propagation logic
- Product-Warehouse relationship tests (bonus feature)

Phase 3 (Ongoing):
- Maintain and update tests with code changes
- Add tests for new features before implementation (TDD for critical paths)

**Ensuring Effective Coverage:**

1. **Code Coverage Tools**: Use JaCoCo (already configured) to track coverage
   - Target: 70%+ coverage on domain and adapter layers
   - Exclude generated code and models from coverage requirements

2. **Coverage by Layer**:
   - Domain logic (use cases): 100% target
   - Business rules/validations: 95%+ target
   - Repositories: 80%+ target
   - REST adapters: 60%+ (more implementation-specific)

3. **Quality Metrics**:
   - Test code review: Ensure tests actually validate behavior
   - Test maintenance: Remove brittle tests, refactor duplicates
   - Coverage trends: Monitor coverage over time in CI/CD

4. **Testing Best Practices**:
   - Use descriptive test names: testWhenLocationInvalidShouldThrowException()
   - Follow AAA pattern: Arrange, Act, Assert
   - Use test fixtures and builders for complex scenarios
   - Test happy path AND error scenarios
   - Mock external dependencies (LocationGateway in use case tests)

5. **Continuous Integration**:
   - Fail builds if coverage drops below threshold
   - Run tests in CI/CD pipeline
   - Generate and track coverage reports

**Focus Areas for This Project:**

1. **High Priority** (Must test):
   - Warehouse validation constraints (capacity, location, business unit)
   - Use case logic (create, replace, archive)
   - Location resolution
   - Stock validation

2. **Medium Priority**:
   - Store event propagation and transaction commit ordering
   - Product CRUD operations
   - Data persistence and retrieval

3. **Low Priority** (if time permits):
   - Error handling/exception paths
   - Bonus feature: Product-Warehouse-Store relationships
   - Performance testing

**Resource-Conscious Approach:**

Given time constraints:
- 80/20 rule: Focus 80% effort on 20% of code that has highest business impact
- Prioritize domain layer and business rule tests
- Use auto-generated tests for CRUD operations where possible
- Reduce E2E tests if integrated tests cover the same paths
- Create test templates/builders for similar test scenarios

This approach ensures critical business logic is well-tested, catches the most important bugs early, and remains maintainable as the codebase evolves.
```