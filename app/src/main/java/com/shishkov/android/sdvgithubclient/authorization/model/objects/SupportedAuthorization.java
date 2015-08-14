package com.shishkov.android.sdvgithubclient.authorization.model.objects;

import org.eclipse.egit.github.core.Authorization;

/**
 * Due to changes in GitHub OAuth Authorizations API (https://developer.github.com/changes/2014-12-08-removing-authorizations-token/)
 * new response attributes were added to replace getting full Oauth token, one of them is token_last_eight
 */


public class SupportedAuthorization extends Authorization {

	private String token_last_eight;

	public String getTokenLastEight() {
		return token_last_eight;
	}

	public void setTokenLastEight(String token_last_eight) {
		this.token_last_eight = token_last_eight;
	}
}
