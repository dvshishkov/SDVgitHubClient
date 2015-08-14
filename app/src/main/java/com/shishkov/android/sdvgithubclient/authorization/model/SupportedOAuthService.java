package com.shishkov.android.sdvgithubclient.authorization.model;

import com.google.gson.reflect.TypeToken;
import com.shishkov.android.sdvgithubclient.authorization.model.objects.SupportedAuthorization;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.PagedRequest;
import org.eclipse.egit.github.core.service.OAuthService;

import java.io.IOException;
import java.util.List;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_AUTHORIZATIONS;

public class SupportedOAuthService extends OAuthService {

	public SupportedOAuthService(GitHubClient client) {
		super(client);
	}

	public List<SupportedAuthorization> getSupportedAuthorizations() throws IOException {
		PagedRequest<SupportedAuthorization> request = createPagedRequest();
		request.setUri(SEGMENT_AUTHORIZATIONS);
		request.setType(new TypeToken<List<SupportedAuthorization>>() {
		}.getType());
		return getAll(request);
	}

}
