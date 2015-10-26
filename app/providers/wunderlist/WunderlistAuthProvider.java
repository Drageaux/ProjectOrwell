package providers.wunderlist;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feth.play.module.pa.exceptions.AccessTokenException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthInfo;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider;
import com.feth.play.module.pa.user.AuthUserIdentity;
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

    public static final String PROVIDER_KEY = "wunderlist";


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

        JsonNode data = Json.newObject()
                .put("id", info.getAccessToken()) ;

        return new WunderlistAuthUser(data, info, state) ;

    }

    @Override
    public String getKey() {
        return PROVIDER_KEY ;
    }
}
