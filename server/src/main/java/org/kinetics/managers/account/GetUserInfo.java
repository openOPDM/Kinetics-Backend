package org.kinetics.managers.account;

import static org.kinetics.managers.account.GetUserInfo.METHOD;
import static org.kinetics.rest.Protocol.Arguments.USER;

import org.kinetics.dao.session.Session;
import org.kinetics.dao.user.User;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol.Managers;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.protocol.core.response.data.GenericResponseData;
import com.lohika.server.core.strategy.RequestDescriptor;

@Component
@RequestDescriptor(target = Managers.ACCOUNT_MANANGER, method = METHOD)
public class GetUserInfo extends AuthKineticsRequestStrategy {

	static final String METHOD = "getUserInfo";

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		GenericResponseData<User> data = new GenericResponseData<User>(USER,
				session.getUser());

		return ResponseFactory.makeSuccessDataResponse(data, requestData);
	}
}
