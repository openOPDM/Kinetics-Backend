package org.kinetics.managers.account;

import org.kinetics.dao.session.Session;
import org.kinetics.dao.session.SessionRepository;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol.Managers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.server.core.strategy.RequestDescriptor;

@Component
@RequestDescriptor(target = Managers.ACCOUNT_MANANGER, method = Logout.METHOD)
public class Logout extends AuthKineticsRequestStrategy {

	static final String METHOD = "logout";
	@Autowired
	private SessionRepository sessionRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		sessionRepo.delete(session);

		// TODO: remove all expired sessions for current user to avoid tangling
		// sessions. Consider some kind of daily job to check if user session
		// alive for too long

		return ResponseFactory.makeSuccessResponse(requestData);
	}

}
