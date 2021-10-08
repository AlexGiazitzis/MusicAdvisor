package advisor.entities;

/**
 * @author Alex Giazitzis
 */
public class Category {
    private final String name;
    private final String id;

    public Category(final String name, final String id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }
}
