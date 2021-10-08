package advisor.utils;

import advisor.Main;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.ResourceBundle;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author Alex Giazitzis
 */
public class Utils {
    /**
     * Builds the required URI for the start of the OAuth process.
     */
    public static final Function<Void, String>                  urlBuilder;

    /**
     * Builds the required POST {@link java.net.http.HttpRequest}
     * in order to retrieve the access code for the user.
     */
    public static final Function<String, HttpRequest>           authCodeRequest;

    /**
     * Parses the JSON payload of the response to the POST request
     * the application makes and retrieves the access code.
     */
    public static final Function<String, String>                authCodeRetriever;

    /**
     * Builds a GET {@link java.net.http.HttpRequest} with the use of two {@link java.lang.String} params,
     * the first being the user access code and
     * the second being endpoint of Spotify's REST API.
     */
    public static final BiFunction<String, String, HttpRequest> getRequest;

    /**
     * Builds a GET {@link java.net.http.HttpRequest} with the use of two {@link java.lang.String} params,
     * the first being the user access code and
     * the second being the category ID retrieved from Spotify's categories.
     */
    public static final BiFunction<String, String, HttpRequest> getPlaylistRequest;

    static {

        urlBuilder = unused -> {
            ResourceBundle keys = ResourceBundle.getBundle("keys");
            return Main.accessPoint +
                   "/authorize?client_id=" +
                   keys.getString("client_id") +
                   "&response_type=code&redirect_uri=http://localhost:" + keys.getString("port");
        };

        authCodeRequest = responseQuery -> {
            String code = responseQuery.substring(responseQuery.indexOf("=") + 1);

            ResourceBundle keys = ResourceBundle.getBundle("keys");
            String authHeader =
                    " Basic " + Base64.getUrlEncoder().encodeToString(
                            (keys.getString("client_id") + ":" + keys.getString("client_secret"))
                                    .getBytes(StandardCharsets.UTF_8));
            String payload = "grant_type=authorization_code&code=" +
                             code +
                             "&redirect_uri=http://localhost:" + keys.getString("port");

            return HttpRequest.newBuilder()
                              .headers("Authorization", authHeader,
                                       "Content-Type", "application/x-www-form-urlencoded")
                              .uri(URI.create(Main.accessPoint + "/api/token"))
                              .POST(HttpRequest.BodyPublishers.ofString(payload))
                              .build();
        };

        authCodeRetriever = responseBody -> {
            JsonObject auth = JsonParser.parseString(responseBody).getAsJsonObject();
            return auth.get("access_token").getAsString();
        };

        getRequest = (authToken, endpoint) -> HttpRequest.newBuilder()
                                                         .headers("Authorization", "Bearer " + authToken,
                                                                  "Content-Type", "application/x-www-form-urlencoded")
                                                         .uri(URI.create(Main.resourcePoint + "/v1/browse/" + endpoint))
                                                         .GET()
                                                         .build();

        getPlaylistRequest = (authToken, categoryId) -> HttpRequest.newBuilder()
                                                                   .header("Authorization", "Bearer " + authToken)
                                                                   .uri(URI.create(Main.resourcePoint +
                                                                                   "/v1/browse/categories/" +
                                                                                   categoryId + "/playlists"))
                                                                   .GET()
                                                                   .build();
    }
}
