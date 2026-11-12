package rushandroast.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Defines the fixed drinks and sizes available at Rush & Roast
 * and validates customer selections.
 */
public class Menu {

    private static final List<String> DRINKS = Collections.unmodifiableList(Arrays.asList(
        "Espresso",
        "Ristretto",
        "Doppio",
        "Latte",
        "Cappuccino",
        "Americano"
    ));

    private static final List<String> SIZES = Collections.unmodifiableList(Arrays.asList(
        "Small",
        "Medium",
        "Large"
    ));

    private Menu() {
        // Utility class with static accessors
    }

    /**
     * Returns the fixed list of drink choices.
     *
     * @return unmodifiable list of drink names
     */
    public static List<String> getDrinks() {
        return DRINKS;
    }

    /**
     * Returns the fixed list of size choices.
     *
     * @return unmodifiable list of size names
     */
    public static List<String> getSizes() {
        return SIZES;
    }

    /**
     * Checks if the given drink name is a valid menu item.
     *
     * @param choice the drink name to validate
     * @return true if the choice is on the menu; false if null or invalid
     */
    public static boolean isValidDrink(String choice) {
        if (choice == null) {
            return false;
        }
        for (String drink : DRINKS) {
            if (drink.equalsIgnoreCase(choice.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the given size is a valid drink size.
     *
     * @param choice the size name to validate
     * @return true if the choice is on the list; false if null or invalid
     */
    public static boolean isValidSize(String choice) {
        if (choice == null) {
            return false;
        }
        for (String size : SIZES) {
            if (size.equalsIgnoreCase(choice.trim())) {
                return true;
            }
        }
        return false;
    }
}
