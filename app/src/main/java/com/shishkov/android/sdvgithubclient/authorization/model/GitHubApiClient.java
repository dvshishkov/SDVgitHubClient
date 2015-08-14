package com.shishkov.android.sdvgithubclient.authorization.model;

import com.shishkov.android.sdvgithubclient.authorization.model.objects.SupportedAuthorization;

import org.eclipse.egit.github.core.Authorization;
import org.eclipse.egit.github.core.service.OAuthService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class GitHubApiClient {

	private static final String APP_NOTE_URL = "https://github.com/github/android";
	private static final String APP_NOTE = "SDVGitHubClient";
	private static final List<String> SCOPES = Arrays.asList("repo", "user");

	/**
	 * returns token_last_eight of SDVGitHubClient personal access token from GitHub
	 * IMPORTANT: we can`t get full Oauth token from GitHub by response
	 * (see: https://developer.github.com/changes/2014-12-08-removing-authorizations-token/)
	 * IMPORTANT: we can get and store full-length Oauth token only once - by creation.
	 * So we crated it, stored in account on device, then send each time to authorize.
	 * BUT if user: change token on GitHub manually or delete account or delete app
	 * user will have to delete it manually, because there is NO way we can get it by response.
	 */

	public static SupportedAuthorization getAuthorization(final SupportedOAuthService service) throws IOException {
		List<SupportedAuthorization> supportedAuthorizations = service.getSupportedAuthorizations();
		for (SupportedAuthorization auth : supportedAuthorizations) {
			if (isValidAuthorization(auth, SCOPES)) {
				return auth;
			}
		}
		return null;
	}

	/**
	 * creates SDVGitHubClient Personal access token on GitHub and returns this token as a String
	 */
	public static String createAuthorization(final OAuthService service) throws IOException {
		Authorization auth = new Authorization();
		auth.setNote(APP_NOTE);
		auth.setNoteUrl(APP_NOTE_URL);
		auth.setScopes(SCOPES);
		auth = service.createAuthorization(auth);
		return auth != null ? auth.getToken() : null;
	}

	private static boolean isValidAuthorization(final Authorization auth,
	                                            final List<String> requiredScopes) {
		if (auth == null)
			return false;

		if (!APP_NOTE.equals(auth.getNote()))
			return false;

		if (!APP_NOTE_URL.equals(auth.getNoteUrl()))
			return false;

		List<String> scopes = auth.getScopes();
		return scopes != null && scopes.containsAll(requiredScopes);
	}
}
