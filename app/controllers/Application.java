package controllers;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit ;


import models.LinkedAccount;
import models.User;
import models.entries.CheckinEntry;
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

		List<Entry> entries = Entry.find
								.where()
								.eq("linkedAccounts.user.id", localUser.id)
								.orderBy("end_time desc")
								.findList() ;

		return ok(index.render(entries));
    }

	//================================================================================
	// Statistics page
	//================================================================================
	@Restrict(@Group(Application.USER_ROLE))
	public static Result statistics() {
		final User localUser = getLocalUser(session());
		//Get the current date value (use this later to add a blank value to the calculated maps for each provider)
		Long currentTime = new Long(new Date().getTime()) ;

		//This will hold the averages for 1 week for each provider.
		Map<String, Long> averages = new HashMap<String, Long>() ;

		//Wunderlist tasks
		List<TaskEntry> tasks = TaskEntry.find
				.where()
				.eq("linkedAccounts.user.id", localUser.id)
				.orderBy("end_time desc")
				.findList();

		//Get the averages, if needed.
		if(!tasks.isEmpty()){
			//I know, this way of doing an array conversion as a parameter is horrendous.  Sorry.
			averages.put("Wunderlist", getAverageWeek(tasks.toArray(new TaskEntry[tasks.size()]))) ;
		}

		//We have to convert the tasks List to an array of TaskEntries before passing it into getCounts.
		Map<Long, Long> taskCounts = getCounts(tasks.toArray(new TaskEntry[tasks.size()]));
		taskCounts.put(currentTime, new Long(0)) ;

		//Github pushes
		List<PushEntry> pushes = PushEntry.find
				.where()
				.eq("linkedAccounts.user.id", localUser.id)
				.orderBy("end_time desc")
				.findList();
		if(!pushes.isEmpty()){
			averages.put("Github", getAverageWeek(pushes.toArray(new PushEntry[pushes.size()]))) ;
		}
		Map<Long, Long> pushCounts = getCounts(pushes.toArray(new PushEntry[pushes.size()]));
		pushCounts.put(currentTime, new Long(0)) ;

		//Facebook checkins
		List<CheckinEntry> checkins = CheckinEntry.find
				.where()
				.eq("linkedAccounts.user.id", localUser.id)
				.orderBy("end_time desc")
				.findList();
		if(!pushes.isEmpty()){
			averages.put("Github", getAverageWeek(pushes.toArray(new CheckinEntry[pushes.size()]))) ;
		}
		Map<Long, Long> checkinCounts = getCounts(pushes.toArray(new CheckinEntry[pushes.size()]));
		pushCounts.put(currentTime, new Long(0)) ;

		return ok(statistics.render(taskCounts, pushCounts, checkinCounts, averages));
	}

	//This will create a mapping between the date and the number of entries that were created on that date.
	private static Map<Long, Long> getCounts(Entry[] entries){
		Map<Long,Long> stats = new HashMap<Long,Long>();
		for(Entry e: entries){
			//The key being used to store values is the string representation of the start date.
			Date date = e.getStartTime() ;
			Calendar cal = Calendar.getInstance() ;
			cal.setTime(date) ;
			//Because we only care about the date, set the hour/minute/second/millisecond all to zero.
			cal.set(Calendar.HOUR, 0) ;
			cal.set(Calendar.MINUTE, 0) ;
			cal.set(Calendar.SECOND, 0) ;
			cal.set(Calendar.MILLISECOND, 0) ;

			Long key = cal.getTimeInMillis() ;

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

	private static long getAverageWeek(Entry[] entries){
		Date d1, d2 ;
		long avg ;
		//Oldest entry
		d1 = entries[entries.length-1].getEndTime() ;
		//Current date
		d2 = new Date() ;
		//Get the difference of the two times.
		long diff = d2.getTime() - d1.getTime() ;
		int dayDiff = new Long(TimeUnit.MILLISECONDS.toDays(diff)).intValue() ;
		//Calculate the average # of entries per week
		avg = (entries.length * 7) / (new Long(dayDiff + 1).longValue()) ;
		return avg ;
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