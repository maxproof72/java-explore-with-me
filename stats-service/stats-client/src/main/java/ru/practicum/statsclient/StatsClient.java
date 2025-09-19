package ru.practicum.statsclient;

import lombok.NonNull;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.statsdto.NewHitDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class StatsClient {

    private final RestTemplate rest;

    public StatsClient(String serverUrl) {

        rest = new RestTemplate();
        rest.setUriTemplateHandler(new DefaultUriBuilderFactory(serverUrl));
        rest.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    protected ResponseEntity<Object> get(String path, @Nullable Map<String, Object> parameters) {
        return makeAndSendRequest(HttpMethod.GET, path, parameters, null);
    }

    protected <T> ResponseEntity<Object> post(String path, T body) {
        return makeAndSendRequest(HttpMethod.POST, path, null, body);
    }

    private <T> ResponseEntity<Object> makeAndSendRequest(HttpMethod method, String path, @Nullable Map<String, Object> parameters, @Nullable T body) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<T> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Object> response;
        try {
            if (parameters != null) {
                response = rest.exchange(path, method, requestEntity, Object.class, parameters);
            } else {
                response = rest.exchange(path, method, requestEntity, Object.class);
            }
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
        return prepareResponse(response);
    }

    private static ResponseEntity<Object> prepareResponse(ResponseEntity<Object> response) {

        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());

        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }

        return responseBuilder.build();
    }


    public void addHit(@NonNull String app,
                       @NonNull String uri,
                       @NonNull String ip,
                       @NonNull LocalDateTime time) {

        post("/hit", new NewHitDto(app, uri, ip, time));
    }

    public ResponseEntity<Object> getStats(
            @NonNull LocalDateTime start,
            @NonNull LocalDateTime end,
            /*Nullable*/ List<String> uris,
            boolean unique) {

        return get("/stats", Map.of(
                "start", start,
                "end", end,
                "uris", uris,
                "unique", unique));
    }
}
