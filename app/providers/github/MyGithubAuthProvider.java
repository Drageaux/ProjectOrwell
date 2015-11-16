package providers.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.oauth2.github.GithubAuthInfo;
import com.feth.play.module.pa.providers.oauth2.github.GithubAuthProvider;
import com.feth.play.module.pa.providers.oauth2.github.GithubAuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.google.inject.Inject;
import play.Application;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import service.GithubServicePlugin;

/**
 * Created by Austin on 11/12/2015.
 *
 * This overwrites the built-in github oauth2 implementation for attaching webhooks.
 */
public class MyGithubAuthProvider extends GithubAuthProvider {

    private final GithubServicePlugin service ;

    private static final String USER_INFO_URL_SETTING_KEY = "userInfoUrl";

    @Inject
    public MyGithubAuthProvider(Application application, GithubServicePlugin service){
        super(application) ;
        this.service = service ;
    }

    @Override
    protected GithubAuthUser transform(final GithubAuthInfo info, String state) throws AuthException {

        // Repeated code from superclass ---
        final String url = getConfiguration().getString(
                USER_INFO_URL_SETTING_KEY);

        final WSResponse r = WS
                .url(url)
                .setQueryParameter(Constants.ACCESS_TOKEN,
                        info.getAccessToken()).get()
                .get(getTimeout());

        final JsonNode result = r.asJson();
        if (result.get(Constants.ERROR) != null) {
            throw new AuthException(result.get(
                    Constants.ERROR).asText());
        }
        // --- End repeated code


        String hooks_url ;

        // Create the webhooks
        for(JsonNode n : service.getRepos(info)) {
            hooks_url = n.get("hooks_url").asText();
            service.createWebhook(info, hooks_url);
        }

        return new GithubAuthUser(result, info, state);

    }



}
