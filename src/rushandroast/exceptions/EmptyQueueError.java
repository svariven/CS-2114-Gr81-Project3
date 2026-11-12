package rushandroast.exceptions;

/**
 * Thrown when an operation is attempted on an empty queue or stack.
 */
public class EmptyQueueError extends RuntimeException {
    public EmptyQueueError() {
        super("Queue is empty.");
    }

    public EmptyQueueError(String message) {
        super(message);
    }
}
