package rushandroast;

import rushandroast.controller.CafeController;
import rushandroast.model.Deque;
import rushandroast.view.ConsoleView;

/**
 * Main application entry point for Rush & Roast.
 * Wires together Model, View, and Controller and launches the console application.
 */
public class RushAndRoastApp {

    public static void main(String[] args) {
        Deque deque = new Deque();
        ConsoleView view = new ConsoleView();
        CafeController controller = new CafeController(deque, view);

        controller.run();
    }
}
