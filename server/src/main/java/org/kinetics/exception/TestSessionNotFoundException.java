package org.kinetics.exception;

import org.kinetics.rest.Protocol.Errors;

import com.lohika.server.core.exception.RestException;

public class TestSessionNotFoundException extends RestException {

	private static final long serialVersionUID = 92491811610464154L;

	public TestSessionNotFoundException() {
		super(Errors.TEST_NOT_FOUND);

	}

}
