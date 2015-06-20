package org.kinetics.dao.session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.server.core.strategy.SessionProvider;

@Component
public class SessionProviderImpl implements SessionProvider<Session> {

	@Autowired
	private SessionRepository sessionRepo;

	@Override
	public Session find(String sessionToken) {
		return sessionRepo.findOne(sessionToken);
	}

	@Override
	public void invalidate(String sessionToken) {
		if (sessionRepo.exists(sessionToken)) {
			sessionRepo.delete(sessionToken);
		}
	}

}
