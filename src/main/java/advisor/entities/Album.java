package advisor.entities;

import java.util.Arrays;
import java.util.List;

/**
 * @author Alex Giazitzis
 */
public class Album {
    private final String       name;
    private final List<String> artists;
    private final String uri;

    public Album(final String name, final List<String> artists, final String uri) {
        this.name = name;
        this.artists = artists;
        this.uri = uri;
    }

    @Override
    public String toString() {
        return name + "\n" + Arrays.toString(artists.toArray()) + "\n" + uri + "\n";
    }
}
