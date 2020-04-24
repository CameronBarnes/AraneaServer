package com.cameronbarnes.AraneaServer.crypto.credentials;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Arrays;

public class Password {
	
	private char[] mPassword;
	private boolean mIsCleared;
	
	public Password(char[] pass) {
		mPassword = pass;
		mIsCleared = false;
	}
	
	public char[] getValue() {
		return mPassword;
	}
	
	public boolean checkValue(char[] chars) {
		return Arrays.equals(chars, mPassword);
	}
	
	public String getString() {
		return Arrays.toString(mPassword);
	}
	
	public void setPassword(char[] chars) {
		clear();
		mPassword = chars;
		mIsCleared = false;
	}
	
	public void clear() {
		mPassword = RandomStringUtils.randomAlphanumeric(mPassword.length).toCharArray();
		System.gc();
		mIsCleared = true;
	}
	
	public boolean isCleared() {
		return mIsCleared;
	}
	
	@Override
	public boolean equals(Object o) {
		
		if (o.getClass() == Password.class) {
			return ((Password) o).checkValue(mPassword);
		}
		return false;
		
	}
	
}
