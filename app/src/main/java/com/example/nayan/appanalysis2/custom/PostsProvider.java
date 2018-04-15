package com.example.nayan.appanalysis2.custom;

import android.content.Context;

import me.everything.providers.core.AbstractProvider;
import me.everything.providers.core.Data;
/**
 * Created by Dev on 12/27/2017.
 */
public class PostsProvider extends AbstractProvider {

    public PostsProvider(Context context) {
        super(context);
    }

    /**
     * Get all posts
     */
    public Data<Post> getPosts() {
        Data<Post> posts = getContentTableData(Post.uri, Post.class);
        return posts;
    }
}
