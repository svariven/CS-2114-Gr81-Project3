package rushandroast.controller;

import java.util.List;
import rushandroast.exceptions.EmptyQueueError;
import rushandroast.exceptions.InvalidChoiceError;
import rushandroast.exceptions.InvalidOrderError;
import rushandroast.model.Deque;
import rushandroast.model.DrinkOrder;
import rushandroast.model.Menu;
import rushandroast.view.ConsoleView;

/**
 * Controller connecting ConsoleView and the Deque model.
 * Directs user interactions, translates raw input, handles exceptions,
 * and maintains the lifecycle of the application loop.
 */
public class CafeController {

    private final Deque deque;
    private final ConsoleView view;
    private boolean running;

    public CafeController(Deque deque, ConsoleView view) {
        this.deque = deque;
        this.view = view;
        this.running = false;
    }

    /**
     * Executes the main interactive console loop until the user chooses Quit (6).
     */
    public void run() {
        running = true;
        while (running) {
            view.showMainMenu();
            String rawChoice = view.prompt("Enter command (1-6): ");
            try {
                int command = parseChoice(rawChoice, 1, 6);
                switch (command) {
                    case 1:
                        handleAddOrder();
                        break;
                    case 2:
                        handlePeek();
                        break;
                    case 3:
                        handleComplete();
                        break;
                    case 4:
                        handleCompleted();
                        break;
                    case 5:
                        handleCounts();
                        break;
                    case 6:
                        running = false;
                        view.showMessage("Thank you for using Rush & Roast. Goodbye!");
                        break;
                    default:
                        view.showError("Unexpected command.");
                }
            } catch (InvalidChoiceError e) {
                view.showError(e.getMessage());
            }
        }
    }

    /**
     * Parses a string into an integer within the inclusive range [min, max].
     *
     * @param text raw input string
     * @param min  minimum allowed integer
     * @param max  maximum allowed integer
     * @return parsed integer
     * @throws InvalidChoiceError if the string is non-numeric, empty, or out-of-range
     */
    public int parseChoice(String text, int min, int max) {
        if (text == null || text.trim().isEmpty()) {
            throw new InvalidChoiceError("Input cannot be empty.");
        }
        try {
            int value = Integer.parseInt(text.trim());
            if (value < min || value > max) {
                throw new InvalidChoiceError(
                    String.format("Selection out of range: must be between %d and %d.", min, max));
            }
            return value;
        } catch (NumberFormatException e) {
            throw new InvalidChoiceError("Invalid number format. Please enter digits only.");
        }
    }

    /**
     * Prompts the user step-by-step for customer name, drink, size, and priority.
     * Re-prompts each field on invalid input.
     */
    public void handleAddOrder() {
        String name = "";
        while (true) {
            name = view.prompt("Enter customer name (1-50 characters): ");
            if (name == null || name.trim().isEmpty()) {
                view.showError("Customer name cannot be empty or whitespace.");
                continue;
            }
            if (name.trim().length() > 50) {
                view.showError("Customer name cannot exceed 50 characters.");
                continue;
            }
            name = name.trim();
            break;
        }

        List<String> drinks = Menu.getDrinks();
        view.showDrinkMenu(drinks);
        String selectedDrink = "";
        while (true) {
            String choiceStr = view.prompt(String.format("Select drink (1-%d): ", drinks.size()));
            try {
                int choice = parseChoice(choiceStr, 1, drinks.size());
                selectedDrink = drinks.get(choice - 1);
                break;
            } catch (InvalidChoiceError e) {
                view.showError(e.getMessage());
            }
        }

        List<String> sizes = Menu.getSizes();
        view.showSizeMenu(sizes);
        String selectedSize = "";
        while (true) {
            String choiceStr = view.prompt(String.format("Select size (1-%d): ", sizes.size()));
            try {
                int choice = parseChoice(choiceStr, 1, sizes.size());
                selectedSize = sizes.get(choice - 1);
                break;
            } catch (InvalidChoiceError e) {
                view.showError(e.getMessage());
            }
        }

        boolean isPriority = false;
        while (true) {
            String prioStr = view.prompt("Is this a priority order? (y/n): ");
            if (prioStr == null || prioStr.trim().isEmpty()) {
                view.showError("Priority choice cannot be empty. Please enter 'y' or 'n'.");
                continue;
            }
            String cleaned = prioStr.trim().toLowerCase();
            if (cleaned.equals("y") || cleaned.equals("yes")) {
                isPriority = true;
                break;
            } else if (cleaned.equals("n") || cleaned.equals("no")) {
                isPriority = false;
                break;
            } else {
                view.showError("Invalid entry. Please enter 'y' for yes or 'n' for no.");
            }
        }

        try {
            DrinkOrder order = new DrinkOrder(name, selectedDrink, selectedSize, isPriority);
            deque.enqueue(order);
            view.showMessage("Order added successfully: " + order.describe());
        } catch (InvalidOrderError e) {
            view.showError("Failed to create order: " + e.getMessage());
        }
    }

    /**
     * Views the next waiting order without removing it.
     */
    public void handlePeek() {
        try {
            DrinkOrder next = deque.peek();
            view.showOrder(next);
        } catch (EmptyQueueError e) {
            view.showMessage("No orders waiting.");
        }
    }

    /**
     * Completes and dequeues the next waiting order.
     */
    public void handleComplete() {
        try {
            DrinkOrder completed = deque.dequeue();
            view.showMessage("Completed order: " + completed.describe());
        } catch (EmptyQueueError e) {
            view.showMessage("No orders waiting.");
        }
    }

    /**
     * Displays all orders that have been completed during this session.
     */
    public void handleCompleted() {
        List<DrinkOrder> completedList = deque.getCompleted();
        if (completedList.isEmpty()) {
            view.showMessage("Nothing completed yet.");
        } else {
            view.showOrderList("Completed Orders:", completedList);
        }
    }

    /**
     * Displays current counts for waiting and completed orders.
     */
    public void handleCounts() {
        view.showCounts(deque.waitingCount(), deque.completedCount());
    }
}
