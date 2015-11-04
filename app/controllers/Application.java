package controllers;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.databind.JsonNode;
import models.User;
import models.entries.TaskEntry;
import play.Routes;
import play.data.Form;
import play.mvc.*;
import play.mvc.Http.Session;
import play.mvc.Result;
import providers.MyUsernamePasswordAuthProvider;
import providers.MyUsernamePasswordAuthProvider.MyLogin;
import providers.MyUsernamePasswordAuthProvider.MySignup;

import scala.util.parsing.json.JSONObject$;
import views.html.*;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
import com.feth.play.module.pa.user.AuthUser;

public class Application extends Controller {

	public static final String FLASH_MESSAGE_KEY = "message";
	public static final String FLASH_ERROR_KEY = "error";
	public static final String USER_ROLE = "user";


	//================================================================================
	// Home Page
	//================================================================================
	public static Result index() {
		
		return ok(index.render());
	}


	//================================================================================
	// Authentication
	//================================================================================

	public static User getLocalUser(final Session session) {
		final AuthUser currentAuthUser = PlayAuthenticate.getUser(session);

		final User localUser = User.findByAuthUserIdentity(currentAuthUser);
		return localUser;
	}

	@Restrict(@Group(Application.USER_ROLE))
	public static Result restricted() {
		final User localUser = getLocalUser(session());
		return ok(restricted.render(localUser));
	}

	@Restrict(@Group(Application.USER_ROLE))
	public static Result profile() {
		final User localUser = getLocalUser(session());
		return ok(profile.render(localUser));
	}

	public static Result login() {
		return ok(login.render(MyUsernamePasswordAuthProvider.LOGIN_FORM));
	}

	public static Result webhook(){

		JsonNode body = request().body().asJson();
		long userId = body.get("client").get("user_id").asLong();
		String operation = body.get("operation").textValue();

		if(operation.equals("create")) {
			String time = body.get("after").get("created_at").asText() ;
			// create TaskAction for creation
			TaskEntry.create(userId, time, time, "created") ;
		} else if(operation.equals("update")) {
			boolean afterCompletion = body.get("after").get("completed").asBoolean();
			boolean beforeCompletion = body.get("before").get("completed").asBoolean();

			if(afterCompletion && !beforeCompletion) {
				String start_time = body.get("before").get("created_at").asText() ;
				String end_time = body.get("after").get("updated_at").asText() ;
				// create TaskAction for completion
				TaskEntry.create(userId, start_time, end_time, "completed") ;
				System.out.println("JOB COMPLETED!");
			}

		}

		return ok(index.render());
	}

	public static Result doLogin() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<MyLogin> filledForm = MyUsernamePasswordAuthProvider.LOGIN_FORM
				.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not fill everything properly
			return badRequest(login.render(filledForm));
		} else {
			// Everything was filled
			return UsernamePasswordAuthProvider.handleLogin(ctx());
		}
	}

	public static Result signup() {
		return ok(signup.render(MyUsernamePasswordAuthProvider.SIGNUP_FORM));
	}

	public static Result jsRoutes() {
		return ok(
				Routes.javascriptRouter("jsRoutes",
						controllers.routes.javascript.Signup.forgotPassword()))
				.as("text/javascript");
	}

	public static Result doSignup() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<MySignup> filledForm = MyUsernamePasswordAuthProvider.SIGNUP_FORM
				.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not fill everything properly
			return badRequest(signup.render(filledForm));
		} else {
			// Everything was filled
			// do something with your part of the form before handling the user
			// signup
			return UsernamePasswordAuthProvider.handleSignup(ctx());
		}
	}



	//================================================================================
	// Integration Settings
	//================================================================================

	public static Result settings() {

		return ok(integration_settings.render());
	}


	//================================================================================
	// Miscellaneous
	//================================================================================

	public static String formatTimestamp(final long t) {
		return new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").format(new Date(t));
	}

}