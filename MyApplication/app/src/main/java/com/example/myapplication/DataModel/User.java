package com.example.myapplication.DataModel;

public class User {
    private long id;
    private String type;

    public User(String username, String email, String first_name, String last_name, String type,long id) {
        this.username = username;
        this.email = email;
        this.first_name = first_name;
        this.last_name = last_name;
        this.type=type;
        this.id=id;
    }
    public User(String username, String email, String first_name, String last_name, String type) {
        this.username = username;
        this.email = email;
        this.first_name = first_name;
        this.last_name = last_name;
        this.type=type;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    private String username;


    private String password;

    private String email;


    private String first_name;


    private String last_name;

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
