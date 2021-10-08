package advisor.view;

/**
 * Command Line Interface view that the user sees any and all output the program has.
 * @author Alex Giazitzis
 */
public class CLI {
    /**
     * Outputs the given objects to the command line interface the user uses.
     * For better usage, override the {@link java.lang.Object#toString()} method on the entity classes passed.
     * @param output - array format of passed objects.
     * @param <T> - the class type used as a parameter type.
     */
    @SafeVarargs
    public static <T> void update(final T... output) {
        for (T t : output) {
            System.out.println(t.toString());
        }
    }
}
