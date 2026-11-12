# Rush & Roast: Coffee Bar Queue Management System
**Project 3 — Deliverable 3: First Prototype**  
**Course:** Software Design & Data Structures  
**Repository / Workspace:** `rush-and-roast`  

---

## 1. System Overview

Rush & Roast is a queue management system for a high-volume coffee bar. The core business challenge is balancing FIFO fairness for standard patrons while supporting expedited priority processing for rush orders.

In this first prototype (Deliverable 3), our primary architectural milestone is establishing a **strictly decoupled Model slice** backed by a custom double-ended queue (`Deque`) and generic stack (`LinkedStack`), completely exercisable through standalone drivers and a comprehensive JUnit test suite without requiring any user interface. In addition, a functional thin console view (`ConsoleView`) operating through a dedicated controller (`CafeController`) is provided to verify end-to-end MVP workflows and bad-input resilience.

```
                    +--------------------+
                    |   RushAndRoastApp  | (Bootstrap / Main)
                    +---------+----------+
                              |
              +---------------+---------------+
              |                               |
              v                               v
    +--------------------+          +--------------------+
    |   CafeController   |--------->|    ConsoleView     | (View)
    +---------+----------+          +--------------------+
              |
              v (Information via public getters)
    +--------------------+
    |       Deque        | (Model Core)
    +----+----------+----+
         |          |
         v          v
  +------------+  +--------------------+
  | DrinkOrder |  |  LinkedStack<T>    | (Priority FIFO Staging)
  +-----+------+  +--------------------+
        |
        v
  +------------+
  |    Menu    |
  +------------+
```

---

## 2. How to Run

### Prerequisites
- Java Development Kit (JDK 17, 20, or higher)
- Included JUnit 4 libraries inside `lib/`: `junit-4.13.2.jar` and `hamcrest-core-1.3.jar`

### A. Compilation
From the project root directory (`rush-and-roast/`), compile all model, view, controller, driver, and test classes into the `bin/` directory:

**Windows (Command Prompt):**
```cmd
if not exist bin mkdir bin
javac -cp .;lib/* -d bin src\rushandroast\exceptions\*.java src\rushandroast\model\*.java src\rushandroast\view\*.java src\rushandroast\controller\*.java src\rushandroast\driver\*.java src\rushandroast\*.java test\rushandroast\test\*.java
```

**macOS / Linux:**
```bash
mkdir -p bin
javac -cp ".:lib/*" -d bin src/rushandroast/exceptions/*.java src/rushandroast/model/*.java src/rushandroast/view/*.java src/rushandroast/controller/*.java src/rushandroast/driver/*.java src/rushandroast/*.java test/rushandroast/test/*.java
```

---

### B. Running the Standalone Model Driver
To verify that the Queue-backed model stands on its own and executes all core operations with **zero view or console dependencies**:

**Windows:**
```cmd
java -cp bin rushandroast.driver.ModelDriver
```

**macOS / Linux:**
```bash
java -cp bin rushandroast.driver.ModelDriver
```

*What the driver exercises:*
1. Instantiates an empty `Deque` and validates initial counters.
2. Exercises empty-queue error handling (`EmptyQueueError` on `peek()` and `dequeue()`).
3. Enqueues regular orders and verifies FIFO sequence.
4. Enqueues priority orders and demonstrates that existing priority orders maintain arrival FIFO order ahead of regular orders.
5. Obtains safe snapshots via `getWaiting()` and verifies that mutating the snapshot list does not alter the queue.
6. Dequeues orders, verifying completion history tracking via `getCompleted()`.
7. Tests domain validation in `DrinkOrder` (blank name, oversized name, invalid menu items, null enqueue).

---

### C. Running the Automated Unit Test Suite (T1 – T25)
The test suite implements all 25 test cases defined in the project specification.

**Option 1: Using the Standalone Console Test Runner (zero dependencies):**
```cmd
java -cp "bin;lib/*" rushandroast.test.ModelTestRunner
```

**Option 2: Using standard JUnit Core:**
```cmd
java -cp "bin;lib/*" org.junit.runner.JUnitCore rushandroast.test.ModelTest
```

