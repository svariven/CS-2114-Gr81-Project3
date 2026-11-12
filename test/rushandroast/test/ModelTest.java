package rushandroast.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import rushandroast.controller.CafeController;
import rushandroast.exceptions.EmptyQueueError;
import rushandroast.exceptions.InvalidChoiceError;
import rushandroast.exceptions.InvalidOrderError;
import rushandroast.model.Deque;
import rushandroast.model.DrinkOrder;
import rushandroast.view.ConsoleView;

/**
 * Formal JUnit test suite implementing the complete Test Plan (T1 - T25)
 * defined in Section 8 of the Rush & Roast specification.
 */
public class ModelTest {

    private Deque deque;
    private DrinkOrder orderA;
    private DrinkOrder orderB;
    private DrinkOrder orderP;
    private DrinkOrder orderP1;
    private DrinkOrder orderP2;
    private CafeController controller;

    @Before
    public void setUp() {
        deque = new Deque();
        orderA = new DrinkOrder("Alice", "Latte", "Medium", false);
        orderB = new DrinkOrder("Bob", "Cappuccino", "Large", false);
        orderP = new DrinkOrder("Perry", "Espresso", "Small", true);
        orderP1 = new DrinkOrder("Paula", "Doppio", "Small", true);
        orderP2 = new DrinkOrder("Peter", "Ristretto", "Medium", true);
        controller = new CafeController(deque, new ConsoleView());
    }

    /**
     * T1: enqueue normal (non-priority)
     * Setup: Empty queue; enqueue A, then B
     * Expected: peek() is A; waitingCount() is 2; getWaiting() is [A, B]
     */
    @Test
    public void testT01_EnqueueNormalNonPriority() {
        deque.enqueue(orderA);
        deque.enqueue(orderB);
        assertEquals(orderA, deque.peek());
        assertEquals(2, deque.waitingCount());
        List<DrinkOrder> waiting = deque.getWaiting();
        assertEquals(2, waiting.size());
        assertEquals(orderA, waiting.get(0));
        assertEquals(orderB, waiting.get(1));
    }

    /**
     * T2: enqueue normal (priority)
     * Setup: Queue [A]; enqueue P
     * Expected: peek() is P; getWaiting() is [P, A]
     */
    @Test
    public void testT02_EnqueuePriorityAheadOfRegular() {
        deque.enqueue(orderA);
        deque.enqueue(orderP);
        assertEquals(orderP, deque.peek());
        List<DrinkOrder> waiting = deque.getWaiting();
        assertEquals(2, waiting.size());
        assertEquals(orderP, waiting.get(0));
        assertEquals(orderA, waiting.get(1));
    }

    /**
     * T3: enqueue multiple priority orders (FIFO priority behavior)
     * Setup: Queue [A]; enqueue P1, P2
     * Expected: peek() is P1; getWaiting() is [P1, P2, A]
     */
    @Test
    public void testT03_EnqueueMultiplePriorityOrdersPreservesFIFO() {
        deque.enqueue(orderA);
        deque.enqueue(orderP1);
        deque.enqueue(orderP2);
        assertEquals(orderP1, deque.peek());
        List<DrinkOrder> waiting = deque.getWaiting();
        assertEquals(3, waiting.size());
        assertEquals(orderP1, waiting.get(0));
        assertEquals(orderP2, waiting.get(1));
        assertEquals(orderA, waiting.get(2));
    }

    /**
     * T4: enqueue bad input (null order)
     * Setup: enqueue(null)
     * Expected: InvalidOrderError; waitingCount() unchanged
     */
    @Test
    public void testT04_EnqueueNullRaisesInvalidOrderError() {
        try {
            deque.enqueue(null);
            fail("Expected InvalidOrderError when enqueueing null.");
        } catch (InvalidOrderError e) {
            // expected
        }
        assertEquals(0, deque.waitingCount());
    }

    /**
     * T5: dequeue normal
     * Setup: Queue [A, B]; dequeue
     * Expected: Returns A; waiting is [B]; completed is [A]; completedCount() is 1
     */
    @Test
    public void testT05_DequeueNormal() {
        deque.enqueue(orderA);
        deque.enqueue(orderB);
        DrinkOrder served = deque.dequeue();
        assertEquals(orderA, served);
        assertEquals(1, deque.waitingCount());
        assertEquals(orderB, deque.peek());
        assertEquals(1, deque.completedCount());
        List<DrinkOrder> completed = deque.getCompleted();
        assertEquals(1, completed.size());
        assertEquals(orderA, completed.get(0));
    }

