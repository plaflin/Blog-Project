package com.teamtreehouse.blog.dao;

import java.util.ArrayList;
import java.util.List;

import com.teamtreehouse.blog.model.BlogEntry;
import com.teamtreehouse.blog.model.NotFoundException;

public class SimpleBlogDAOImpl implements BlogDAO{
    private List<BlogEntry> entries;

    public SimpleBlogDAOImpl() {
        entries = new ArrayList<>();
    }

    @Override
    public boolean addEntry(BlogEntry blogEntry) {
        return entries.add(blogEntry);
    }

    @Override
    public List<BlogEntry> findAllEntries() {
        return new ArrayList<>(entries);
    }


    @Override
    public BlogEntry findEntryBySlug(String slug) {
        return entries.stream()
                .filter(entry -> entry.getSlug()
                        .equals(slug))
                .findFirst()
                .orElseThrow(NotFoundException::new);
    }

    @Override
    public boolean removeEntry(BlogEntry blogEntry) {
        return entries.remove(blogEntry);
    }

}

