package rushandroast.model;

import java.util.ArrayList;
import java.util.List;
import rushandroast.exceptions.EmptyQueueError;
import rushandroast.exceptions.InvalidOrderError;

/**
 * Double-ended queue implementation that manages customer drink orders.
 * Supports priority order insertion (FIFO among priority orders ahead of regular orders)
 * using an internal LinkedStack, and records completed orders.
 */
public class Deque {

    private static class Node {
        DrinkOrder data;
        Node next;
        Node prev;

        Node(DrinkOrder data, Node next, Node prev) {
            this.data = data;
            this.next = next;
            this.prev = prev;
        }
    }

    private Node firstNode;
    private Node lastNode;
    private int size;
    private final List<DrinkOrder> completed;

    public Deque() {
        this.firstNode = null;
        this.lastNode = null;
        this.size = 0;
        this.completed = new ArrayList<>();
    }

    /**
     * Enqueues an order. If the order has priority status, it is inserted
     * ahead of all non-priority orders while preserving FIFO order with respect
     * to other priority orders. Otherwise, it is added to the back.
     *
     * @param order the DrinkOrder to enqueue
     * @throws InvalidOrderError if order is null
     */
    public void enqueue(DrinkOrder order) {
        if (order == null) {
            throw new InvalidOrderError("Cannot enqueue a null order.");
        }

        if (order.isPriority()) {
            LinkedStack<DrinkOrder> priorityTemp = new LinkedStack<>();

            // Temporarily peel off existing priority orders from the front
            while (firstNode != null && firstNode.data.isPriority()) {
                priorityTemp.push(removeFirstInternal());
            }

            // Place the newly arrived priority order at the front of the remaining line
            addFirstInternal(order);

            // Re-insert earlier priority orders back in their original sequence
            while (!priorityTemp.isEmpty()) {
                addFirstInternal(priorityTemp.pop());
            }
        } else {
            addLastInternal(order);
        }
    }

    /**
     * Removes and returns the next order to be served, recording it in the completed list.
     *
     * @return the served DrinkOrder
     * @throws EmptyQueueError if the queue is empty
     */
    public DrinkOrder dequeue() {
        if (isEmpty()) {
            throw new EmptyQueueError("Cannot dequeue from an empty queue.");
        }
        DrinkOrder order = removeFirstInternal();
        completed.add(order);
        return order;
    }

    /**
     * Inspects the next order to be served without removing it.
     *
     * @return the front DrinkOrder
     * @throws EmptyQueueError if the queue is empty
     */
    public DrinkOrder peek() {
        if (isEmpty()) {
            throw new EmptyQueueError("Cannot peek into an empty queue.");
        }
        return firstNode.data;
    }

    /**
     * Checks whether any orders are currently waiting.
     *
     * @return true if no orders are waiting; false otherwise
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the number of waiting orders.
     *
     * @return waiting order count
     */
    public int waitingCount() {
        return size;
    }

    /**
     * Returns the total number of orders completed.
     *
     * @return completed order count
     */
    public int completedCount() {
        return completed.size();
    }

    /**
     * Returns a safe, independent snapshot copy of all waiting orders, front to back.
     * Mutating the returned list does not affect the internal queue.
     *
     * @return snapshot copy of waiting orders
     */
    public List<DrinkOrder> getWaiting() {
        List<DrinkOrder> snapshot = new ArrayList<>(size);
        Node curr = firstNode;
        while (curr != null) {
            snapshot.add(curr.data);
            curr = curr.next;
        }
        return snapshot;
    }

    /**
     * Returns a safe, independent snapshot copy of all completed orders in completion order.
     * Mutating the returned list does not affect the internal completed history.
     *
     * @return snapshot copy of completed orders
     */
    public List<DrinkOrder> getCompleted() {
        return new ArrayList<>(completed);
    }

    // --- Private internal node manipulation helpers ---

    private void addFirstInternal(DrinkOrder order) {
        Node newNode = new Node(order, firstNode, null);
        if (firstNode != null) {
            firstNode.prev = newNode;
        }
        firstNode = newNode;
        if (lastNode == null) {
            lastNode = newNode;
        }
        size++;
    }

    private void addLastInternal(DrinkOrder order) {
        Node newNode = new Node(order, null, lastNode);
        if (lastNode != null) {
            lastNode.next = newNode;
        }
        lastNode = newNode;
        if (firstNode == null) {
            firstNode = newNode;
        }
        size++;
    }

    private DrinkOrder removeFirstInternal() {
        if (firstNode == null) {
            return null;
        }
        Node target = firstNode;
        firstNode = firstNode.next;
        if (firstNode != null) {
            firstNode.prev = null;
        } else {
            lastNode = null;
        }
        size--;
        return target.data;
    }
}
