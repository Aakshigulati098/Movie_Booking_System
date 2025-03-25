// src/main/java/com/example/movie_booking_system/dto/MovieDTO.java
package com.example.movie_booking_system.dto;

public class MovieDTO {
    private Long id;
    private String title;
    private String image;
    private String type;

    public MovieDTO(Long id, String title, String image, String type) {
        this.id = id;
        this.title = title;
        this.image = image;
        this.type = type;
    }

    public MovieDTO() {

    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}