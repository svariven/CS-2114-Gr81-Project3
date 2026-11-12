package rushandroast.view;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;
import rushandroast.model.DrinkOrder;

/**
 * Text-based console view for Rush & Roast.
 * Strictly presents output and captures raw user input without
 * knowing about the underlying Deque, LinkedStack, or domain validation rules.
 */
public class ConsoleView {

    private final Scanner scanner;
    private final PrintStream out;

    public ConsoleView() {
        this(System.in, System.out);
    }

    public ConsoleView(InputStream in, PrintStream out) {
        this.scanner = new Scanner(in);
        this.out = out;
    }

    public ConsoleView(Scanner scanner, PrintStream out) {
        this.scanner = scanner;
        this.out = out;
    }

    /**
     * Prints the primary application menu options.
     */
    public void showMainMenu() {
        out.println();
        out.println("==========================================");
        out.println("          RUSH & ROAST COFFEE BAR         ");
        out.println("==========================================");
        out.println("1. Add order");
        out.println("2. View next order");
        out.println("3. Complete next order");
        out.println("4. View completed orders");
        out.println("5. View counts");
        out.println("6. Quit");
        out.println("==========================================");
    }

    /**
     * Prints the available drink options as a numbered list.
     *
     * @param choices list of drink names
     */
    public void showDrinkMenu(List<String> choices) {
        out.println("Select a Drink:");
        for (int i = 0; i < choices.size(); i++) {
            out.printf("  %d. %s%n", i + 1, choices.get(i));
        }
    }

    /**
     * Prints the available size options as a numbered list.
     *
     * @param choices list of size names
     */
    public void showSizeMenu(List<String> choices) {
        out.println("Select a Size:");
        for (int i = 0; i < choices.size(); i++) {
            out.printf("  %d. %s%n", i + 1, choices.get(i));
        }
    }

    /**
     * Prints a prompt and captures raw text entered by the user.
     *
     * @param message prompt message
     * @return raw line typed by user
     */
    public String prompt(String message) {
        out.print(message);
        if (scanner.hasNextLine()) {
            return scanner.nextLine();
        }
        return "";
    }

    /**
     * Displays a single order.
     *
     * @param order the order to display
     */
    public void showOrder(DrinkOrder order) {
        if (order == null) {
            out.println("No order details available.");
        } else {
            out.println("Next order: " + order.describe());
        }
    }

    /**
     * Displays a numbered list of orders under a given header.
     *
     * @param title  header title
     * @param orders list of orders
     */
    public void showOrderList(String title, List<DrinkOrder> orders) {
        out.println();
        out.println(title);
        if (orders == null || orders.isEmpty()) {
            out.println("  (None)");
            return;
        }
        for (int i = 0; i < orders.size(); i++) {
            out.printf("  %d. %s%n", i + 1, orders.get(i).describe());
        }
    }

    /**
     * Displays waiting and completed counts.
     *
     * @param waiting   number of orders waiting
     * @param completed number of orders completed
     */
    public void showCounts(int waiting, int completed) {
        out.println();
        out.println("Order Counts:");
        out.printf("  Waiting orders:   %d%n", waiting);
        out.printf("  Completed orders: %d%n", completed);
    }

    /**
     * Displays a standard informational message.
     *
     * @param text message text
     */
    public void showMessage(String text) {
        out.println(text);
    }

    /**
     * Displays an error message to the user.
     *
     * @param text error message text
     */
    public void showError(String text) {
        out.println("[Error] " + text);
    }
}
