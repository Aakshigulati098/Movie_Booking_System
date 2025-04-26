package com.example.movie_booking_system.models;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "movies")  // Explicit table name
public class Movie {  // ✅ Class name should be capitalized
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ✅ Auto-increment ID
    private Long id;

    private String status; // Add this line

    @Column(nullable = false)
    private String title;

    private String releaseDate;
    private String genre;

    @Column(length = 1000)  // ✅ Prevents truncation
    private String summary;

    @Column(length = 2000)  // ✅ Stores long descriptions
    private String description;

    private String language;

      // ✅ Stores a list of image URLs
    private String imageUrls;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // ✅ Default Constructor
    public Movie() {}

    // ✅ Constructor (without ID, since ID is auto-generated)
    public Movie(String title, String releaseDate, String genre, String summary, String description, String language, String imageUrls,String status) {
        this.title = title;
        this.releaseDate = releaseDate;
        this.genre = genre;
        this.summary = summary;
        this.description = description;
        this.language = language;
        this.imageUrls = imageUrls;
        this.status = status;
    }

    // ✅ Getters & Setters
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGenre() {
        return genre != null ? genre : ""; // Return an empty string if genre is null
    }
    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(String imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getImage(){
        return this.imageUrls; // Return the single image URL directly
    }

    public String getType() {
        if (genre == null || genre.isEmpty()) {
            return ""; // Return empty string or some default value
        }
        return genre.split(",")[0];
    }
}
