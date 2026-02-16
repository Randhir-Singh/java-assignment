# GitHub Actions Workflows Documentation

This directory contains all the CI/CD workflows for the Java Assignment project.

## 📋 Available Workflows

### 1. **CI Pipeline** (`ci.yml`)
**Trigger:** Push to main/master/develop branches, Pull Requests, Manual dispatch

**Jobs:**
- **build-and-test**: Compiles code, runs tests with H2 database, generates JaCoCo coverage
- **code-quality**: Runs additional quality checks

**Artifacts:**
- JaCoCo coverage report
- Test results (Surefire reports)
- JAR artifacts


### 2. **Release** (`release.yml`)
**Trigger:** Push to tags matching `v*`, Manual dispatch

**Features:**
- Builds release artifacts
- Creates GitHub release
- Uploads JAR to release assets

## 🗄️ Database Configuration

### Test Database: H2 In-Memory
The tests use H2 database configured to emulate PostgreSQL:
- **JDBC URL:** `jdbc:h2:mem:testdb;MODE=PostgreSQL`
- **User:** `sa`
- **Password:** (empty)

Configuration file: `src/test/resources/application.properties`

### Why H2?
- ✅ No external database required
- ✅ Fast test execution
- ✅ PostgreSQL compatibility mode
- ✅ Perfect for CI/CD pipelines
- ✅ Automatic cleanup after tests

## 📊 Code Coverage

JaCoCo is configured to:
- Generate coverage reports after tests
- Exclude certain packages:
  - `**/stores/**` (no tests)
  - `**/adapters/database/**` (integration tests)
  - `**/warehouse/api/**` (generated code)
  - `**/generated/**` (generated code)

### View Coverage Locally
```bash
mvn clean test jacoco:report
```
Then open: `target/site/jacoco/index.html`

## 🔧 Manual Workflow Execution

All workflows can be triggered manually via GitHub UI:
1. Go to "Actions" tab
2. Select the workflow
3. Click "Run workflow"
4. Choose branch and click "Run workflow"

## 📦 Maven Commands

```bash
# Run tests with H2 database
mvn test

# Generate coverage report
mvn jacoco:report

# Check coverage thresholds
mvn jacoco:check

# Full build with tests
mvn clean verify

# Package without running tests
mvn package -DskipTests
```

## 🔐 Required Secrets

Currently, no secrets are required. If you want to add:
- Slack notifications
- External reporting services
- Deployment credentials

Add them via: Repository Settings → Secrets and variables → Actions


## 🚀 Getting Started

1. Push your code to GitHub
2. Workflows will automatically trigger
3. Check "Actions" tab for workflow status
4. Download artifacts from completed workflow runs

## 📝 Customization

To customize workflows:
- Edit `.github/workflows/*.yml` files
- Adjust test database in `src/test/resources/application.properties`
- Modify coverage exclusions in `pom.xml`

## 🐛 Troubleshooting

### Tests fail in CI but pass locally?
- Check H2 compatibility mode
- Verify test resources are committed
- Review PostgreSQL-specific queries

### Coverage not generated?
```bash
mvn clean test jacoco:report
```
Check if `target/site/jacoco/` exists

### Workflow not triggering?
- Verify branch names in workflow files
- Check if `.github/workflows/` is in repository root
- Ensure YAML syntax is correct

## 📞 Support

For issues with workflows:
1. Check workflow logs in GitHub Actions
2. Review this documentation
3. Contact the development team

