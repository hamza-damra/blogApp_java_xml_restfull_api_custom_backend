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
@Builder
@AllArgsConstructor
public class Comment {
    private int id;
    private String name;
    private String email;
    private String body;
    private List<CommentReplay> replies;

    // Add method to convert JSON to Comment object
    public static Comment fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Comment.class);
    }
}
