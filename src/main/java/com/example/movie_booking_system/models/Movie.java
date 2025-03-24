package com.example.movie_booking_system.models;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "movies")  // Explicit table name
public class Movie {  // ✅ Class name should be capitalized
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ✅ Auto-increment ID
    private Long id;

    @Column(nullable = false)
    private String title;

    private Integer year;
    private String genre;

    @Column(length = 1000)  // ✅ Prevents truncation
    private String summary;

    @Column(length = 2000)  // ✅ Stores long descriptions
    private String description;

    private String language;

      // ✅ Stores a list of image URLs
    private String imageUrls;

    // ✅ Default Constructor
    public Movie() {}

    // ✅ Constructor (without ID, since ID is auto-generated)
    public Movie(String title, Integer year, String genre, String summary, String description, String language, String imageUrls) {
        this.title = title;
        this.year = year;
        this.genre = genre;
        this.summary = summary;
        this.description = description;
        this.language = language;
        this.imageUrls = imageUrls;
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

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getGenre() {
        return genre;
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
}