    /**
     * T6: dequeue bad input (empty queue 3 times)
     * Setup: New queue; dequeue 3 times
     * Expected: EmptyQueueError each time; completedCount() stays 0
     */
    @Test
    public void testT06_DequeueEmptyQueueRepeatedly() {
        for (int i = 0; i < 3; i++) {
            try {
                deque.dequeue();
                fail("Expected EmptyQueueError on empty dequeue attempt " + (i + 1));
            } catch (EmptyQueueError e) {
                // expected
            }
        }
        assertEquals(0, deque.completedCount());
        assertEquals(0, deque.waitingCount());
    }

    /**
     * T7: peek normal
     * Setup: Queue [A, B]; peek twice
     * Expected: Returns A both times; waitingCount() still 2
     */
    @Test
    public void testT07_PeekNormal() {
        deque.enqueue(orderA);
        deque.enqueue(orderB);
        assertEquals(orderA, deque.peek());
        assertEquals(orderA, deque.peek());
        assertEquals(2, deque.waitingCount());
    }

    /**
     * T8: peek bad input (empty queue)
     * Setup: New queue; peek
     * Expected: EmptyQueueError; nothing changes
     */
    @Test
    public void testT08_PeekEmptyQueue() {
        try {
            deque.peek();
            fail("Expected EmptyQueueError on empty peek.");
        } catch (EmptyQueueError e) {
            // expected
        }
        assertEquals(0, deque.waitingCount());
    }

    /**
     * T9: isEmpty normal
     * Setup: New queue (true); then enqueue A (false)
     */
    @Test
    public void testT09_IsEmptyNormal() {
        assertTrue(deque.isEmpty());
        deque.enqueue(orderA);
        assertFalse(deque.isEmpty());
    }

    /**
     * T10: isEmpty edge (enqueue A, dequeue A)
     * Setup: Enqueue A, dequeue A -> true again
     */
    @Test
    public void testT10_IsEmptyEdgeAfterDequeue() {
        deque.enqueue(orderA);
        assertFalse(deque.isEmpty());
        deque.dequeue();
        assertTrue(deque.isEmpty());
    }

    /**
     * T11: waitingCount normal
     * Setup: Enqueue 3 orders -> 3
     */
    @Test
    public void testT11_WaitingCountNormal() {
        deque.enqueue(orderA);
        deque.enqueue(orderB);
        deque.enqueue(orderP);
        assertEquals(3, deque.waitingCount());
    }

    /**
     * T12: waitingCount bad-state
     * Setup: Failed dequeue/peek on empty queue -> Still 0
     */
    @Test
    public void testT12_WaitingCountBadStateOnEmpty() {
        try {
            deque.dequeue();
        } catch (EmptyQueueError ignored) { }
        try {
            deque.peek();
        } catch (EmptyQueueError ignored) { }
        assertEquals(0, deque.waitingCount());
    }

    /**
     * T13: completedCount normal
     * Setup: Enqueue 2, dequeue 2 -> 2
     */
    @Test
    public void testT13_CompletedCountNormal() {
        deque.enqueue(orderA);
        deque.enqueue(orderB);
        deque.dequeue();
        deque.dequeue();
        assertEquals(2, deque.completedCount());
    }

    /**
     * T14: completedCount bad-state
     * Setup: Dequeue on empty queue -> Still 0
     */
    @Test
    public void testT14_CompletedCountBadStateOnEmpty() {
        try {
            deque.dequeue();
        } catch (EmptyQueueError ignored) { }
        assertEquals(0, deque.completedCount());
    }

    /**
     * T15: getWaiting normal
     * Setup: Queue [P, A, B] -> Returns [P, A, B] in that order; queue not changed
     */
    @Test
    public void testT15_GetWaitingOrderPreserved() {
        deque.enqueue(orderA);
        deque.enqueue(orderB);
        deque.enqueue(orderP);
        List<DrinkOrder> waiting = deque.getWaiting();
        assertEquals(3, waiting.size());
        assertEquals(orderP, waiting.get(0));
        assertEquals(orderA, waiting.get(1));
        assertEquals(orderB, waiting.get(2));
        assertEquals(3, deque.waitingCount());
    }

    /**
     * T16: getWaiting bad input (misuse of result)
     * Setup: Remove an item from the returned list -> waitingCount() and queue order unchanged
     */
    @Test
    public void testT16_GetWaitingDefensiveCopy() {
        deque.enqueue(orderA);
        deque.enqueue(orderB);
        List<DrinkOrder> snapshot = deque.getWaiting();
        assertEquals(2, snapshot.size());
        snapshot.remove(0);
        snapshot.clear();
        assertEquals(2, deque.waitingCount());
        assertEquals(orderA, deque.peek());
    }

