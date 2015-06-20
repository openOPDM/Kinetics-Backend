package org.kinetics.exception;

import static org.kinetics.rest.Protocol.Errors.USER_DOESNT_EXIST;

import org.kinetics.rest.Protocol.Errors;

import com.lohika.server.core.exception.RestException;

/**
 * {@link RestException} specification for {@link Errors#USER_DOESNT_EXIST}
 * error
 * 
 * @author akaverin
 * 
 */
public class UserNotExistException extends RestException {

	private static final long serialVersionUID = 9120980627379771050L;

	public UserNotExistException() {
		super(USER_DOESNT_EXIST);
	}

}