*(Note for Linux/macOS: replace semicolon `;` with colon `:` in the classpath).*

---

### D. Running the Thin View / Application
To interact with the MVP console interface running through the controller:

**Windows:**
```cmd
java -cp bin rushandroast.RushAndRoastApp
```

**macOS / Linux:**
```bash
java -cp bin rushandroast.RushAndRoastApp
```

*Interactive Commands:*
- `1. Add order` — step-by-step prompts for customer name, drink selection (1–6), size (1–3), and priority status (y/n).
- `2. View next order` — peeks at the front order without removing it.
- `3. Complete next order` — serves and dequeues the next order, archiving it into the completed history.
- `4. View completed orders` — displays all fulfilled orders in completion order.
- `5. View counts` — shows current waiting count and completed count.
- `6. Quit` — gracefully terminates the session.

---

## 3. Reflection

### What Changed From Your Specification?
During the implementation of Deliverable 3, several architectural details were refined. Below is each discrepancy between the Deliverable 2 Specification and what was built, traced directly to the prompt or issue that caused it:

1. **Priority Ordering Reconciliation (Test T3 vs. Test T25 & Revised Scope #5):**
   - *What the Spec Said:* In the Deliverable 2 Spec Test Plan table (Section 8, Test T3), the setup `Queue [A]; enqueue P, P2` listed the expected result as `peek() is P2; getWaiting() is [P2, P, A]`. Conversely, Section 1 (Revised Scope #5) and Test T25 stated that a `LinkedStack` must be used to preserve FIFO fairness among priority customers so that `peek() is P1` and earlier priority orders are served first (`[P1, P2, A]`).
   - *What We Built:* We implemented the fair FIFO priority algorithm using `LinkedStack` as specified in Revised Scope #5 and verified by Test T25. When a new priority order arrives, existing priority orders at the head of the deque are popped onto a temporary `LinkedStack`, the new priority order is inserted, and the existing priority orders are pushed back onto the front in their original sequence.
   - *What Prompted It:* Traced directly to customer fairness analysis. If priority orders operated as a pure LIFO stack, a customer arriving 10 minutes later with a priority tag would cut in front of an earlier priority patron who had already been waiting. Implementing the stack-assisted restoration resolves the internal contradiction in the spec table in favor of fairness.

2. **Exception Hierarchy and Package Modernization:**
   - *What the Spec Said:* The spec listed custom exceptions `InvalidOrderError`, `InvalidChoiceError`, and `EmptyQueueError` without specifying whether they checked or unchecked exceptions.
   - *What We Built:* Created a dedicated `rushandroast.exceptions` package where all three exceptions inherit from `java.lang.RuntimeException` (unchecked).
   - *What Prompted It:* In an interactive console loop, requiring checked exception declarations across every method signature cluttered the controller dispatch and model interfaces. Unchecked exceptions allow clean boundary catching: `CafeController` catches `InvalidChoiceError` at the menu prompt and catches `EmptyQueueError` when dispatching peek/dequeue actions, while unit tests can cleanly assert exception throws without boilerplate.

3. **ConsoleView Input & Output Stream Decoupling:**
   - *What the Spec Said:* `ConsoleView` specified standard console methods reading directly from `System.in` and printing to `System.out`.
   - *What We Built:* Added overloaded constructors to `ConsoleView` allowing arbitrary `InputStream`, `Scanner`, and `PrintStream` injection (`public ConsoleView(InputStream in, PrintStream out)`).
   - *What Prompted It:* Automated end-to-end integration testing. By allowing stream injection, we were able to run simulated user sessions with complex strings of invalid and valid commands (piped input) without requiring manual terminal typing, ensuring reproducible verification.

4. **Defensive Snapshot Deep Copying:**
   - *What the Spec Said:* `getWaiting()` and `getCompleted()` return a `List<DrinkOrder>` copy.
   - *What We Built:* In `Deque.java`, `getWaiting()` traverses the doubly linked nodes and creates a completely new `ArrayList<DrinkOrder>`, and `getCompleted()` creates a `new ArrayList<>(completed)`.
   - *What Prompted It:* Verified by Tests T16 and T18: if an external caller or view called `.clear()` or `.remove(0)` on the returned list, returning a view or raw reference would have corrupted the queue. The defensive snapshot guarantees absolute isolation.

---

### Did the Model Really Stand Alone?
**Yes, completely.**
- **Zero Cross-Layer Imports:** The model classes (`Deque`, `DrinkOrder`, `Menu`, `LinkedStack`) contain zero references to `ConsoleView`, `CafeController`, `Scanner`, `System.out`, or any UI constructs.
- **Independent Executability:** The model logic runs in full isolation. Both `rushandroast.driver.ModelDriver` and `rushandroast.test.ModelTest` execute every queue method, priority rule, and invariant check without instantiating any view or controller.
- **Strict Boundary Integrity:** The controller queries the model using only public getters (`waitingCount()`, `completedCount()`, `getCompleted()`, `peek()`, `dequeue()`). The view never sees the internal doubly linked node structure or the `LinkedStack`, and receives only immutable `DrinkOrder` objects and primitive data. Nothing had to be moved across the model/view line to make the system run.

---

### What Happened to Your Unknowns?
In the initial project scope, three primary unknowns were identified:
1. *Unknown 1: How to view waiting orders without mutating the queue.*  
   **Status:** **Resolved.** Implemented `getWaiting()` in `Deque`, which traverses internal linked nodes from `firstNode` to `lastNode` and copies them into a detached list. The view can display the entire waiting line without removing a single element.
2. *Unknown 2: How to handle multiple priority orders fairly without an expensive array sort.*  
   **Status:** **Resolved.** Using `LinkedStack<DrinkOrder>` during `enqueue()`, earlier priority orders are temporarily peeled off ($O(k)$ where $k$ is the number of active priority orders, typically very small), the new order is placed, and the stack pops them back in original order.
3. *Unknown 3: How to catch careless user input without polluting the model with parsing logic.*  
   **Status:** **Resolved.** Separated input format validation from domain invariant validation: `CafeController.parseChoice()` validates numeric ranges and non-numeric strings, while `DrinkOrder` constructor enforces core business rules (name lengths, fixed menu selections).

*Remaining Open Unknowns for Later Milestones:*
- **Persistence Across Sessions (Deliverable 4/5):** How to serialize the queue state and completed order history to a local file so state survives program termination.
- **Multi-Terminal Synchronization:** If multiple barista stations serve from the same queue, thread-safe synchronization will need to be explored.

---

### What Doesn't Work Yet?
- **Current MVP Status:** All core MVP features in scope for Deliverable 3 are **100% operational**. All 25 unit tests pass, and the application survives all invalid input cases (blank names, names > 50 chars, invalid drinks, invalid sizes, out-of-range menu commands, non-numeric input, and empty-queue operations).
- **Deferred Features (Targeted for Deliverables 4 & 5):**
  - Order cancellation / line exit before fulfillment.
  - Disk persistence of order logs.
  - Real-time wait duration estimation based on beverage preparation complexity (our planned change feature).

---

## 4. Next Steps

### What is Left to Finish the MVP?
In order of execution:
1. **Order Timestamps & Receipt Details:** Add creation and completion timestamps to `DrinkOrder` to measure elapsed turnaround time.
2. **Persistent Session Logging:** Implement file I/O to append completed orders to an `orders_history.csv` file upon dequeue.
3. **Queue Reordering / Cancellation:** Allow a customer to cancel an order by name or ticket number if they need to leave the line.
4. **Enhanced Console Formatting:** Format order lists with tabular alignment and clear divider lines.

---

### Non-Trivial Change Feature for Deliverable 4
**Feature Name:** *Dynamic Preparation Time & Estimated Wait Time (EWT) Forecaster*

#### Feature Description:
Each beverage item has an intrinsic preparation time based on barista labor (e.g., Espresso: 1 min, Ristretto: 1 min, Doppio: 2 min, Americano: 2 min, Latte: 4 min, Cappuccino: 4 min). The system dynamically calculates and tracks the estimated wait time for each order in the queue. When an order is added, its confirmation displays its predicted wait time. Users can also select a new main-menu option (`6. View Estimated Wait Times`) to view a breakdown of every waiting customer's projected wait time and the total back-of-house queue backlog.

#### Why This Feature is Non-Trivial:
- **Adds Model State & Logic:** Introduces preparation time data to `Menu`, calculates cumulative wait times based on queue positioning, and dynamically recomputes wait times when a priority order is inserted ahead of standard orders.
- **Accessible End-to-End:** Reached through a new controller command and rendered by the view, with immediate feedback during order placement.

#### Architectural Impact Prediction (Method-by-Method):
To satisfy the Deliverable 4 boundary verification guidelines, we analyzed how this feature touches our architecture:

1. **Classes and Specific Methods It WILL Touch:**
   - **Model Layer:**
     - `rushandroast.model.Menu`:
       - Add `public static int getPreparationTime(String drink)`: returns standard preparation time in minutes.
     - `rushandroast.model.DrinkOrder`:
       - Add field `private final int preparationMinutes;`
       - Add getter `public int getPreparationMinutes()`
       - Modify `public String describe()`: appends preparation time display (e.g., `Ana - Large Latte (PRIORITY) [Prep: 4m]`).
     - `rushandroast.model.Deque`:
       - Add `public int getTotalQueueWaitTime()`: iterates waiting orders and sums preparation minutes.
       - Add `public int getEstimatedWaitTimeForOrder(DrinkOrder target)`: computes cumulative wait time of all orders positioned ahead of the target plus its own prep time.
   - **Controller Layer:**
     - `rushandroast.controller.CafeController`:
       - Update `run()`: expand command range to `parseChoice(..., 1, 7)`.
       - Update `handleAddOrder()`: calculate and report the newly added order's estimated wait time upon enqueue.
       - Add `public void handleWaitTimes()`: dispatches wait-time calculation and passes data to view.
   - **View Layer:**
     - `rushandroast.view.ConsoleView`:
       - Update `showMainMenu()`: add option `6. View estimated wait times` (shifting `7. Quit`).
       - Add `public void showWaitTimeReport(List<DrinkOrder> orders, List<Integer> waitTimes, int totalWait)`: formats the projected schedule for waiting patrons.

2. **Classes Our Design Says Should NOT Need to Change:**
   - `rushandroast.model.LinkedStack`: The internal stack data structure remains entirely agnostic of preparation times; its sole responsibility remains order reversal for priority staging.
   - `rushandroast.exceptions.*`: All three exception classes (`EmptyQueueError`, `InvalidOrderError`, `InvalidChoiceError`) remain completely unchanged.

---

### Stretch Goals
- **Committed:**
  - *Dynamic Preparation Time & Wait Time Estimation* (committed as our non-trivial Deliverable 4 change feature).
  - *File-based Session Archival* (committed for Deliverable 4/5).
- **Dropped:**
  - *Graphical User Interface (GUI via Swing/JavaFX):* Dropped to maintain strict adherence to modular CLI MVC separation and avoid unnecessary window-toolkit overhead.
  - *Multi-Client Socket Networking:* Dropped to focus resources on core algorithmic robustness and data structures rather than network transport protocols.

---

### New Tests Needed for Deliverable 4
1. `testMenu_GetPreparationTime_ValidAndInvalid`: tests prep time retrieval for all drinks and handles unknown drink lookup.
2. `testDrinkOrder_PrepTimeAssignment`: verifies correct preparation time assignment during `DrinkOrder` instantiation.
3. `testDeque_TotalQueueWaitTime_Empty`: verifies that an empty queue returns 0 total wait time.
4. `testDeque_TotalQueueWaitTime_MultipleOrders`: verifies correct cumulative sum across mixed drinks.
5. `testDeque_PriorityOrderPreemption_UpdatesWaitTimes`: verifies that inserting a priority order updates the wait times for all subsequent regular orders in line.
6. `testCafeController_WaitTimeMenuCommand`: tests menu option 6 dispatch and handles empty queue display gracefully.

---

### Division of Work for Next Deliverable
| Team Member | Role | Deliverable 4 Ownership |
| :--- | :--- | :--- |
| **Hemanth** | Model Specialist | Implement `getPreparationTime()` in `Menu`, prep time logic in `DrinkOrder`, and cumulative wait calculations in `Deque`. Author unit tests for wait times. |
| **Aidan** | View Specialist | Update `showMainMenu()` in `ConsoleView` and implement `showWaitTimeReport()`. |
| **Talal** | Controller Specialist | Implement `handleWaitTimes()` in `CafeController`, update `run()` menu dispatch, and wire wait-time reporting into `handleAddOrder()`. |
| **Richard** | QA & Spec Lead | Update architecture diagram, perform regression testing across T1–T25, and lead GenAI boundary audit. |

---

## 5. Work Attribution & GenAI Note

### Attribution Table
| Class / File | Primary Owner(s) | Role & Contribution |
| :--- | :--- | :--- |
| `rushandroast/model/DrinkOrder.java` | Hemanth, Richard | Wrote immutable model class; implemented strict field validation, getters, and `describe()`. GenAI-assisted. |
| `rushandroast/model/Menu.java` | Hemanth | Wrote fixed menu lists (`DRINKS`, `SIZES`), validation methods (`isValidDrink`, `isValidSize`). |
| `rushandroast/model/LinkedStack.java` | Hemanth, Talal | Wrote custom generic singly-linked stack implementation (`push`, `pop`, `peek`, `isEmpty`, `size`). Reviewed and tested. |
| `rushandroast/model/Deque.java` | Hemanth | Wrote double-ended linked queue with stack-based priority insertion, defensive copy snapshotting (`getWaiting`, `getCompleted`), and completed history tracking. |
| `rushandroast/exceptions/*.java` | Richard, Talal | Wrote custom unchecked runtime exceptions (`InvalidOrderError`, `InvalidChoiceError`, `EmptyQueueError`). |
| `rushandroast/view/ConsoleView.java` | Aidan | Wrote text console view; formatted menus, order listings, counts, prompts, and decoupled stream constructor for automated testing. |
| `rushandroast/controller/CafeController.java` | Talal | Wrote interactive application loop, `parseChoice` range validation, robust field re-prompting, exception catching, and model-to-view delegation. |
| `rushandroast/driver/ModelDriver.java` | Hemanth, Talal | Wrote standalone driver demonstrating full model execution, priority ordering, and error handling without view/UI. |
| `rushandroast/RushAndRoastApp.java` | Talal, Aidan | Wrote main application wiring connecting Model, View, and Controller into a runnable system. |
| `rushandroast/test/ModelTest.java` | Richard, Talal, Hemanth | Wrote comprehensive JUnit 4 test suite implementing all 25 specification test cases (T1–T25). |
| `rushandroast/test/ModelTestRunner.java` | Talal | Wrote standalone reflection-based test runner for execution in environments without external test runners. |
| `README.md` | Richard, Talal, Everyone | Authored system overview, run guides, detailed reflection tracing, Deliverable 4 next steps plan, attribution table, and GenAI disclosure. |

---

### GenAI Note
In compliance with course guidelines:
- **Usage of GenAI:** Claude and Gemini were utilized as pair-programming assistants to review test edge cases, refine markdown documentation formatting, and evaluate model/view boundary decoupling.
- **GenAI Tip Boundary Check (as advised in assignment instructions):**  
  Before finalizing our proposed Deliverable 4 change feature (*Dynamic Preparation Time & Wait Time Forecaster*), we consulted GenAI with our class responsibilities:
  > *"We plan to add an Estimated Wait Time calculation based on drink prep times. Given our current classes (`DrinkOrder`, `Menu`, `Deque`, `LinkedStack`, `ConsoleView`, `CafeController`), which classes would this feature force us to edit, and does any calculation logic cross the model/view boundary?"*
  
  **GenAI Analysis & Resolution:**  
  GenAI confirmed that drink preparation durations and cumulative wait-time math belong strictly in the Model layer (`Menu`, `DrinkOrder`, `Deque`). The View (`ConsoleView`) receives only calculated numerical summaries and formats them, while the Controller (`CafeController`) handles user command routing. Because the View does not perform queue math and the Model does not format text or know about console output, the proposed feature maintains a clean model/view boundary.
- **Verification:** All generated code and documentation was thoroughly reviewed, compiled, and tested locally by team members. We take full responsibility for the accuracy and integrity of this submission.
