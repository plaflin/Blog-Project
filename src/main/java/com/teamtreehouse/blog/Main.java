package com.teamtreehouse.blog;

import static spark.Spark.*;

import java.util.HashMap;
import java.util.Map;

import com.teamtreehouse.blog.dao.BlogDAO;
import com.teamtreehouse.blog.dao.SimpleBlogDAOImpl;
import com.teamtreehouse.blog.model.BlogEntry;
import com.teamtreehouse.blog.model.Comment;
import com.teamtreehouse.blog.model.NotFoundException;
import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;

// Driver class for the website. Holds all of the routes and the default blog entries.
public class Main
{

    public static void main( String[] args )
    {
        // Lets the website know where the css folders are
        staticFileLocation("/public");
        BlogDAO dao = new SimpleBlogDAOImpl();

        // HTTP Middleware that protects the page from being added to or edited without permission
        before(((req,res)->{
            if(req.cookie("password") != null){
                req.attribute("password", req.cookie("password"));
            }
        }));

        before("/new-entry", (req,res)->{
            if(req.attribute("password") == null){
                res.redirect("/password");
                halt();
            }
        });

        before("/:slug/edit", (req,res)->{
            if(req.attribute("password") == null){
                res.redirect("/password");
                halt();
            }
        });

        // I almost forgot this one. Good thing I went back and added comments. Huh?
        before("/:slug/delete", (req, res) -> {
            if(req.attribute("password") == null) {
                res.redirect("/password");
                halt();
            }
        });

        // Routes for the website follow
        // Route for the index/home page
        get("/", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("entries", dao.findAllEntries());
            return new ModelAndView(model, "index.hbs");
        }, new HandlebarsTemplateEngine());

        // Routes for making a new entry.
        // This route brings up the new entry page.
        get("/new-entry", (req, res) ->
                        new ModelAndView(null, "new.hbs"),
                new HandlebarsTemplateEngine());

        // This route posts the user's new blog page
        post("/new-entry", (req, res) -> {
            BlogEntry blogEntry = new BlogEntry(req.queryParams("title"), req.queryParams("entry"));
            dao.addEntry(blogEntry);
            res.redirect("/");
            return null;
        });

