package org.kinetics.exception;

import org.kinetics.rest.Protocol.Errors;

import com.lohika.server.core.exception.RestException;

public class ProjectNotExistException extends RestException {

	private static final long serialVersionUID = -2979748828344875902L;

	public ProjectNotExistException() {
		super(Errors.PROJECT_DOESNT_EXIST);
	}

}
