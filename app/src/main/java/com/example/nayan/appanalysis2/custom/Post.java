package com.example.nayan.appanalysis2.custom;

import android.net.Uri;

import me.everything.providers.core.Entity;
import me.everything.providers.core.FieldMapping;
import me.everything.providers.core.IgnoreMapping;
/**
 * Created by Dev on 12/27/2017.
 */
public class Post extends Entity  {

    @IgnoreMapping
    public static Uri uri = PostsContentProvider.POSTS_URI;

    @FieldMapping(columnName = PostsTable.COLUMN_ID, physicalType = FieldMapping.PhysicalType.Int)
    public Integer id;

    @FieldMapping(columnName = PostsTable.COLUMN_TITLE, physicalType = FieldMapping.PhysicalType.String)
    public String title;

    @FieldMapping(columnName = PostsTable.COLUMN_THUMBNAIL, physicalType = FieldMapping.PhysicalType.Blob, logicalType = FieldMapping.LogicalType.Boolean)
    public byte[] thumbnail;

    @FieldMapping(columnName = PostsTable.COLUMN_FROM_ID, physicalType = FieldMapping.PhysicalType.Int, logicalType = FieldMapping.LogicalType.Long)
    public Long fromId;

    @FieldMapping(columnName = PostsTable.COLUMN_IS_OWNER, physicalType = FieldMapping.PhysicalType.Int, logicalType = FieldMapping.LogicalType.Boolean)
    public Boolean isOwner;

    @FieldMapping(columnName = PostsTable.COLUMN_UPDATED_AT, physicalType = FieldMapping.PhysicalType.Int, logicalType = FieldMapping.LogicalType.Long)
    public Long updatedAt;


}
