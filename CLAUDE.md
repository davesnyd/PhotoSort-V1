# Overview
Create the application in an iterative fashion. The specification is in "PhotoSpecification.md". Implement one step at a time. Each step in the specification includes: Number, name, description, components involved, and list of initial test cases.

# Quick Reference

## Project Structure
```
photoSort/
├── docs/                              # Documentation and tracking files
│   ├── WorkLog.csv                    # Development progress tracking
│   ├── Learnings.md                   # Development insights and improvements
│   ├── TestPlan.md                    # Manual testing procedures
│   ├── PhotoSortUserDocumentation.md  # User-facing documentation
│   └── PhotoSortDevDocumentation.md   # Developer documentation
├── src/
│   ├── main/java/                     # Spring Boot backend
│   └── main/resources/                # Frontend React code
├── test/                              # JUnit test cases
└── PhotoSpecification.md              # Feature specifications
```

## Environment Requirements
- **Java**: 17 or higher
- **Node.js**: 18 or higher (for React frontend)
- **Maven**: 3.8 or higher
- **Spring Boot**: Latest stable version
- **React**: Latest stable version

## Common Commands
```bash
mvn clean install      # Build the project
mvn test              # Run all tests
mvn spring-boot:run   # Run the application
```

# Files Written by Claude
Create files in the "docs" directory of the repository directory.

## WorkLog File
Maintain a file named "WorkLog.csv". It contains the following columns: Date, Time, Step Number, Step Name, Status.

**Format:**
- **Date**: YYYY-MM-DD (ISO 8601 format)
- **Time**: HH:MM (24-hour format)
- **Step Number**: Integer matching PhotoSpecification.md step number
- **Step Name**: Short description of the functionality
- **Status**: One of the following codes:
  - `DEV` - Start development
  - `START-TESTS` - Test cases initiated
  - `PLAN-CREATE` - Plan created
  - `PLAN-IMPLEMENT` - Plan implemented
  - `TESTS-AUGMENTED` - Additional test cases created
  - `CURRENT-TESTS` - Current test cases succeeded
  - `ALL-TESTS` - All test cases (previous and current) succeeded
  - `DOCS` - Documentation complete

### WorkLog Examples
```csv
Date,Time,Step Number,Step Name,Status
2025-11-26,15:47,1,Database Configuration,DEV
2025-11-26,15:50,1,Database Configuration,START-TESTS
2025-11-26,15:53,1,Database Configuration,PLAN-CREATE
2025-11-26,16:15,1,Database Configuration,PLAN-IMPLEMENT
2025-11-26,17:30,1,Database Configuration,ALL-TESTS
2025-11-26,18:00,1,Database Configuration,DOCS
```

## Learnings
Maintain a file named "Learnings.md". It contains information to improve future development. It will be a bulleted list with two pieces of information for each entry:
- **Problem**: Description of the issue or challenge encountered
- **Approach to Improve**: Solution or lesson learned

### Example Entry
```markdown
- **Problem**: Test cases failed due to missing database initialization
  **Approach to Improve**: Always include @BeforeEach setup method to initialize test data
```

## Test Plan
Maintain a file named "TestPlan.md". For each test case, include the following four sections:
1. **Name**: Descriptive name of the test case
2. **Functionality Tested**: What feature or component is being tested
3. **Steps Required**: User actions needed to execute the test
4. **Expected Outcome**: What should happen when the test succeeds

### Example Entry
```markdown
### Test Case: Upload Photo with Metadata
1. **Name**: Upload Photo with Metadata
2. **Functionality Tested**: Photo upload and EXIF data extraction
3. **Steps Required**:
   - Navigate to upload page
   - Select a JPG file with EXIF data
   - Click "Upload" button
4. **Expected Outcome**: Photo appears in gallery with correct date and location tags
```

## User Documentation
Maintain a file named "PhotoSortUserDocumentation.md". It contains a block for each piece of functionality created:
- **Functionality Created**: Name and brief description
- **UI Elements**: For each UI element, describe what it does and how to use it
  - Inputs required
  - Buttons to click
  - Expected outputs or results

