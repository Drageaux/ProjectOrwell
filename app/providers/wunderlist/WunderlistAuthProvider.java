package providers.wunderlist;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feth.play.module.pa.exceptions.AccessTokenException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthInfo;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.user.EmailIdentity;
import com.feth.play.module.pa.user.NameIdentity;
import com.google.inject.Inject;
import play.Application;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;

/**
 * Created by Austin on 10/25/2015.
 * Handles authorization for Wunderlist
 */
public class WunderlistAuthProvider extends
        OAuth2AuthProvider<WunderlistAuthUser, WunderlistAuthInfo>{

    public static final String CLIENT_ID_KEY = "clientId";
    public static final String PROVIDER_KEY = "wunderlist";
    public static final String URL_KEY = "userInfoUrl";

    private static final String TOKEN_HEADER = "X-Access-Token";
    private static final String CLIENT_ID_HEADER = "X-Client-ID";


    @Inject
    public WunderlistAuthProvider(Application app) {
        super(app);
    }

    @Override
    protected WunderlistAuthInfo buildInfo(WSResponse r) throws AccessTokenException {
        if (r.getStatus() >= 400) {
            throw new AccessTokenException(r.toString());
        } else {
            final JsonNode result = r.asJson();
            Logger.debug(result.asText());
            return new WunderlistAuthInfo(result.get(
                    OAuth2AuthProvider.Constants.ACCESS_TOKEN).asText());
        }
    }


    @Override
    protected AuthUserIdentity transform(WunderlistAuthInfo info, String state) throws AuthException {
        final String clientId = getConfiguration().getString(CLIENT_ID_KEY);
        final String url = getConfiguration().getString(URL_KEY);

        final WSResponse r = WS
                .url(url)
                .setHeader(TOKEN_HEADER, info.getAccessToken())
                .setHeader(CLIENT_ID_HEADER, clientId)
                .setQueryParameter(OAuth2AuthProvider.Constants.ACCESS_TOKEN,
                        info.getAccessToken()).get()
                .get(getTimeout());

        // Result comes back as an array... for some reason...
        final JsonNode result = r.asJson().get(0);

        if (result.get(OAuth2AuthProvider.Constants.ERROR) != null) {
            throw new AuthException(result.get(
                    OAuth2AuthProvider.Constants.ERROR).asText());
        } else {
            Logger.debug(result.toString());
            //JsonNode res = getWunderListWebhook(result, info, clientId);
            return new WunderlistAuthUser(result, info, state);
        }

    }

    private JsonNode getWunderListWebhook(JsonNode result, WunderlistAuthInfo info, String cliendId){
        JsonNode webhookPost = Json.newObject()
                .put("list_id", result.get("id").intValue())
                .put("url", "metaknight.student.rit.edu/webhook/wunderlist")
                .put("processor_type", "generic")
                .put("configuration", "");

        final WSResponse r = WS
                .url("a.wunderlist.com/api/v1/webhooks")
                .setHeader("X-Client-ID", cliendId)
                .setHeader("X-Access-Token", OAuth2AuthProvider.Constants.ACCESS_TOKEN)
                .post(webhookPost)
                .get(10000);

        System.out.println("Getting Web Hook -------------");
        return r.asJson();
    }

    @Override
    public String getKey() {
        return PROVIDER_KEY ;
    }
}
