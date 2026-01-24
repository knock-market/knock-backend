package com.knock.auth.exception;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class MemberNameNotFoundException extends UsernameNotFoundException {

	public MemberNameNotFoundException() {
		super("Member name cannot found.");
	}

}