        // Routes for the password protection package
        // This route brings up the password page
        get("/password", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            return new ModelAndView(model, "password.hbs");
        }, new HandlebarsTemplateEngine());

        // This route accepts the password. If it is correct the user is redirected to index.
        post("/password", (req, res)->{
            String password = req.queryParams("password");
            if(password.toLowerCase().equals("admin")){
                res.cookie("password", password);
                res.redirect("/");
            }else {
                res.redirect("/password");
            }
            return null;
        });

        // This route takes the user to a specific blog page
        get("/:slug", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("entry", dao.findEntryBySlug(req.params("slug")));
            return new ModelAndView(model, "detail.hbs");
        }, new HandlebarsTemplateEngine());

        // This route posts comments to the page without changing the post
        post("/:slug", (req, res) ->{
            BlogEntry blogEntry = dao.findEntryBySlug(req.params("slug"));
            Comment comment = new Comment(req.queryParams("comment"), req.queryParams("name"));
            blogEntry.addComment(comment);
            res.redirect("/" + blogEntry.getSlug());
            return null;
        });

        // Routes for the editing of pages
        // This route takes the user to the page that edits a particular blog
        get("/:slug/edit", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("entry", dao.findEntryBySlug(req.params("slug")));
            return new ModelAndView(model, "edit.hbs");
        }, new HandlebarsTemplateEngine());

        // This route posts the edited blog page
        post("/:slug/edit", (req, res) ->{
            BlogEntry blogEntry = dao.findEntryBySlug(req.params("slug"));
            String oldTitle = blogEntry.getTitle();
            if(!req.queryParams("title").equals("")) {
                blogEntry.setTitle(req.queryParams("title"));
            }
            else {
                blogEntry.setTitle(oldTitle);
            }
            blogEntry.setBody(req.queryParams("entry"));
            res.redirect("/" + blogEntry.getSlug());
            return null;
        });

        // This route deletes the selected blog. Now, password protected!
        get("/:slug/delete", (req, res) ->
        {
            BlogEntry blogEntry = dao.findEntryBySlug(req.params("slug"));
            dao.removeEntry(blogEntry);
            res.redirect("/");
            return null;
        });

        // Lets the user know that we can't find the link they're looking for.
        exception(NotFoundException.class,(exc, req, res) -> {
            res.status(404);
            HandlebarsTemplateEngine engine = new HandlebarsTemplateEngine();
            String html = engine.render(new ModelAndView(null , "not-found.hbs"));
            res.body(html);
        });

        /*
        Storing the provided blogs because I didn't like seeing an empty index.
        Also, found out this was required. I'm glad I did this.
        Creative names I know but they work.
        */
        BlogEntry blogEntry1, blogEntry2, blogEntry3;

        blogEntry1 = new BlogEntry("The best day I’ve ever had",
                "<p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc ut rhoncus felis, vel tincidunt neque." +
                        "Vestibulum ut metus eleifend, malesuada nisl at, scelerisque sapien. " +
                        "Vivamus pharetra massa libero, sed feugiat turpis efficitur at.<p>" +
                        "<p>Cras egestas ac ipsum in posuere. Fusce suscipit, libero id malesuada placerat, " +
                        "orci velit semper metus, quis pulvinar sem nunc vel augue. In ornare tempor metus, sit amet congue justo porta et. " +
                        "Etiam pretium, sapien non fermentum consequat, <a href=\"\">dolor augue</a> gravida lacus, non accumsan lorem odio id risus. " +
                        "Vestibulum pharetra tempor molestie. Integer sollicitudin ante ipsum, a luctus nisi egestas eu. Cras accumsan cursus ante, non dapibus tempor.<p>");
        dao.addEntry(blogEntry1);
        // I couldn't figure out the tags option and didn't want to delete these. So, I commented them out.
        //blogEntry1.addTag("happy");
        //blogEntry1.addTag("diary entry");
        blogEntry1.addComment(new Comment("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc ut rhoncus felis, vel tincidunt neque. Vestibulum ut metus eleifend, malesuada nisl at, scelerisque sapien. Vivamus pharetra massa libero, sed feugiat turpis efficitur at.",
                "Carling Kirk"));

        blogEntry2 = new BlogEntry("The absolute worst day I’ve ever had",
                "<p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc ut rhoncus felis, vel tincidunt neque." +
                        "Vestibulum ut metus eleifend, malesuada nisl at, scelerisque sapien. " +
                        "Vivamus pharetra massa libero, sed feugiat turpis efficitur at.<p>" +
                        "<p>Cras egestas ac ipsum in posuere. Fusce suscipit, libero id malesuada placerat, " +
                        "orci velit semper metus, quis pulvinar sem nunc vel augue. In ornare tempor metus, sit amet congue justo porta et. " +
                        "Etiam pretium, sapien non fermentum consequat, <a href=\"\">dolor augue</a> gravida lacus, non accumsan lorem odio id risus. " +
                        "Vestibulum pharetra tempor molestie. Integer sollicitudin ante ipsum, a luctus nisi egestas eu. Cras accumsan cursus ante, non dapibus tempor.<p>");
        dao.addEntry(blogEntry2);

        blogEntry3 = new BlogEntry("That time at the mall",
                "<p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc ut rhoncus felis, vel tincidunt neque." +
                        "Vestibulum ut metus eleifend, malesuada nisl at, scelerisque sapien. " +
                        "Vivamus pharetra massa libero, sed feugiat turpis efficitur at.<p>" +
                        "<p>Cras egestas ac ipsum in posuere. Fusce suscipit, libero id malesuada placerat, " +
                        "orci velit semper metus, quis pulvinar sem nunc vel augue. In ornare tempor metus, sit amet congue justo porta et. " +
                        "Etiam pretium, sapien non fermentum consequat, <a href=\"\">dolor augue</a> gravida lacus, non accumsan lorem odio id risus. " +
                        "Vestibulum pharetra tempor molestie. Integer sollicitudin ante ipsum, a luctus nisi egestas eu. Cras accumsan cursus ante, non dapibus tempor.<p>");
        dao.addEntry(blogEntry3);

    }
}