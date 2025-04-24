package com.example.movie_booking_system.defaultlauncher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Component
public class BrowserLauncher {
    private static final Logger browserLogger = LoggerFactory.getLogger(BrowserLauncher.class);
    private static final String DEFAULT_PORT = "8080";
    private static final String URL_FORMAT = "http://localhost:%s";

    private final Environment environment;

    @Autowired
    public BrowserLauncher(Environment environment) {
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void launchBrowser() {
        browserLogger.info("Attempting to launch browser...");
        String port = environment.getProperty("server.port", DEFAULT_PORT);
        String url = String.format(URL_FORMAT, port);

        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
                return;
            }

            browserLogger.info("Desktop is not supported. Attempting alternative launch methods.");
            launchBrowserByOS(url);

        } catch (URISyntaxException e) {
            browserLogger.error("Invalid URL syntax: {}", url, e);
        } catch (IOException e) {
            browserLogger.error("Failed to launch browser: {}", e.getMessage(), e);
        } catch (Exception e) {
            browserLogger.error("Unexpected error while launching browser: {}", e.getMessage(), e);
        }
    }

    private void launchBrowserByOS(String url) throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        ProcessBuilder processBuilder;

        if (os.contains("win")) {
            processBuilder = new ProcessBuilder("cmd", "/c", "start", url);
        } else if (os.contains("mac")) {
            processBuilder = new ProcessBuilder("open", url);
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            processBuilder = new ProcessBuilder("xdg-open", url);
        } else {
            browserLogger.warn("Operating system not supported. Cannot launch browser automatically.");
            browserLogger.info("Application is running. Please open {} in your browser", url);
            return;
        }

        processBuilder.start();
    }
}