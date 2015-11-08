package controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import models.User;
import models.entries.Entry;
import models.entries.TaskEntry;
import play.Routes;
import play.data.Form;
import play.db.DB;
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
	// Display Page
	//================================================================================

	@Restrict(@Group(Application.USER_ROLE))
    public static Result index() {

        final User localUser = getLocalUser(session());

		List<Entry> tasks = TaskEntry.find.all();
		List<TaskEntry> taskEntries = new ArrayList<TaskEntry>();
		for (int i=0; i<tasks.size();i++){
			taskEntries.add((TaskEntry)tasks.get(tasks.size()-i-1));
		}

		return ok(index.render(taskEntries));
    }


    public static Result webhook(){

		JsonNode body = request().body().asJson();
		System.out.println(body.toString());
		long userId = body.get("client").get("user_id").asLong();
		String title = body.get("after").get("title").asText();

		String operation = body.get("operation").textValue();

		if(operation.equals("create")) {
			String time = body.get("after").get("created_at").asText() ;
			//Format the string to remove non-parseable letters
			time = time.substring(0, time.indexOf("T")) + " " + time.substring(time.indexOf("T") + 1);
			time = time.substring(0, time.length()-1) ;
			// create TaskAction for creation
			Entry entry = TaskEntry.create(userId, title, time, "created") ;
			entry.save() ;
			System.out.println("Created TaskEntry for creation") ;
		} else if(operation.equals("update")) {
			boolean afterCompletion = body.get("after").get("completed").asBoolean();
			boolean beforeCompletion = body.get("before").get("completed").asBoolean();

			if(afterCompletion && !beforeCompletion) {
				String start_time = body.get("before").get("created_at").asText() ;
				start_time = start_time.substring(0, start_time.indexOf("T")) + " " + start_time.substring(start_time.indexOf("T") + 1);
				start_time = start_time.substring(0, start_time.length()-1) ;

				String end_time = body.get("after").get("updated_at").asText() ;
				end_time = end_time.substring(0, end_time.indexOf("T")) + " " + end_time.substring(end_time.indexOf("T") + 1);
				end_time = end_time.substring(0, end_time.length()-1) ;

				// create TaskAction for completion
				Entry entry = TaskEntry.create(userId, title ,start_time, "completed") ;
				entry.save() ;
				System.out.println(Entry.find.all()) ;
				System.out.println("Created TaskEntry for completion") ;
			}

		}

		return index();
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

	public static Result login() {

		final User localUser = getLocalUser(session());

		if(localUser == null) {
			return ok(login.render());
		}

		return redirect("/");
	}


	@Restrict(@Group(Application.USER_ROLE))
	public static Result profile() {
		final User localUser = getLocalUser(session());
		return ok(profile.render(localUser));
	}

	/**
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
	}*/

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

	@Restrict(@Group(Application.USER_ROLE))
	public static Result accounts() {
		final User localUser = getLocalUser(session());
		return ok(accounts.render());
	}


	//================================================================================
	// Miscellaneous
	//================================================================================

	public static String formatTimestamp(final long t) {
		return new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").format(new Date(t));
	}

}