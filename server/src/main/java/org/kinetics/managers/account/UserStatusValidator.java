package org.kinetics.managers.account;

import static org.kinetics.dao.user.UserStatus.DISABLED;
import static org.kinetics.dao.user.UserStatus.WAITING_CONFIRMATION;
import static org.kinetics.dao.user.UserStatus.WAITING_PASS;
import static org.kinetics.rest.Protocol.Errors.CREDENTIALS_INVALID;
import static org.kinetics.rest.Protocol.Errors.USER_DISABLED;
import static org.kinetics.rest.Protocol.Errors.USER_IS_ACTIVE;
import static org.kinetics.rest.Protocol.Errors.USER_NOT_ACTIVE;

import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserStatus;

import com.lohika.server.core.exception.RestException;

/**
 * Helper utility class to perform commons validation for {@link UserStatus}
 */
public class UserStatusValidator {

	static void isNotWaitingConfirmation(User user) {
		if (WAITING_CONFIRMATION.equals(user.getStatus())) {
			throw new RestException(USER_NOT_ACTIVE);
		}
	}

	static void isNotActive(User user) {
		if (UserStatus.ACTIVE.equals(user.getStatus())) {
			throw new RestException(USER_IS_ACTIVE);
		}
	}

	static void isNotDisabled(User user) {
		if (DISABLED.equals(user.getStatus())) {
			throw new RestException(USER_DISABLED);
		}
	}

	static void isNotWaitingPassword(User user) {
		if (WAITING_PASS.equals(user.getStatus())) {
			throw new RestException(CREDENTIALS_INVALID);
		}
	}

}
