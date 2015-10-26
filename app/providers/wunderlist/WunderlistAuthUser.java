package providers.wunderlist;

import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.providers.oauth2.BasicOAuth2AuthUser;

/**
 * Created by Austin on 10/25/2015.
 * Handles authorization for Wunderlist
 */
public class WunderlistAuthUser extends BasicOAuth2AuthUser {

    private abstract class Constants{
        public static final String ID = "id";
    }

    public WunderlistAuthUser(final JsonNode node, final WunderlistAuthInfo info, final String state){
        super(node.get(Constants.ID).asText(), info, state);
    }

    @Override
    public String getEmail() {
        return null;
    }

    @Override
    public String getProvider() {
        return WunderlistAuthProvider.PROVIDER_KEY ;
    }

    @Override
    public String getName() {
        return null;
    }


}
