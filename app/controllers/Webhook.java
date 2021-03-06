package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.PlayAuthenticate;
import com.typesafe.config.ConfigFactory;
import models.entries.Entry;
import models.entries.PushEntry;
import models.entries.TaskEntry;
import play.api.Play;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.Date;
import java.util.Map;

/**
 * Created by jrenner on 11/6/15.
 */
public class Webhook extends Controller {

    public static Result wunderlist() {
        JsonNode body = request().body().asJson();
        long userId = body.get("client").get("user_id").asLong();
        String title = body.get("after").get("title").asText();
        String taskId = body.get("after").get("id").asText();

        String operation = body.get("operation").textValue();

        if(operation.equals("create")) {
            String time = body.get("after").get("created_at").asText();
            //Format the string to remove non-parseable letters
            time = time.substring(0, time.indexOf("T")) + " " + time.substring(time.indexOf("T") + 1);
            time = time.substring(0, time.length()-1) ;

            // create TaskAction for creation
            Entry entry = TaskEntry.create(userId, taskId, title, time, "created") ;
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
                TaskEntry taskEntry = (TaskEntry)Entry.find.where().eq("taskId", taskId).findUnique();
                if(taskEntry == null){
                    taskEntry = TaskEntry.create(userId, taskId, title, start_time, "created");
                    taskEntry.save();
                }
                taskEntry.setEndTime(end_time);
                taskEntry.setTaskType("completed");
                taskEntry.save() ;
                System.out.println("Created TaskEntry for completion") ;
            }
        }

        return ok();
    }

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

    public static Result github() {

        JsonNode body = request().body().asJson() ;
        // Get the user id of the user who sent the push.
        long userId = body.get("sender").get("id").asLong();
        // Get the name of the user who pushed and the owner of the repo.
        String pusherName = body.get("pusher").get("name").asText() ;
        String repoOwner = body.get("repository").get("owner").get("name").asText() ;
        // Get the name of the repository pushed to.
        String repoName = body.get("repository").get("full_name").asText() ;
        // Get the timestamp of the push. (given in epoch time)
        long pushTime = body.get("repository").get("pushed_at").asLong() ;
        // Note that the number given by github is # seconds since 1970, but the Date takes # milliseconds since 1970.
        Date pushDate = new Date(pushTime * 1000) ;
        // Get the url to the head commit
        String commitURL = body.get("head_commit").get("url").asText() ;
        // Get the url to the repository
        String repositoryURL = body.get("repository").get("html_url").asText() ;
        // Get the commit message of the head commit.
        String commitMessage = body.get("head_commit").get("message").asText() ;

        // If the pusher is the repository owner (i.e. the current orwell user) then create an Entry.
        if(pusherName.equals(repoOwner)){
            PushEntry p = PushEntry.create(userId, pushDate, pusherName, repoName, commitURL, repositoryURL, commitMessage) ;
            p.save() ;
            System.out.println("Successfully created PushEntry") ;
        }
        // We are currently not saving push entries that come from users other than the current user (owner of the repo)

        return ok() ;
    }

}
