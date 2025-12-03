package com.simpsons.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpsons.model.Personajes;
import com.simpsons.model.SimpsonResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class SimpsonService {
    private static final String BASE_URL = "https://thesimpsonsapi.com/api";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public SimpsonService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public CompletableFuture<SimpsonResponse> getCharacters(int page) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = BASE_URL + "/characters?page=" + page;
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .timeout(Duration.ofSeconds(10))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return objectMapper.readValue(response.body(), SimpsonResponse.class);
                } else {
                    throw new IOException("Error al obtener personajes. Código: " + response.statusCode());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error al consumir la API de Los Simpsons", e);
            }
        });
    }

    public CompletableFuture<Personajes> getCharacterById(int id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = BASE_URL + "/characters/" + id;
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .timeout(Duration.ofSeconds(10))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return objectMapper.readValue(response.body(), Personajes.class);
                } else {
                    throw new IOException("Error al obtener personaje. Código: " + response.statusCode());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error al consumir la API de Los Simpsons", e);
            }
        });
    }
}


