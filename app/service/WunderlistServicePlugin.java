package service;

import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthInfo;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider;
import com.feth.play.module.pa.service.UserServicePlugin;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.google.inject.Inject;
import models.User;
import play.Application;


import play.Configuration;
import play.Plugin;


import play.Application;
import play.libs.Json;
import play.libs.oauth.OAuth;
import play.libs.ws.WS;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

public class WunderlistServicePlugin extends Plugin {


    public static final String CLIENT_ID_KEY = "clientId";
    public static final String PROVIDER_KEY = "wunderlist";
    public static final String URL_KEY = "userInfoUrl";

    private static final String TOKEN_HEADER = "X-Access-Token";
    private static final String CLIENT_ID_HEADER = "X-Client-ID";

    private Application app;

    private Configuration config = null;
    private String clientId = null;

    private final long timeout = 10000L;


    @Inject
    public WunderlistServicePlugin(final Application app) {
        this.app = app;
        config = app.configuration().getConfig("apis").getConfig("wunderlist");
        clientId = app.configuration().getConfig("play-authenticate").getConfig(PROVIDER_KEY).getString(CLIENT_ID_KEY);
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
                .setHeader(TOKEN_HEADER, authInfo.getAccessToken())
                .setHeader(CLIENT_ID_HEADER, clientId)
                .setQueryParameter(OAuth2AuthProvider.Constants.ACCESS_TOKEN,
                        authInfo.getAccessToken());
    }

    public JsonNode getUserInfo(OAuth2AuthInfo authInfo) {
        final WSResponse r = genRequest(config.getString("userInfo"), authInfo).get()
                .get(timeout);
        return r.asJson().get(0);
    }

    public JsonNode getLists(OAuth2AuthInfo authInfo) {
        final WSResponse r = genRequest(config.getString("lists"), authInfo).get().get(timeout);

        //System.out.println(r.getBody());
        return r.asJson();
    }

    public JsonNode getList(OAuth2AuthInfo authInfo, long listId) {
        final WSResponse r = genRequest(config.getString("lists") + "/" + listId, authInfo).get().get(timeout);

        return r.asJson();
    }

    public void createWebhook(OAuth2AuthInfo authInfo, long listId) {

        // Deduplicate webhooks
        long revision = getList(authInfo, listId).get("revision").asLong();

        for(JsonNode hook : getWebhooks(authInfo, listId)) {
            //System.out.println(hook);
            deleteWebhook(authInfo, hook.get("id").asLong(), revision);
        }

        JsonNode webhookPost = Json.newObject()
                .put("list_id", listId)
                .put("url", app.configuration().getString("root") + "/webhook/wunderlist")
                .put("processor_type", "generic")
                .put("configuration", "");



        final WSResponse r = genRequest(config.getString("webhooks"), authInfo)
                .post(webhookPost)
                .get(timeout);

        //System.out.println(r.getBody());
    }

    public void deleteWebhook(OAuth2AuthInfo authInfo, long hookId, long revision) {

        final WSResponse r = genRequest(config.getString("webhooks") + "/" + hookId, authInfo)
                .setQueryParameter("revision", ""+revision)
                .delete()
                .get(timeout);

    }

    public JsonNode getWebhooks(OAuth2AuthInfo authInfo, long listId) {
        final WSResponse r = genRequest(config.getString("webhooks"), authInfo)
                .setQueryParameter("list_id", ""+listId)
                .get()
                .get(timeout);
        return r.asJson();
    }

}
