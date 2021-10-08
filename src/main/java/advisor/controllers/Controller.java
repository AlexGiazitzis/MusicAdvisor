package advisor.controllers;

import advisor.Main;
import advisor.entities.Album;
import advisor.entities.Category;
import advisor.entities.Playlist;
import advisor.services.ParserService;
import advisor.utils.HttpHandlerWithLatch;
import advisor.utils.Utils;
import advisor.view.CLI;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

/**
 * Controls the flow of the program by processing the user's input
 * and updating the program's View.
 *
 * @see advisor.view.CLI
 * @author Alex Giazitzis
 */
public class Controller {
    private static final Scanner        scanner    = new Scanner(System.in);
    private static final ParserService  service    = new ParserService();
    private static       boolean        authed     = false;
    private static       String         authCode;
    private static final CountDownLatch latch      = new CountDownLatch(1);
    private static final HttpClient     client     = HttpClient.newBuilder().build();
    private static       Action         lastAction;
    private static       String         playlist;
    private static       int            offset     = 0;
    private static final List<Album>    albums     = new ArrayList<>();
    private static final List<Category> categories = new ArrayList<>();
    private static final List<Playlist> playlists  = new ArrayList<>();

    /**
     * Handles the user's input based on implemented commands.
     * @return true if the user wants to exit, false otherwise.
     */
    public static boolean handleUserAction() {
        String[] input = scanner.nextLine().split("\\s");

        switch (input[0]) {
            case "auth":
                handleAuth();
                return false;
            case "exit":
                CLI.update("---GOODBYE!---");
                return true;
            default:
                handle(input);
                return false;
        }

    }

    private static void handleAuth() {
        HttpServer server;
        try {
            server = HttpServer.create();
        } catch (IOException e) {
            CLI.update("Could not create HTTP Server.");
            e.printStackTrace();
            return;
        }
        try {
            server.bind(new InetSocketAddress(8080), 0);
        } catch (BindException e) {
            CLI.update("HTTP Server could not bind on specified port. Port is most likely in use.");
            return;
        } catch (IOException e) {
            CLI.update("Unable to connect.");
            return;
        }
        server.createContext("/", new HttpHandlerWithLatch(latch));

        CLI.update("use this link to request the access code:");
        CLI.update(Utils.urlBuilder.apply(null));
        CLI.update("waiting for code...");

        server.start();
        try {
            latch.await();
        } catch (IllegalMonitorStateException e) {
            CLI.update("Current thread not owning monitor.");
            e.printStackTrace();
            return;
        } catch (InterruptedException e) {
            CLI.update("Thread interrupted before latch countdown.");
            e.printStackTrace();
            return;
        }

        String query = HttpHandlerWithLatch.query;

        if (query.contains("error=")) {
            CLI.update("Authorization failed. Try again.");
            //CLI.update(query);
            return;
        }

        CLI.update("code received");
        server.stop(1);
        CLI.update("Making http request for access_token...");

        String responseBody;
        try {
            responseBody = client.send(Utils.authCodeRequest.apply(query), HttpResponse.BodyHandlers.ofString()).body();
        } catch (IOException e) {
            CLI.update("Could not send/receive request/response.");
            e.printStackTrace();
            return;
        } catch (InterruptedException e) {
            CLI.update("Client was interrupted.");
            e.printStackTrace();
            return;
        }

        CLI.update("Success!");
        authed = true;
        authCode = Utils.authCodeRetriever.apply(responseBody);
    }

    private static void handle(final String[] input) {
        if (authed) {
            switch (input[0]) {
                case "featured":
                    showFeatured();
                    return;
                case "new":
                    showNew();
                    return;
                case "categories":
                    showCategories();
                    return;
                case "playlists":
                    showPlaylists(input);
                    return;
                case "next":
                    next();
                    return;
                case "prev":
                    prev();
                    return;
                default:
                    CLI.update("Invalid option inputted. Try again.");
                    return;
            }
        }
        noAuthMessage();
    }