## Developer Documentation
Maintain a file named "PhotoSortDevDocumentation.md". It contains a block for each piece of functionality created:
- **Functionality Created**: Name and technical description
- **Implementation Notes**: How it works, dependencies, design patterns used
- **Limitations**: Known constraints or edge cases
- **Expectations**: Assumptions about inputs, environment, or usage

# General Programming Approaches

1. Optimize for readability and robustness, not cleverness.
2. Focus on not breaking currently working functionality.
3. When planning and implementing, review the code base for methods that can be reused or extended without breaking existing functionality.
4. For each file created, add a copyright notice: `Copyright 2025, David Snyderman`
5. Programming in the backend will be in **Java** using the **Spring Boot** framework
6. Programming in the frontend will be in **JavaScript** using the **React** framework
7. Build using **Maven**
8. Write comprehensive JUnit tests with Spring Boot Testing Utilities for all functionality
9. Follow RESTful API design principles for backend endpoints
10. Use meaningful variable and method names that describe their purpose
11. When requesting permission, default to the most permissive option (which is usually the second one)

# New Functionality Implementation Steps

## 1. Development Start
Create a work log entry with status `DEV`.

## 2. Initial Testing
Review the initial test cases listed in the current step of "PhotoSpecification.md". Implement them using JUnit tests with Spring Boot Testing Utilities. Confirm that they fail (red phase of TDD). Test case names should be based on the step name and the functionality being tested (e.g., `testDatabaseConfiguration_ConnectsSuccessfully`).

**Test Guidelines:**
- Ensure tests are independent and can run in any order
- Use @BeforeEach and @AfterEach for proper setup/teardown
- Tests should not depend on execution order or shared state
- Write unit tests for individual methods and business logic
- Write integration tests for API endpoints, database operations, and component interactions
- Use @SpringBootTest for integration tests, @WebMvcTest for controller tests, @DataJpaTest for repository tests

Create a work log entry with status `START-TESTS`.

## 3. Plan Creation
**CRITICAL: Before creating the plan, thoroughly review the existing codebase for reuse opportunities.**

