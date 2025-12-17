package com.example.reservabackend.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@RestController
@RequestMapping("/api/farmacias")
public class FarmaciaProxyController {
    private static final String REMOTE = "https://midas.minsal.cl/farmacia_v2/WS/getLocalesTurnos.php";
    
    // Configura un SSLContext que confía en todo para evitar errores de certificado (PKIX)
    private static SSLContext createUnsafeSslContext() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            };
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            return sc;
        } catch (Exception e) {
            throw new RuntimeException("Error creando SSLContext inseguro", e);
        }
    }

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .cookieHandler(new java.net.CookieManager(null, java.net.CookiePolicy.ACCEPT_ALL))
            .sslContext(createUnsafeSslContext())
            .build();

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getFarmacias() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(REMOTE))
                    .timeout(Duration.ofSeconds(15))
                    .header("Accept", "application/json, text/plain, */*")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .header("Referer", "https://midas.minsal.cl/farmacia_v2/")
                    .header("Accept-Language", "es-CL,es;q=0.9,en;q=0.8")
                    .GET()
                    .build();

            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            int status = resp.statusCode();
            String body = resp.body() == null ? "[]" : resp.body();
            return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
        } catch (Exception e) {
            String err = String.format("{\"error\": \"proxy failure\", \"message\": \"%s\"}", e.getMessage());
            return ResponseEntity.status(502).contentType(MediaType.APPLICATION_JSON).body(err);
        }
    }
}
