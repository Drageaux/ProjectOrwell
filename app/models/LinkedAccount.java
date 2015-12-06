package models;

import javax.persistence.*;

import com.feth.play.module.pa.providers.oauth2.OAuth2AuthUser;
import com.feth.play.module.pa.user.AuthUser;
import models.entries.Entry;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
public class LinkedAccount extends AppModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	public Long id;

	@ManyToOne
	public User user;

	public String providerUserId;
	public String providerKey;

	public String providerAccessToken;

	@ManyToMany
	@JoinTable(
			name="linkedaccount_entry",
			joinColumns={@JoinColumn(name="linkedaccount_id", referencedColumnName="id")},
			inverseJoinColumns={@JoinColumn(name="entry_id", referencedColumnName="id")})
	public Set<Entry> entries;


	public static final Finder<Long, LinkedAccount> find = new Finder<Long, LinkedAccount>(
			Long.class, LinkedAccount.class);

	public static LinkedAccount findByProviderKey(final User user, String key) {
		return find.where().eq("user", user).eq("providerKey", key)
				.findUnique();
	}

	public static LinkedAccount create(final AuthUser authUser) {
		final LinkedAccount ret = new LinkedAccount();
		ret.update(authUser);
		return ret;
	}
	
	public void update(final AuthUser authUser) {
		this.providerKey = authUser.getProvider();
		this.providerUserId = authUser.getId();

        if(authUser instanceof OAuth2AuthUser) {
            this.providerAccessToken = ((OAuth2AuthUser) authUser).getOAuth2AuthInfo().getAccessToken();
        }
	}

	public static LinkedAccount create(final LinkedAccount acc) {
		final LinkedAccount ret = new LinkedAccount();
		ret.providerKey = acc.providerKey;
		ret.providerUserId = acc.providerUserId;

		return ret;
	}
}