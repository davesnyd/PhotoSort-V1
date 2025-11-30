Create the application in an iterative fashion. The specification is in "PhotoSpecification.md". Implement one step at a time. Each step will include: Number, name, description, components involved, list of initial test cases.

Maintain a file named "WorkLog.csv". It will contain the following columns: Date, Time, Step Number, Step Name, Status. Date will be of the format: YYYY.mm.DD. Time will be of the format: HH.MM (with hours in 24 hour format). Status will be one of: {Start development, Test cases initiated, Plan created, Plan implemented, Test cases augmented, Current test cases succeeded, All test cases successful, Documentation complete}

General programming approaches: 
1. Optimize for readability and robustness, not cleverness.
2. Focus on not breaking currently working functionality.
3. When planning and implementing, review the code base for methods that can be reused or extended without breaking existing functionality.
4. For each file created, add a copyright notice assigned to David Snyderman

New functionality implementation steps:
1. Create a work log entry for start development
2. Review the initial test cases for the step, implement them, confirm that they fail; create a work log entry for "Test cases initiated"
3. Format a plan. Start a separate agent to review it and make sure that it is correct, does not break existing functionality, and suggest improvements; incorporate those improvements in the plan. Consult with me, describe the plan, highlight any forseen problems, and receive authorization to proceed. Then, create a work log entry for "Plan created".
4. Implement the plan. Start a separate agent to review the implementation, assess for potential conflict with existing functionality and suggest changes to decrease the likelihood of conflict, make suggestions to improve robustness, readability, and performance, and implement those changes. Create work log entry for "Plan Implemented". Update the "PhotoSpecification.md" document to reflect any changes made as part of the implementation. Update the "Learnings.md" document with any information that would help improve the development process in the future.
5. Review the functionality created and determine additional test cases to create. As much as possible, all functionality should be tested in an automated fashion. Create a work log entry for "Test cases augmented".
6. Run the test cases that can be run with the current development level, fix bugs that occur, and proceed iteratively until all test cases have succeeded. Create a work log entry for "Current test cases succeeded". Update the "Learnings.md" document with any information that would help improve the development process in the future.
7. Run all previous test cases for existing functionality; and any previous tests that hadn't been run because functionality was not yet available but that has now been created. Fix bugs that occur, and proceed iteratively until all test cases-- previous and current-- have succeeded. Create a work log entry for "All test cases succeeded". Update the "Learnings.md" document with any information that would help improve the development process in the future.
8. Create documentation: standard comments in code for each method, comments in each method to explain any non-clear blocks of code, a "user document" that explains what the code is doing and how to run it, a "developer documentation" that describes what the code is, how it works, what it relies on, and anything else that a developer would need to understand and fix the code. Create entries in a test plan: spot testing of the functionality that is in the automated tests plus testing of any functionality that you aren't able to create an automated test for. When done, create a work log entry for "Documentation complete"
9. Check in all changes to the GIT repository.

On startup:
1. Read the WorkLog.csv file to determine where to continue working
2. Read the "PhotoSpecification.md" file to determine parameters of the current development implementation
3. Continue where you left off


