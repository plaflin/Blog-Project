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

public class Main
{

    public static void main( String[] args )
    {
        staticFileLocation("/public");
        BlogDAO dao = new SimpleBlogDAOImpl();

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

        get("/", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("entries", dao.findAllEntries());
            return new ModelAndView(model, "index.hbs");
        }, new HandlebarsTemplateEngine());

        get("/new-entry", (req, res) ->
                        new ModelAndView(null, "new.hbs"),
                new HandlebarsTemplateEngine());

        post("/new-entry", (req, res) -> {
            BlogEntry blogEntry = new BlogEntry(req.queryParams("title"), req.queryParams("entry"));
            dao.addEntry(blogEntry);
            res.redirect("/");
            return null;
        });

        get("/password", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            return new ModelAndView(model, "password.hbs");
        }, new HandlebarsTemplateEngine());


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

        get("/:slug", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("entry", dao.findEntryBySlug(req.params("slug")));
            return new ModelAndView(model, "detail.hbs");
        }, new HandlebarsTemplateEngine());

        get("/:slug/edit", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("entry", dao.findEntryBySlug(req.params("slug")));
            return new ModelAndView(model, "edit.hbs");
        }, new HandlebarsTemplateEngine());

        post("/:slug/edit", (req, res) ->{
            BlogEntry blogEntry = dao.findEntryBySlug(req.params("slug"));
            blogEntry.setTitle(req.queryParams("title"));
            blogEntry.setBody(req.queryParams("entry"));
            res.redirect("/" + blogEntry.getSlug());
            return null;
        });

        get("/:slug/delete", (req, res) ->
        {
            BlogEntry blogEntry = dao.findEntryBySlug(req.params("slug"));
            dao.removeEntry(blogEntry);
            res.redirect("/");
            return null;
        });

        exception(NotFoundException.class,(exc, req, res) -> {
            res.status(404);
            HandlebarsTemplateEngine engine = new HandlebarsTemplateEngine();
            String html = engine.render(new ModelAndView(null , "not-found.hbs"));
            res.body(html);
        });

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