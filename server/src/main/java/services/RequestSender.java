package services;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

class RequestSender {
    private static final String API_URL = "https://api.football-data.org/v2/";
    private static final String TOKEN = "af0f8f2906a845aba548a5916e1f8bc5";
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    static CompletableFuture<String> getLeagueStanding(final String leagueCode) {
        final HttpRequest request = HttpRequest.newBuilder().uri(URI.create(
                RequestSender.API_URL +
                        URLEncoder.encode("competitions", StandardCharsets.UTF_8)
                        + "/"
                        + URLEncoder.encode(leagueCode, StandardCharsets.UTF_8)
                        + "/"
                        + URLEncoder.encode("standings", StandardCharsets.UTF_8)
                        + "?standingType=TOTAL"
        ))
                .headers("X-Auth-Token", RequestSender.TOKEN)
                .build();
        return RequestSender.CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    static CompletableFuture<String> getTopScorer(final String leagueCode) {
        final HttpRequest request = HttpRequest.newBuilder().uri(URI.create(
                RequestSender.API_URL +
                        URLEncoder.encode("competitions", StandardCharsets.UTF_8)
                        + "/"
                        + URLEncoder.encode(leagueCode, StandardCharsets.UTF_8)
                        + "/"
                        + URLEncoder.encode("scorers", StandardCharsets.UTF_8)
                        + "?limit=3"))
                .headers("X-Auth-Token", RequestSender.TOKEN)
                .build();
        return RequestSender.CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

}
