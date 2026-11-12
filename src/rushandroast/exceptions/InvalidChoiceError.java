package rushandroast.exceptions;

/**
 * Thrown when raw input cannot be parsed into a valid numeric choice within range.
 */
public class InvalidChoiceError extends RuntimeException {
    public InvalidChoiceError(String message) {
        super(message);
    }
}
