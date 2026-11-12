package rushandroast.exceptions;

/**
 * Thrown when an order contains invalid data (blank name, name > 50 characters,
 * unavailable drink, or unavailable size).
 */
public class InvalidOrderError extends RuntimeException {
    public InvalidOrderError(String message) {
        super(message);
    }
}
