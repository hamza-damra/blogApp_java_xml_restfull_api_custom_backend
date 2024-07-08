package com.hamza.blogapp_custombackend.models;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class CommentReplay {
    private int id;
    private String name;
    private String email;
    private String body;

    // Add method to convert JSON to CommentReplay object
    public static CommentReplay fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, CommentReplay.class);
    }
}
