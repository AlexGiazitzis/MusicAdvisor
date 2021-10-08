package advisor.services;

import advisor.entities.Album;
import advisor.entities.Category;
import advisor.entities.Playlist;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alex Giazitzis
 */
public class ParserService {
    /**
     * Takes a JSON as {@link java.lang.String} argument and returns a {@link java.util.List} of {@link advisor.entities.Playlist}
     * by parsing the argument with {@link com.google.gson.JsonParser}.
     * @param  json - {@link java.lang.String} form of a JSON payload.
     * @return {@link java.util.List} of {@link advisor.entities.Playlist}.
     */
    public List<Playlist> getPlaylists(final String json) {
        JsonObject     body      = JsonParser.parseString(json).getAsJsonObject();
        JsonObject     playlists = body.getAsJsonObject("playlists");
        JsonArray      items     = playlists.getAsJsonArray("items");
        List<Playlist> list      = new ArrayList<>();
        for (JsonElement i : items) {
            list.add(new Playlist(i.getAsJsonObject().get("name").getAsString(),
                                  i.getAsJsonObject().get("external_urls").getAsJsonObject().get("spotify")
                                   .getAsString()));
        }
        return list;
    }

    /**
     * Takes a JSON as {@link java.lang.String} argument and returns a {@link java.util.List} of {@link advisor.entities.Category}
     * by parsing the argument with {@link com.google.gson.JsonParser}.
     * @param  json - {@link java.lang.String} form of a JSON payload.
     * @return {@link java.util.List} of {@link advisor.entities.Category}.
     */
    public List<Category> getCategories(final String json) {
        JsonObject     body       = JsonParser.parseString(json).getAsJsonObject();
        JsonObject     categories = body.getAsJsonObject("categories");
        JsonArray      items      = categories.getAsJsonArray("items");
        List<Category> list       = new ArrayList<>();
        for (JsonElement c : items) {
            list.add(new Category(c.getAsJsonObject().get("name").getAsString(),
                                  c.getAsJsonObject().get("id").getAsString()));
        }
        return list;
    }

    /**
     * Takes a JSON as {@link java.lang.String} argument and returns a {@link java.util.List} of {@link advisor.entities.Album}
     * by parsing the argument with {@link com.google.gson.JsonParser}.
     * @param  json - {@link java.lang.String} form of a JSON payload.
     * @return {@link java.util.List} of {@link advisor.entities.Album}.
     */
    public List<Album> getAlbums(final String json) {
        JsonObject  body   = JsonParser.parseString(json).getAsJsonObject();
        JsonObject  albums = body.getAsJsonObject("albums");
        JsonArray   items  = albums.getAsJsonArray("items");
        List<Album> list   = new ArrayList<>();
        for (JsonElement i : items) {
            String       name    = i.getAsJsonObject().get("name").getAsString();
            List<String> artists = new ArrayList<>();
            for (JsonElement artist : i.getAsJsonObject().get("artists").getAsJsonArray()) {
                artists.add(artist.getAsJsonObject().get("name").getAsString());
            }
            String uri = i.getAsJsonObject().get("external_urls").getAsJsonObject().get("spotify").getAsString();
            list.add(new Album(name, artists, uri));
        }
        return list;
    }

    /**
     * Scans a JSON as {@link java.lang.String} payload for error field.
     * @param  json - {@link java.lang.String} for of a JSON payload.
     * @return error message of error field.
     */
    public String getErrorMessage(final String json) {
        JsonObject body = JsonParser.parseString(json).getAsJsonObject();
        JsonObject error = body.getAsJsonObject("error");
        return error.get("message").getAsString();
    }
}
