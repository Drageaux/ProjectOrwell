package providers.wunderlist;

import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.exceptions.AccessTokenException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.google.inject.Inject;
import play.Application;
import play.Logger;
import play.libs.ws.WSResponse;
import service.WunderlistServicePlugin;

/**
 * Created by Austin on 10/25/2015.
 * Handles authorization for Wunderlist
 */
public class WunderlistAuthProvider extends
        OAuth2AuthProvider<WunderlistAuthUser, WunderlistAuthInfo>{

    public static final String CLIENT_ID_KEY = "clientId";
    public static final String PROVIDER_KEY = "wunderlist";
    public static final String URL_KEY = "userInfoUrl";

    private final WunderlistServicePlugin service;




    @Inject
    public WunderlistAuthProvider(Application app, WunderlistServicePlugin service) {
        super(app);
        this.service = service;
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

        // Get the user info
        final JsonNode result = service.getUserInfo(info);

        // Create the webhooks
        for(JsonNode n : service.getLists(info)) {
            service.createWebhook(info, n.get("id").asLong());
        }

        return new WunderlistAuthUser(result, info, state);

    }


    @Override
    public String getKey() {
        return PROVIDER_KEY ;
    }
}
