package com.hamza.blogapp_custombackend.models;

import com.google.gson.Gson;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Post {
    private int id;
    private String title;
    private String description;
    private String content;
    private List<Comment> comments;

    public Post(String title, String description, String content, List<Comment> comments) {
        this.title = title;
        this.description = description;
        this.content = content;
        this.comments = comments;
    }

    // Add method to convert JSON to Post object
    public static Post fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Post.class);
    }
}