    private static void showFeatured() {

        HttpResponse<String> featuredList;
        try {
            featuredList = client.send(Utils.getRequest.apply(authCode, "featured-playlists"),
                                       HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            CLI.update("Could not send/receive request/response.");
            e.printStackTrace();
            return;
        } catch (InterruptedException e) {
            CLI.update("Client was interrupted.");
            e.printStackTrace();
            return;
        }

        if (getPlaylistOutput(featuredList)) return;

        lastAction = Action.FEATURED;
    }

    private static void showNew() {
        if (albums.isEmpty()) {
            HttpResponse<String> newList;
            try {
                newList = client.send(Utils.getRequest.apply(authCode, "new-releases"),
                                      HttpResponse.BodyHandlers.ofString());
            } catch (IOException e) {
                CLI.update("Could not send/receive request/response.");
                e.printStackTrace();
                return;
            } catch (InterruptedException e) {
                CLI.update("Client was interrupted.");
                e.printStackTrace();
                return;
            }

            if (hasError(newList)) {
                CLI.update(service.getErrorMessage(newList.body()));
                return;
            }

            albums.addAll(service.getAlbums(newList.body()));
        }
        CLI.update(albums.subList(offset, offset + Main.pageSize));
        CLI.update("---PAGE " + (offset / Main.pageSize + 1) + " OF " + (albums.size() % Main.pageSize == 0
                                                                         ? albums.size() / Main.pageSize
                                                                         : albums.size() / Main.pageSize + 1) + "---");

        lastAction = Action.NEW;
    }

    private static void showCategories() {
        if (categories.isEmpty()) {
            getCategories();
        }
        CLI.update(categories.subList(offset, offset + Main.pageSize).toArray());
        CLI.update("---PAGE " + (offset / Main.pageSize + 1) + " OF " + (categories.size() % Main.pageSize == 0
                                                                         ? categories.size() / Main.pageSize
                                                                         : categories.size() / Main.pageSize + 1) +
                   "---");

        lastAction = Action.CATEGORIES;
    }

    private static void getCategories() {
        HttpResponse<String> categoriesList;
        try {
            categoriesList = client.send(Utils.getRequest.apply(authCode, "categories"),
                                         HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            CLI.update("Could not send/receive request/response.");
            e.printStackTrace();
            return;
        } catch (InterruptedException e) {
            CLI.update("Client was interrupted.");
            e.printStackTrace();
            return;
        }

        if (hasError(categoriesList)) {
            CLI.update(service.getErrorMessage(categoriesList.body()));
            return;
        }
        categories.addAll(service.getCategories(categoriesList.body()));
    }

    private static void showPlaylists(final String[] input) {
        if (categories.isEmpty()) {
            getCategories();
        }
        playlist = String.join(" ", input).trim().replace("playlists ", "");
        String categoryId = categories.stream()
                                      .filter(c -> c.getName().equalsIgnoreCase(playlist))
                                      .findFirst()
                                      .orElse(new Category("none", "none"))
                                      .getId();

        HttpResponse<String> playlistList;
        try {
            playlistList = client.send(Utils.getPlaylistRequest.apply(authCode, categoryId),
                                       HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            CLI.update("Could not send/receive request/response.");
            e.printStackTrace();
            return;
        } catch (InterruptedException e) {
            CLI.update("Client was interrupted.");
            e.printStackTrace();
            return;
        }

        if (getPlaylistOutput(playlistList)) return;

        lastAction = Action.PLAYLISTS;
    }

    private static boolean getPlaylistOutput(final HttpResponse<String> playlistList) {
        if (hasError(playlistList)) {
            CLI.update(service.getErrorMessage(playlistList.body()));
            return true;
        }
        playlists.clear();
        playlists.addAll(service.getPlaylists(playlistList.body()));
        CLI.update(playlists.subList(offset, offset + Main.pageSize));
        CLI.update("---PAGE " + (offset / Main.pageSize + 1) + " OF " + (playlists.size() % Main.pageSize == 0
                                                                         ? playlists.size() / Main.pageSize
                                                                         : playlists.size() / Main.pageSize + 1) +
                   "---");
        return false;
    }

    private static void next() {
        switch (lastAction) {
            case FEATURED:
                if (checkOutOfBound(Action.FEATURED)) {
                    CLI.update("No more pages.");
                    return;
                }
                modifyOffset(1);
                showFeatured();
                return;
            case NEW:
                if (checkOutOfBound(Action.NEW)) {
                    CLI.update("No more pages.");
                    return;
                }
                modifyOffset(1);
                showNew();
                return;
            case CATEGORIES:
                if (checkOutOfBound(Action.CATEGORIES)) {
                    CLI.update("No more pages.");
                    return;
                }
                modifyOffset(1);
                showCategories();
                return;
            case PLAYLISTS:
                if (checkOutOfBound(Action.FEATURED)) {
                    CLI.update("No more pages.");
                    return;
                }
                modifyOffset(1);
                showPlaylists(new String[]{playlist});
                return;
            default:
                CLI.update("No pages to show.");
        }
    }

    private static void prev() {
        if (offset == 0) {
            CLI.update("No more pages.");
            return;
        }
        modifyOffset(-1);
        switch (lastAction) {
            case FEATURED:
                showFeatured();
                return;
            case NEW:
                showNew();
                return;
            case CATEGORIES:
                showCategories();
                return;
            case PLAYLISTS:
                showPlaylists(new String[]{playlist});
        }
    }

    private static boolean checkOutOfBound(final Action action) {
        switch (action) {
            case NEW:
                return offset + Main.pageSize == albums.size();
            case CATEGORIES:
                return offset + Main.pageSize == categories.size();
            case FEATURED:
            case PLAYLISTS:
                return offset + Main.pageSize == playlists.size();
            default:
                return false;
        }
    }

    private static void modifyOffset(final int control) {
        switch (control) {
            case 0:
                offset = 0;
                return;
            case 1:
                offset += Main.pageSize;
                return;
            case -1:
                offset -= Main.pageSize;
        }
    }

    private static boolean hasError(HttpResponse<String> response) {
        JsonObject body = JsonParser.parseString(response.body()).getAsJsonObject();
        return body.has("error");
    }

    private static void noAuthMessage() {
        CLI.update("Please, provide access for application.");
    }

    private enum Action {
        FEATURED,
        NEW,
        CATEGORIES,
        PLAYLISTS
    }
}
