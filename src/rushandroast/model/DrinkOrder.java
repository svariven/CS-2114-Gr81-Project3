package rushandroast.model;

import java.util.Objects;
import rushandroast.exceptions.InvalidOrderError;

/**
 * Holds immutable data for one customer's drink order and enforces domain validation.
 * Refuses to exist if data is invalid.
 */
public class DrinkOrder {

    private final String customerName;
    private final String drink;
    private final String size;
    private final boolean isPriority;

    /**
     * Constructs a validated DrinkOrder.
     *
     * @param customerName Customer name (trimmed, 1-50 characters)
     * @param drink        Drink name (must exist on Menu)
     * @param size         Drink size (must exist on Menu)
     * @param isPriority   True if order has priority rush status
     * @throws InvalidOrderError if any field fails validation
     */
    public DrinkOrder(String customerName, String drink, String size, boolean isPriority) {
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new InvalidOrderError("Customer name cannot be empty or whitespace.");
        }

        String trimmedName = customerName.trim();
        if (trimmedName.length() > 50) {
            throw new InvalidOrderError("Customer name cannot exceed 50 characters.");
        }

        if (!Menu.isValidDrink(drink)) {
            throw new InvalidOrderError("Unavailable drink: " + drink);
        }

        if (!Menu.isValidSize(size)) {
            throw new InvalidOrderError("Unavailable size: " + size);
        }

        this.customerName = trimmedName;
        this.drink = drink.trim();
        this.size = size.trim();
        this.isPriority = isPriority;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getDrink() {
        return drink;
    }

    public String getSize() {
        return size;
    }

    public boolean isPriority() {
        return isPriority;
    }

    /**
     * Formats the order into a concise one-line description.
     * Example: "Ana - Large Latte (PRIORITY)" or "Bob - Small Espresso"
     *
     * @return single-line description of the order
     */
    public String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append(customerName).append(" - ").append(size).append(" ").append(drink);
        if (isPriority) {
            sb.append(" (PRIORITY)");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return describe();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DrinkOrder that = (DrinkOrder) o;
        return isPriority == that.isPriority &&
            customerName.equalsIgnoreCase(that.customerName) &&
            drink.equalsIgnoreCase(that.drink) &&
            size.equalsIgnoreCase(that.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerName.toLowerCase(), drink.toLowerCase(), size.toLowerCase(), isPriority);
    }
}
