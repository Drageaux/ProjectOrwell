package providers.wunderlist;

import com.feth.play.module.pa.providers.oauth2.OAuth2AuthInfo;

/**
 * Created by Austin on 10/25/2015.
 * Handles authorization for Wunderlist
 */
public class WunderlistAuthInfo extends OAuth2AuthInfo{

    //The token is the access_token provided by wunderlist api.
    public WunderlistAuthInfo(String token) {
        super(token);
    }

}