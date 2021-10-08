package advisor;

import advisor.controllers.Controller;
import advisor.view.CLI;

import java.util.List;

/**
 * @author Alex Giazitzis
 */
public class Main {
    public static       String accessPoint   = "https://accounts.spotify.com";
    public static       String resourcePoint = "https://api.spotify.com";
    public static       int    pageSize      = 5;

    public static void main(String[] args) {
        //Transforms the args array into a list for easier parsing.
        List<String> arguments = List.of(args);
        if (arguments.contains("-access") && arguments.size() > arguments.indexOf("-access")) {
            accessPoint = arguments.get(arguments.indexOf("-access") + 1);
        }
        if (arguments.contains("-resource") && arguments.size() > arguments.indexOf("-resource")) {
            resourcePoint = arguments.get(arguments.indexOf("-resource") + 1);
        }
        if (arguments.contains("-page") && arguments.size() > arguments.indexOf("-page")) {
            try {
                pageSize = Integer.parseInt(arguments.get(arguments.indexOf("-page") + 1));
            } catch (NumberFormatException ignored) {
                CLI.update("Falling back to default page size. Page argument is invalid.");
            }
        }

        boolean exit = false;
        while (!exit) {
            exit = Controller.handleUserAction();
        }
    }
}
