package org.kinetics.util.mail;

import java.util.List;

import org.kinetics.dao.project.Project;
import org.kinetics.dao.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fake {@link MailService} implementation to be used in UT environment. Doesn't
 * send real mails, only logs them.
 * 
 * @author akaverin
 * 
 */
public class FakeMailService implements MailService {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(FakeMailService.class);

	// TODO: optionally to check mail is sent in UT - we can add counter for
	// each mail type...

	@Override
	public void sendConfirmationCode(User receiver, String code, String token) {
		LOGGER.info(
				"Sending confirmation email with code {} and token {} to {}",
				code, token, receiver);
	}

	@Override
	public void sendPasswordToken(User receiver, String token) {
		LOGGER.info("Sending resetPass email with pass {} to {}", token,
				receiver);
	}

	@Override
	public void sendPatientInvitation(User receiver, String code) {
		LOGGER.info("Sending invitation email with code {} to patient {}",
				code, receiver);
	}

	@Override
	public void sendUserInvitation(User receiver, String token) {
		LOGGER.info("Sending invitation email with token {} to user {}", token,
				receiver);
	}

	@Override
	public void sendShareInvitation(User sender, String email, Project project) {
		LOGGER.info(
				"Sending share invitation email with user id {} to email {}",
				sender.getId(), email);
	}

	@Override
	public void sendShareNotification(User sender, User receiver,
			Project project) {
		LOGGER.info(
				"Sending share notification email with user id {} to user {}",
				sender.getId(), receiver);
	}

	@Override
	public void sendMessage(List<String> receivers, String message, String url) {
		LOGGER.info(
				"Sending share via mail with receivers: {}, body: {} and URL: {}",
				receivers, message, url);
	}

}
