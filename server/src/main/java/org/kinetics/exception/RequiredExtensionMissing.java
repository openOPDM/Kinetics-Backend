package org.kinetics.exception;

import org.kinetics.rest.Protocol.Errors;

import com.lohika.server.core.exception.RestException;

public class RequiredExtensionMissing extends RestException {

	private static final long serialVersionUID = -945038094349738739L;

	public RequiredExtensionMissing() {
		super(Errors.EXTENSION_REQUIRED_MISSING);
	}

}
