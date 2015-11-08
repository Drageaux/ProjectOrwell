package controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import com.typesafe.config.ConfigFactory;
import play.api.Play;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.Map;

/**
 * Created by jrenner on 11/6/15.
 */
public class Webhook extends Controller {

    public static Result facebook() {

        Map<String, String[]> a = request().queryString();
        System.out.println(a);
        if(!a.containsKey("hub.challenge") || !a.containsKey("hub.verify_token")) {
            System.out.println("Missing stuff?");
            return noContent();
        }

        String challenge = a.get("hub.challenge")[0];
        String token = a.get("hub.verify_token")[0];

        if(token.equals(ConfigFactory.load().getConfig("play-authenticate").getConfig("facebook").getString("verify_token"))) {
            return ok(challenge);
        }
        return noContent();
    }
}