    /**
     * T17: getCompleted normal
     * Setup: Dequeue A then B -> Returns [A, B]
     */
    @Test
    public void testT17_GetCompletedNormal() {
        deque.enqueue(orderA);
        deque.enqueue(orderB);
        deque.dequeue();
        deque.dequeue();
        List<DrinkOrder> completed = deque.getCompleted();
        assertEquals(2, completed.size());
        assertEquals(orderA, completed.get(0));
        assertEquals(orderB, completed.get(1));
    }

    /**
     * T18: getCompleted bad input (misuse of result)
     * Setup: Clear the returned list -> completedCount() unchanged
     */
    @Test
    public void testT18_GetCompletedDefensiveCopy() {
        deque.enqueue(orderA);
        deque.dequeue();
        List<DrinkOrder> completed = deque.getCompleted();
        assertEquals(1, completed.size());
        completed.clear();
        assertEquals(1, deque.completedCount());
        assertEquals(1, deque.getCompleted().size());
    }

    /**
     * T19: DrinkOrder normal construction & getters
     * Setup: ("Ana", "Latte", "Large", false)
     * Expected: Object created; getters return the same values; describe matches
     */
    @Test
    public void testT19_DrinkOrderNormal() {
        DrinkOrder order = new DrinkOrder("Ana", "Latte", "Large", false);
        assertNotNull(order);
        assertEquals("Ana", order.getCustomerName());
        assertEquals("Latte", order.getDrink());
        assertEquals("Large", order.getSize());
        assertFalse(order.isPriority());
        assertEquals("Ana - Large Latte", order.describe());
    }

    /**
     * T20: DrinkOrder blank name
     * Setup: "" and "   "
     * Expected: InvalidOrderError for both
     */
    @Test
    public void testT20_DrinkOrderBlankName() {
        try {
            new DrinkOrder("", "Latte", "Large", false);
            fail("Expected InvalidOrderError for empty name.");
        } catch (InvalidOrderError e) {
            // expected
        }
        try {
            new DrinkOrder("    ", "Latte", "Large", false);
            fail("Expected InvalidOrderError for whitespace name.");
        } catch (InvalidOrderError e) {
            // expected
        }
    }

    /**
     * T21: DrinkOrder name length boundary
     * Setup: 50-character name (accepted), 51-character name (raises InvalidOrderError)
     */
    @Test
    public void testT21_DrinkOrderNameLengthBoundary() {
        StringBuilder name50 = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            name50.append("a");
        }
        DrinkOrder valid = new DrinkOrder(name50.toString(), "Latte", "Large", false);
        assertEquals(50, valid.getCustomerName().length());

        String name51 = name50.toString() + "b";
        try {
            new DrinkOrder(name51, "Latte", "Large", false);
            fail("Expected InvalidOrderError for 51-character name.");
        } catch (InvalidOrderError e) {
            // expected
        }
    }

    /**
     * T22: DrinkOrder unavailable drink / size
     * Setup: drink "Pizza"; size "Huge"
     * Expected: InvalidOrderError for each
     */
    @Test
    public void testT22_DrinkOrderUnavailableDrinkOrSize() {
        try {
            new DrinkOrder("Sam", "Pizza", "Large", false);
            fail("Expected InvalidOrderError for invalid drink Pizza.");
        } catch (InvalidOrderError e) {
            // expected
        }
        try {
            new DrinkOrder("Sam", "Latte", "Huge", false);
            fail("Expected InvalidOrderError for invalid size Huge.");
        } catch (InvalidOrderError e) {
            // expected
        }
    }

    /**
     * T23: parseChoice normal
     * Setup: ("3", 1, 6)
     * Expected: 3
     */
    @Test
    public void testT23_ParseChoiceNormal() {
        int choice = controller.parseChoice("3", 1, 6);
        assertEquals(3, choice);
    }

    /**
     * T24: parseChoice bad input
     * Setup: "abc", "", "0", "7"
     * Expected: InvalidChoiceError for each
     */
    @Test
    public void testT24_ParseChoiceBadInput() {
        String[] badInputs = {"abc", "", "   ", "0", "7", "-1"};
        for (String bad : badInputs) {
            try {
                controller.parseChoice(bad, 1, 6);
                fail("Expected InvalidChoiceError for input: " + bad);
            } catch (InvalidChoiceError e) {
                // expected
            }
        }
    }

    /**
     * T25: Two priority orders documented behavior (Section 1, #5)
     * Setup: Enqueue P1, then P2
     * Expected: peek() is P1 (first priority order is served first)
     */
    @Test
    public void testT25_TwoPriorityOrdersFIFOResolution() {
        deque.enqueue(orderP1);
        deque.enqueue(orderP2);
        assertEquals(orderP1, deque.peek());
        List<DrinkOrder> waiting = deque.getWaiting();
        assertEquals(2, waiting.size());
        assertEquals(orderP1, waiting.get(0));
        assertEquals(orderP2, waiting.get(1));
    }
}
