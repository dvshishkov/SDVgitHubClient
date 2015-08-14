package com.shishkov.android.sdvgithubclient.authorization.model.exceptions;

import java.io.IOException;

/**
 * Exception class to be thrown when server responds with a 401 and
 * an X-GitHub-OTP: required;:2fa-type header.
 * This exception wraps an {@link java.io.IOException} that is the actual exception
 * that occurred when the request was made.
 */
public class TwoFactorAuthException extends IOException {

	protected final IOException cause;

	/**
	 * Two-factor authentication type
	 */
	public final int twoFactorAuthType;

	/**
	 * Create two-factor authentication exception
	 */
	public TwoFactorAuthException(IOException cause, int twoFactorAuthType) {
		this.cause = cause;
		this.twoFactorAuthType = twoFactorAuthType;
	}

	@Override
	public String getMessage() {
		return cause != null ? cause.getMessage() : super.getMessage();
	}

	@Override
	public IOException getCause() {
		return cause;
	}
}