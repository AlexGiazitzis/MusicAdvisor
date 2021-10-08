package advisor.entities;

/**
 * @author Alex Giazitzis
 */
public class Playlist {
    private final String name;
    private final String url;

    public Playlist(final String name, final String url) {
        this.name = name;
        this.url = url;
    }

    @Override
    public String toString() {
        return name + "\n" + url + "\n";
    }
}