**Code Reuse Requirements:**
1. Search for similar functionality already implemented
2. Identify patterns that could be extracted into reusable components/services
3. Look for duplicate code that should be refactored into shared utilities
4. Consider creating generic components rather than specific implementations
5. Review existing services, hooks, and utilities that could be extended
6. Apply DRY (Don't Repeat Yourself) principle rigorously

**Planning Process:**
Format a detailed implementation plan including:
- **Code Reuse Analysis**: List existing components/code that can be reused or require refactoring
- **Refactoring Requirements**: Identify code that should be made generic/reusable as part of this step
- Components to be created or modified
- API endpoints or UI components needed
- Database schema changes (if any)
- External dependencies

For **major features or complex changes**, start a separate code-reviewer agent using the Task tool to:
- Review the plan for correctness
- Check for potential conflicts with existing functionality
- Identify additional reuse opportunities
- Suggest improvements

Incorporate suggested improvements into the plan. Consult with the user, describe the plan, highlight any foreseen problems, and receive authorization to proceed. Create a work log entry with status `PLAN-CREATE`.

Create and write a TODO list so that you know where to continue programming if you need to stop.

## 4. Plan Implementation
Implement the plan following the general programming approaches.

For **major features or complex changes**, start a separate code-reviewer agent using the Task tool to:
- Review the implementation
- Assess potential conflicts with existing functionality
- Suggest changes to improve robustness, readability, and performance

Implement suggested changes. Create a work log entry with status `PLAN-IMPLEMENT`.

Update "PhotoSpecification.md" to reflect any changes made during implementation. Update "Learnings.md" with any insights that would improve future development.

## 5. Test Case Augmentation
Review the functionality created and determine additional test cases beyond the initial ones. As much as possible, all functionality should be tested in an automated fashion using JUnit. Consider:
- Edge cases
- Error conditions
- Integration points
- Performance scenarios

Create a work log entry with status `TESTS-AUGMENTED`.

## 6. Current Test Cases
Run the test cases that can be executed with the current development level. **You MUST complete a full test-fix-iteration cycle before proceeding.** This means:

1. Run `mvn test` to execute all current test cases
2. If any tests fail:
   - Analyze the failure
   - Fix the bug
   - Run `mvn test` again
   - Repeat until ALL tests pass
3. Do NOT move to the next step until you see "BUILD SUCCESS" and "Failures: 0, Errors: 0"
4. Show the user the final test results output (pass counts, execution time)
5. Only after all tests pass, create a work log entry with status `CURRENT-TESTS`

Update "Learnings.md" with any insights gained during debugging.

**If tests fail after 2-3 fix iterations:**
- Document the issue in Learnings.md
- Show the user the failing test output
- Use the AskUserQuestion tool to get guidance on approach
- May need to rollback and revise the plan (see Rollback Procedure below)

## 7. All Test Cases
Run all previous test cases for existing functionality, plus any previous tests that couldn't be run earlier due to missing dependencies but are now runnable. **You MUST complete a full test-fix-iteration cycle before proceeding.** This means:

1. Run `mvn test` to execute ALL test cases (previous and current)
2. If any tests fail:
   - Analyze the failure
   - Fix the bug (may be a regression in existing functionality)
   - Run `mvn test` again
   - Repeat until ALL tests pass
3. Do NOT move to the next step until you see "BUILD SUCCESS" and "Failures: 0, Errors: 0"
4. Show the user the final test results output (pass counts, execution time)
5. Only after all tests pass, create a work log entry with status `ALL-TESTS`

Update "Learnings.md" with any insights gained during regression testing.

**If tests fail after 2-3 fix iterations:**
- Document the issue in Learnings.md
- Show the user the failing test output
- Use the AskUserQuestion tool to get guidance on approach
- May need to rollback and revise the plan (see Rollback Procedure below)

## 8. Documentation
Create comprehensive documentation:
- **Code Comments**: Add standard JavaDoc/JSDoc comments for each method
- **Inline Comments**: Explain any non-obvious blocks of code
- **User Documentation**: Update PhotoSortUserDocumentation.md with what the feature does and how to use it
- **Developer Documentation**: Update PhotoSortDevDocumentation.md with implementation details, dependencies, and maintenance notes
- **Test Plan**: Add entries to TestPlan.md for manual testing scenarios (functionality covered by automated tests plus any that cannot be automated)

Create a work log entry with status `DOCS`.

## 9. Git Commit
Check in all changes to the Git repository:
- **Commit Message Format**: `Step [N]: [Step Name] - [Brief description of changes]`
  - Example: `Step 1: Database Configuration - Add PostgreSQL connection and entity models`
- **Branch Strategy**: Work on feature branches for major features, commit directly to main for small fixes
- After commit, push to remote: `git push origin [branch-name]`

# Rollback Procedure

If implementation encounters blocking issues:

1. **Document the Issue**: Add detailed entry to Learnings.md describing what went wrong
2. **Assess Impact**: Determine if partial implementation can be salvaged or needs complete rollback
3. **Rollback if Needed**:
   ```bash
   git reset --hard HEAD~1  # Undo last commit (if committed)
   git clean -fd            # Remove untracked files
   ```
4. **Consult User**: Use AskUserQuestion tool to discuss alternative approaches
5. **Revise Plan**: Create new plan addressing the issues encountered
6. **Update WorkLog**: Add entry noting the rollback and reason

# On Startup

1. Read the "WorkLog.csv" file to determine where to continue working (check the most recent entry)
2. Read the "PhotoSpecification.md" file to understand the current step's requirements
3. Read the "Learnings.md" file to refresh memory about how to improve development moving forward
4. Continue from where you left off based on the last status in WorkLog.csv:
   - `DEV` → Proceed to step 2 (Initial Testing)
   - `START-TESTS` → Proceed to step 3 (Plan Creation)
   - `PLAN-CREATE` → Proceed to step 4 (Plan Implementation)
   - `PLAN-IMPLEMENT` → Proceed to step 5 (Test Case Augmentation)
   - `TESTS-AUGMENTED` → Proceed to step 6 (Current Test Cases)
   - `CURRENT-TESTS` → Proceed to step 7 (All Test Cases)
   - `ALL-TESTS` → Proceed to step 8 (Documentation)
   - `DOCS` → Move to next step in PhotoSpecification.md
5. Read the TODO file to refresh memory about what the next steps are


