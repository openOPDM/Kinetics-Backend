package org.kinetics.request;

import java.util.List;

import org.codehaus.jackson.type.TypeReference;
import org.kinetics.dao.session.Session;
import org.springframework.beans.factory.annotation.Autowired;

import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.server.core.helpers.SessionManagementHelper;
import com.lohika.server.core.strategy.RequestStrategy;
import com.lohika.server.core.strategy.SessionProvider;
import com.lohika.server.core.validator.AuthRequired;

/**
 * Base {@link RequestStrategy} implementation for authorized request. Handy for
 * Session class modification and other permission checks stuff
 * 
 * @author akaverin
 * 
 */
@AuthRequired
public abstract class AuthKineticsRequestStrategy implements RequestStrategy {

	public static final TypeReference<List<Integer>> LIST_INTEGER_TYPE = new TypeReference<List<Integer>>() {
	};
	protected static final TypeReference<List<String>> LIST_STRING_TYPE = new TypeReference<List<String>>() {
	};

	@Autowired
	private SessionProvider<Session> sessionProvider;

	// TODO: issue if assign @Transactional, needs investigation!
	@Override
	public ResponseContainer execute(RequestFunction requestData) {

		String sessionToken = SessionManagementHelper
				.getSessionToken(requestData);
		Session session = sessionProvider.find(sessionToken);

		return processAuthenticatedRequest(requestData, session);
	}

	protected abstract ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session);

}
