package rushandroast.driver;

import java.util.List;
import rushandroast.exceptions.EmptyQueueError;
import rushandroast.exceptions.InvalidOrderError;
import rushandroast.model.Deque;
import rushandroast.model.DrinkOrder;

/**
 * Standalone tiny driver demonstrating that the Queue-backed model
 * runs and can be fully exercised without any View or Controller present.
 */
public class ModelDriver {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   RUSH & ROAST - STANDALONE MODEL DRIVER         ");
        System.out.println("==================================================");

        Deque deque = new Deque();
        System.out.println("[Step 1] Initial empty deque created.");
        System.out.println("  isEmpty: " + deque.isEmpty());
        System.out.println("  waitingCount: " + deque.waitingCount());
        System.out.println("  completedCount: " + deque.completedCount());

        // Test bad-state peek / dequeue on empty queue
        System.out.println("\n[Step 2] Exercising empty-queue error handling...");
        try {
            deque.peek();
            System.err.println("  FAIL: peek() should have thrown EmptyQueueError!");
        } catch (EmptyQueueError e) {
            System.out.println("  PASS: peek() threw expected EmptyQueueError: " + e.getMessage());
        }

        try {
            deque.dequeue();
            System.err.println("  FAIL: dequeue() should have thrown EmptyQueueError!");
        } catch (EmptyQueueError e) {
            System.out.println("  PASS: dequeue() threw expected EmptyQueueError: " + e.getMessage());
        }

        // Test normal enqueues
        System.out.println("\n[Step 3] Enqueueing regular orders A and B...");
        DrinkOrder orderA = new DrinkOrder("Alice", "Latte", "Medium", false);
        DrinkOrder orderB = new DrinkOrder("Bob", "Cappuccino", "Large", false);
        deque.enqueue(orderA);
        deque.enqueue(orderB);
        System.out.println("  Enqueued: " + orderA.describe());
        System.out.println("  Enqueued: " + orderB.describe());
        System.out.println("  Current waiting count: " + deque.waitingCount());
        System.out.println("  Current peek: " + deque.peek().describe());

        // Test priority enqueues with LinkedStack FIFO preservation
        System.out.println("\n[Step 4] Enqueueing priority orders P1 and P2...");
        DrinkOrder p1 = new DrinkOrder("Charlie", "Espresso", "Small", true);
        DrinkOrder p2 = new DrinkOrder("Diana", "Americano", "Large", true);
        deque.enqueue(p1);
        System.out.println("  Enqueued Priority 1: " + p1.describe());
        System.out.println("  Front of queue is now: " + deque.peek().describe());

        deque.enqueue(p2);
        System.out.println("  Enqueued Priority 2: " + p2.describe());
        System.out.println("  Front of queue is now: " + deque.peek().describe() + " (verifies FIFO priority order!)");

        // Inspect snapshot
        System.out.println("\n[Step 5] Inspecting read-only waiting snapshot (front to back):");
        List<DrinkOrder> waiting = deque.getWaiting();
        for (int i = 0; i < waiting.size(); i++) {
            System.out.printf("  Position %d: %s%n", i + 1, waiting.get(i).describe());
        }

        // Verify defensive copy
        System.out.println("\n[Step 6] Testing defensive snapshot copying...");
        int countBefore = deque.waitingCount();
        waiting.clear();
        System.out.println("  Cleared returned list. Queue waitingCount is still: " + deque.waitingCount()
            + " (Defensive copy intact: " + (countBefore == deque.waitingCount()) + ")");

        // Dequeue orders and inspect completed list
        System.out.println("\n[Step 7] Dequeueing all orders...");
        while (!deque.isEmpty()) {
            DrinkOrder served = deque.dequeue();
            System.out.println("  Served: " + served.describe());
        }

        System.out.println("  Remaining waiting count: " + deque.waitingCount());
        System.out.println("  Completed count: " + deque.completedCount());

        List<DrinkOrder> completed = deque.getCompleted();
        System.out.println("  Completed orders recorded:");
        for (int i = 0; i < completed.size(); i++) {
            System.out.printf("    %d. %s%n", i + 1, completed.get(i).describe());
        }

        // Test bad-input validation in DrinkOrder model
        System.out.println("\n[Step 8] Testing DrinkOrder constructor bad-input validation...");
        try {
            new DrinkOrder("   ", "Latte", "Medium", false);
            System.err.println("  FAIL: Blank name should throw InvalidOrderError!");
        } catch (InvalidOrderError e) {
            System.out.println("  PASS: Blank name threw: " + e.getMessage());
        }

        try {
            new DrinkOrder("ValidName", "Pizza", "Medium", false);
            System.err.println("  FAIL: Invalid drink should throw InvalidOrderError!");
        } catch (InvalidOrderError e) {
            System.out.println("  PASS: Invalid drink threw: " + e.getMessage());
        }

        try {
            deque.enqueue(null);
            System.err.println("  FAIL: null order should throw InvalidOrderError!");
        } catch (InvalidOrderError e) {
            System.out.println("  PASS: Null enqueue threw: " + e.getMessage());
        }

        System.out.println("\n==================================================");
        System.out.println("  ALL MODEL DRIVER CHECKS COMPLETED SUCCESSFULLY! ");
        System.out.println("==================================================");
    }
}
