# Music Advisor
Accesses [Spotify's](https://www.spotify.com) REST API based on which command was given, printing links to suggested albums/playlists, in pages of 5, based on the default amount of elements returned.
The objects are parsed from a JSON payload included in each GET request with the use of [Gson](https://github.com/google/gson).

### Command Line Arguments
The project includes the [Maven Exec Plugin](https://www.mojohaus.org/exec-maven-plugin/) which allows you to 
define the program arguments in the [pom.xml](./pom.xml) inside the configuration tags of the plugin, like:

```xml
    <configuration>
        <arguments>
            ...
            <argument>argument-1</argument>
            <argument>value-1</argument>
            <argument>argument-2</argument>
            <argument>argument-3</argument>
            ...
        </arguments>
    </configuration>
```

#### Valid Arguments
`-access`   : Specifies an alternative URL for the OAuth process (default: `https://accounts.spotify.com`).

`-resource` : Specifies an alternative URL for the REST requests (default: `https://api.spotify.com`).

`-page`     : Specifies the page size (default: `5`).