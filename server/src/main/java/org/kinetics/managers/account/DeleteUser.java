package org.kinetics.managers.account;

import static org.kinetics.managers.account.DeleteUser.METHOD;
import static org.kinetics.rest.Protocol.Managers.ACCOUNT_MANANGER;

import org.kinetics.dao.session.Session;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.server.core.strategy.RequestDescriptor;

@Component
@RequestDescriptor(target = ACCOUNT_MANANGER, method = METHOD)
public class DeleteUser extends AuthKineticsRequestStrategy {

	static final String METHOD = "deleteUser";

	@Autowired
	private UserService userService;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		userService.deleteUser(session);

		return ResponseFactory.makeSuccessResponse(requestData);
	}
}
