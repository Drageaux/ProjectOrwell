package controllers;

import java.text.SimpleDateFormat;
import java.util.*;


import models.LinkedAccount;
import models.User;
import models.entries.Entry;
import models.entries.PushEntry;
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

		List<TaskEntry> tasks = TaskEntry.find
									.where()
									.eq("linkedAccounts.user.id", localUser.id)
									.orderBy("end_time desc")
									.findList();

		List<PushEntry> entries = PushEntry.find
									.where()
									.eq("linkedAccounts.user.id", localUser.id)
									.orderBy("end_time desc")
									.findList();

		return ok(index.render(tasks));
    }

	//================================================================================
	// Statistics page
	//================================================================================
	@Restrict(@Group(Application.USER_ROLE))
	public static Result statistics() {
		String[] providers = {"github", "wunderlist", "facebook"};

		final User localUser = getLocalUser(session());

		List<TaskEntry> tasks = TaskEntry.find
				.where()
				.eq("linkedAccounts.user.id", localUser.id)
				.findList();
		//We have to convert the tasks List to an array of TaskEntries before passing it into getCounts.
		Map<String, Long> taskCounts = getCounts(tasks.toArray(new TaskEntry[tasks.size()]));

		List<PushEntry> pushes = PushEntry.find
				.where()
				.eq("linkedAccounts.user.id", localUser.id)
				.findList();
		Map<String, Long> pushCounts = getCounts(pushes.toArray(new PushEntry[pushes.size()]));

		return ok(statistics.render(taskCounts, pushCounts));
	}

	//This will create a mapping between the date and the number of entries that were created on that date.
	private static Map<String, Long> getCounts(Entry[] entries){
		Map<String,Long> stats = new HashMap<String,Long>();
		for(Entry e: entries){
			//The key being used to store values is the string representation of the start date.
			Date date = e.getStartTime() ;
			Calendar cal = Calendar.getInstance() ;
			cal.setTime(date) ;

			String key = cal.get(Calendar.YEAR) + "/" + cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.DAY_OF_MONTH) ;

			// If a value at the current Entry's date exists, increment it. Otherwise add it to the map.
			if(stats.get(key) != null){
				// Get the old value, add one, replace the old value.
				Long newVal = stats.get(key) + 1 ;
				stats.replace(key, newVal) ;
			} else{
				stats.put(key, new Long(1)) ;
			}
		}
		return stats ;
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

		return ok(accounts.render());
	}


	//================================================================================
	// Miscellaneous
	//================================================================================

	public static String formatTimestamp(final long t) {
		return new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").format(new Date(t));
	}

}