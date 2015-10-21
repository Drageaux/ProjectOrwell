package controllers;

import play.*;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {
    public static final String USER_ROLE = "user";
    public Result index() {
        return ok(index.render("Your new application is ready - Dom"));
    }

}
