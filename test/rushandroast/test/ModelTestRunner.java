package rushandroast.test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Command-line runner that executes all JUnit tests in ModelTest and reports
 * clear, colorized PASS/FAIL status for each test T1 - T25.
 */
public class ModelTestRunner {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("      RUSH & ROAST - MODEL TEST SUITE (T1-T25)    ");
        System.out.println("==================================================");

        Class<?> testClass = ModelTest.class;
        List<Method> testMethods = new ArrayList<>();
        Method beforeMethod = null;

        for (Method method : testClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Before.class)) {
                beforeMethod = method;
            }
            if (method.isAnnotationPresent(Test.class)) {
                testMethods.add(method);
            }
        }

        Collections.sort(testMethods, Comparator.comparing(Method::getName));

        int passed = 0;
        int failed = 0;

        for (Method test : testMethods) {
            String name = test.getName();
            try {
                Object instance = testClass.getDeclaredConstructor().newInstance();
                if (beforeMethod != null) {
                    beforeMethod.invoke(instance);
                }
                test.invoke(instance);
                System.out.printf("[PASS] %-45s OK%n", name);
                passed++;
            } catch (Throwable t) {
                Throwable cause = (t.getCause() != null) ? t.getCause() : t;
                System.out.printf("[FAIL] %-45s FAILED: %s%n", name, cause.getMessage());
                cause.printStackTrace(System.out);
                failed++;
            }
        }

        System.out.println("--------------------------------------------------");
        System.out.printf("Total tests: %d | Passed: %d | Failed: %d%n",
            (passed + failed), passed, failed);
        System.out.println("==================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
