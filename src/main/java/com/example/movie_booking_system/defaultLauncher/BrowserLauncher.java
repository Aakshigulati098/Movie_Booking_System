package com.example.movie_booking_system.defaultLauncher;

import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.awt.Desktop;
import java.net.URI;

@Component
public class BrowserLauncher {
    @Autowired
    private Environment environment;
    @EventListener(ApplicationReadyEvent.class)
    public void launchBrowser() {
        try {
            System.out.println("Attempting to launch browser...");
            String url = "http://localhost:"+environment.getProperty("server.port", "8080");
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                System.out.println("Desktop is not supported. Cannot launch browser.\nTrying some brut force:");
                String os = System.getProperty("os.name").toLowerCase();

                if (os.contains("win")) {
                    // For Windows
                    Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", url});
                } else if (os.contains("mac")) {
                    // For macOS
                    Runtime.getRuntime().exec(new String[]{"open", url});
                } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
                    // For Linux
                    Runtime.getRuntime().exec(new String[]{"xdg-open", url});
                } else {
                    System.out.println("Operating system not supported. Cannot launch browser.");
                    System.out.println("Application is running. Open this URL in your browser: http://localhost:8080");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}