package com.teamtreehouse.blog.dao;

import com.teamtreehouse.blog.model.BlogEntry;

import java.util.List;

// Data Access Object Layer for Spark blog project
public interface BlogDAO {
    boolean addEntry(BlogEntry blogEntry);
    List<BlogEntry> findAllEntries();
    BlogEntry findEntryBySlug(String slug);
    boolean removeEntry(BlogEntry blogEntry);
}
