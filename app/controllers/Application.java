package controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import models.LinkedAccount;
import models.User;
import models.entries.Entry;
import models.entries.PushEntry;
import models.entries.TaskEntry;
import org.springframework.scheduling.config.TaskNamespaceHandler;
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

		List<Entry> entries = Entry.find
							.where()
							.eq("linkedAccounts.user.id", localUser.id)
							.orderBy("end_time desc")
							.findList();

		/* Not sure why this block is here.
		for(Entry e : tasks) {
			for(LinkedAccount a : e.getLinkedAccounts()) {
				System.out.println(a.user);
			}
		}
		*/

		List<TaskEntry> taskEntries = new ArrayList<TaskEntry>();
		List<PushEntry> pushEntries = new ArrayList<PushEntry>() ;
//		for (int i=0; i<tasks.size();i++){
//			entry = tasks.get(i) ;
//			if(entry instanceof TaskEntry){
//				taskEntries.add((TaskEntry)entry);
//			} else if(entry instanceof PushEntry){
//				pushEntries.add((PushEntry)entry) ;
//			}
//		}

		return ok(index.render(entries));
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
	// Accounts Page
	//================================================================================

	@Restrict(@Group(Application.USER_ROLE))
	public static Result accounts() {

		return ok(accounts.render());
	}

	public static Result deactivateLinkedAccount(String provider) {

		// return if link account is not active (doesn't have a provider)
		// this would prevent failure from hardcoding bad URLs
		final User localUser = getLocalUser(session());

		// delete the linked account to a provider
		if (localUser.linkedAccounts.size() > 1) {
			LinkedAccount.findByProviderKey(localUser, provider).delete();
		}

		return ok(accounts.render());
	}


	//================================================================================
	// Miscellaneous
	//================================================================================

	public static String formatTimestamp(final long t) {
		return new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").format(new Date(t));
	}

}