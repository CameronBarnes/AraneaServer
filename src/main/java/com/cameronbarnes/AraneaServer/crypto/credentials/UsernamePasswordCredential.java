package com.cameronbarnes.AraneaServer.crypto.credentials;

import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;

public class UsernamePasswordCredential {
	
	private String mUsername;
	private Password mPassword;
	private boolean mIsCleared;
	
	public UsernamePasswordCredential(String username, Password password) {
		
		mUsername = username;
		mPassword = password;
		mIsCleared = false;
		
	}
	
	public void setUsername(String username) {
		mUsername = username;
	}
	
	public void setPassword(Password password) {
		mPassword.clear();
		mPassword = password;
	}
	
	public Password getPassword() {
		return mPassword;
	}
	
	public boolean isCleared() {
		if (mPassword.isCleared()) {
			return mIsCleared;
		}
		return false;
	}
	
	public void clear() {
		mUsername = RandomStringUtils.randomAlphanumeric(mUsername.length());
		mPassword.clear();
	}
	
	public boolean compareValues(@NotNull UsernamePasswordCredential credential) {
		
		if (credential.mUsername.equals(mUsername)) {
			return credential.mPassword.equals(mPassword);
		}
		return false;
		
	}
	
	@Override
	public boolean equals(Object o) {
		
		if (o.getClass() == UsernamePasswordCredential.class) {
			if (((UsernamePasswordCredential) o).mUsername.equals(mUsername)) {
				return ((UsernamePasswordCredential) o).mPassword.equals(mPassword);
			}
		}
		return false;
		
	}
	
}
