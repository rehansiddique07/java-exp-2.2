import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("--- Employee Management ---");
            System.out.println("1. Add an Employee");
            System.out.println("2. Display All Employees");
            System.out.println("3. Exit");
            System.out.print("Choose an option (1-3): ");

            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1":
                    runEmployeeManagerWithArg("add");
                    break;
                case "2":
                    runEmployeeManagerWithArg("display");
                    break;
                case "3":
                    System.out.println("Exiting.");
                    sc.close();
                    return;
                default:
                    System.out.println("Invalid option, try again.");
            }
        }
    }

    private static void runEmployeeManagerWithArg(String actionArg) {
        String className = "EmployeeManager";
        try {
            ProcessBuilder pb = new ProcessBuilder(buildJavaCommand(className, actionArg));
            pb.inheritIO();
            Process p = pb.start();
            int rc = p.waitFor();
            System.out.println();
            System.out.println("[" + className + "] exited with code: " + rc);
        } catch (IOException ioe) {
            System.err.println("Could not start separate JVM for " + className + " (IO): " + ioe.getMessage());
            System.err.println("Falling back to calling " + className + ".main()");
            fallbackCall(className, new String[]{actionArg});
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted while waiting for " + className);
        } catch (Exception e) {
            System.err.println("Unexpected error launching " + className + ": " + e.getMessage());
            fallbackCall(className, new String[]{actionArg});
        }
    }

    private static String[] buildJavaCommand(String className, String arg) {
        if (arg == null || arg.isEmpty()) {
            return new String[] { "java", "-cp", ".", className };
        } else {
            return new String[] { "java", "-cp", ".", className, arg };
        }
    }

    private static void fallbackCall(String className, String[] args) {
        try {
            Class<?> cls = Class.forName(className);
            java.lang.reflect.Method m = cls.getMethod("main", String[].class);
            m.invoke(null, (Object) args);
        } catch (ClassNotFoundException cnfe) {
            System.err.println("Class not found for fallback call: " + cnfe.getMessage());
            System.err.println("Did you compile all .java files? Try: javac *.java");
        } catch (NoSuchMethodException nsme) {
            System.err.println("No main(String[] args) method found in " + className + ": " + nsme.getMessage());
        } catch (IllegalAccessException | InvocationTargetException e) {
            System.err.println("Error while invoking main of " + className + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
