package rushandroast.model;

import rushandroast.exceptions.EmptyQueueError;

/**
 * Standard generic singly-linked stack implementation used to manage
 * order staging, such as preserving FIFO ordering during priority insertion.
 *
 * @param <T> element type stored in the stack
 */
public class LinkedStack<T> {

    private static class Node<E> {
        E data;
        Node<E> next;

        Node(E data, Node<E> next) {
            this.data = data;
            this.next = next;
        }
    }

    private Node<T> top;
    private int size;

    public LinkedStack() {
        this.top = null;
        this.size = 0;
    }

    /**
     * Pushes an item onto the top of the stack.
     *
     * @param item the element to push
     */
    public void push(T item) {
        top = new Node<>(item, top);
        size++;
    }

    /**
     * Removes and returns the item from the top of the stack.
     *
     * @return the top item
     * @throws EmptyQueueError if the stack is empty
     */
    public T pop() {
        if (isEmpty()) {
            throw new EmptyQueueError("Cannot pop from an empty stack.");
        }
        T item = top.data;
        top = top.next;
        size--;
        return item;
    }

    /**
     * Returns the item at the top of the stack without removing it.
     *
     * @return the top item
     * @throws EmptyQueueError if the stack is empty
     */
    public T peek() {
        if (isEmpty()) {
            throw new EmptyQueueError("Cannot peek into an empty stack.");
        }
        return top.data;
    }

    /**
     * Checks if the stack has no elements.
     *
     * @return true if empty; false otherwise
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the number of elements in the stack.
     *
     * @return current stack size
     */
    public int size() {
        return size;
    }
}
