package com.example.myapplication.DataModel;


public class Service {
    String name;
    String description;
    String pictureUrl;

    public Service() {
    }

    public Service(String name, String description, String pictureUrl) {
        this.name = name;
        this.description = description;
        this.pictureUrl = pictureUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }
}
