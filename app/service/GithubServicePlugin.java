package service;

import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthInfo;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider;
import com.google.inject.Inject;
import play.Application;
import play.Configuration;
import play.Plugin;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

/**
 * Created by Austin on 11/12/2015.
 */
public class GithubServicePlugin extends Plugin {


    public static final String PROVIDER_KEY = "github";
    public static final String CLIENT_ID_KEY = "clientId";
    public static final String CLIENT_SECRET_KEY = "clientSecret" ;

    private Application app;

    private Configuration config = null;

    private final long timeout = 10000L;

    private final String CLIENT_ID_PARAM = "client_id" ;
    private final String CLIENT_SECRET_PARAM = "client_secret" ;


    @Inject
    public GithubServicePlugin(final Application app) {
        this.app = app;
        config = app.configuration().getConfig("apis").getConfig(PROVIDER_KEY);
    }

    public void onStart() {

    }

    public void onStop() {
    }

    public boolean enabled() {
        return true;
    }

    private WSRequest genRequest(String url, OAuth2AuthInfo authInfo) {
        return WS
                .url(url)
                .setQueryParameter(CLIENT_ID_PARAM, app.configuration().getString(CLIENT_ID_KEY))
                .setQueryParameter(CLIENT_SECRET_PARAM, app.configuration().getString(CLIENT_SECRET_KEY))
                .setQueryParameter(OAuth2AuthProvider.Constants.ACCESS_TOKEN,
                        authInfo.getAccessToken());
    }

    //Method is currently not used.
    public JsonNode getUserInfo(OAuth2AuthInfo authInfo) {
        final WSResponse r = genRequest(config.getString("userInfo"), authInfo).get()
                .get(timeout);
        return r.asJson().get(0);
    }

    //Get all repositories belonging to current user
    public JsonNode getRepos(OAuth2AuthInfo authInfo) {
        //Build url
        String url = config.getString("userInfo") + "/repos" ;

        final WSResponse r = genRequest(url, authInfo).get().get(timeout);

        //System.out.println(r.getBody());
        return r.asJson();
    }

    //Get a single repository with the specified id
    public JsonNode getRepo(OAuth2AuthInfo authInfo, String ownerName, String repoName) {

        String url = config.getString("repositories") + "/"+ ownerName + "/" + repoName ;

        final WSResponse r = genRequest(url, authInfo).get().get(timeout);

        return r.asJson();
    }

    public void createWebhook(OAuth2AuthInfo authInfo, String hooks_url) {
        System.out.println("Creating webhooks with url: " + hooks_url) ;
        long hookId ;

        // Delete all webhooks that connect to our website
        for(JsonNode hook : getWebhooks(authInfo, hooks_url)) {
            hookId = hook.get("id").asLong();
            // Check if the current webhook is connected to our website
            if(hook.get("config").get("url").asText().startsWith(app.configuration().getString("root"))){
                deleteWebhook(authInfo, hooks_url, hookId) ;
            }
        }

        //Build the config param for webhook post payload
        JsonNode webhookConfig = Json.newObject()
                .put("url", app.configuration().getString("root") + "/webhook/github") // Our url for webhooks to hit
                .put("content_type", "json") // Define the data type received by webhook (json/form)
                .put("secret", "super_secret") // Optional string, probably should save this to verify incoming data from github
                .put("insecure_ssl", "1") ; // Will github verify our url when sending webhook data? (0=verified, 1=not verified)

        // Set the payload being posted to create the webhook.
        JsonNode webhookPost = Json.newObject()
                .put("name", "web")
                .set("config", webhookConfig) ;

        // Make the post request.
        final WSResponse r = genRequest(hooks_url, authInfo)
                .post(webhookPost)
                .get(timeout);

    }

    public void deleteWebhook(OAuth2AuthInfo authInfo, String hooks_url, long hookId) {

        // Build url in the form "https://api.github.com/repos/:ownerName/:repoName/hooks/:hookId"
        String url = hooks_url + "/" + hookId ;

        //No response is returned, but selected webhook is successfully deleted.
        final WSResponse r = genRequest(url, authInfo)
                .delete()
                .get(timeout);

    }

    // Get the webhooks for a single specified repository
    public JsonNode getWebhooks(OAuth2AuthInfo authInfo, String hooks_url) {

        final WSResponse r = genRequest(hooks_url , authInfo)
                .get()
                .get(timeout);
        return r.asJson();
    }

}