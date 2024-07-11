package com.hamza.blogapp_custombackend.models;

import com.google.gson.annotations.Expose;


import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Post {
    @Expose(serialize = false)
    private int id;

    @Expose
    private String title;

    @Expose
    private String description;

    @Expose
    private String content;

    @Expose
    private Set<Comment> comments;

    @Expose
    private Set<Image> imageUrls;

    public Post(String title, String description, String content, Set<Comment> comments, Set<Image> images) {
        this.title = title;
        this.description = description;
        this.content = content;
        this.comments = comments;
        this.imageUrls = images;
    }
}
