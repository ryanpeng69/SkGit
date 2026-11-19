package io.github.ryanpeng69.skgit.features;

import com.google.gson.JsonParser;
import io.github.ryanpeng69.skgit.SkGit;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class Updater {
    /** Check GitHub for new release.
     * @return Whether a new release is found. */
    public static CompletableFuture<String> checkUpdate() {
        return getRemoteVersion().thenApply(remoteStr -> {
            String localStr = SkGit.getInstance().getPluginMeta().getVersion();
            int local = versionToInt(localStr);
            int remote = versionToInt(remoteStr);
            return (remote > local) ? remoteStr : "latest";
        }).exceptionally(e -> {
            SkGit.throwError("Failed to check update, "+e.getMessage());
            return null;
        });
    }

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private static CompletableFuture<String> getRemoteVersion() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/repos/ryanpeng69/SkGit/releases/latest"))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json")
                .header("User-Agent", "ButterWorld")
                .GET()
                .build();
        return HTTP_CLIENT
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenCompose(response -> {
                    if (response.statusCode() < 200 || response.statusCode() >= 300) {
                        return CompletableFuture.failedFuture(new RuntimeException("status code: "+response.statusCode()+" body: "+response.body()));
                    }
                    return CompletableFuture.completedFuture(JsonParser.parseString(response.body())
                            .getAsJsonObject().get("tag_name").getAsString());
                });
    }
    private static int versionToInt(String version) {
        if (version == null || version.isEmpty()) return 0;
        version = version.replaceAll("-pre\\d+$", "");
        return Integer.parseInt(version.replaceAll("\\D+", ""));
    }
}
