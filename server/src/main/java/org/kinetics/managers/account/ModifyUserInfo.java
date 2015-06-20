package org.kinetics.managers.account;

import static org.kinetics.managers.account.ModifyUserInfo.METHOD;

import org.kinetics.dao.session.Session;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserRepository;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol.Managers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.server.core.strategy.RequestDescriptor;

@Component
@RequestDescriptor(target = Managers.ACCOUNT_MANANGER, method = METHOD)
public class ModifyUserInfo extends AuthKineticsRequestStrategy {

	static final String METHOD = "modifyUserInfo";

	@Autowired
	private UserRepository userRepo;
	@Autowired
	private UserService userService;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		User currentUser = session.getUser();
		if (userService.updateUserOptionalData(currentUser, requestData)) {
			userRepo.save(currentUser);
		}
		return ResponseFactory.makeSuccessResponse(requestData);
	}

}
