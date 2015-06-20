package org.kinetics.util.mail;

import java.util.List;

import org.kinetics.dao.project.Project;
import org.kinetics.dao.user.User;
import org.springframework.mail.MailException;

/**
 * Mailing layer service to send specific mail templates
 * 
 * @author akaverin
 * 
 */
public interface MailService {

	/**
	 * Sends email with confirmation
	 * 
	 * @param receiver
	 *            of the confirmation
	 * @param code
	 *            code to be confirmed via client UI
	 * @param token
	 *            optional token
	 * @throws MailException
	 */
	void sendConfirmationCode(User receiver, String code, String token);

	/**
	 * Sends new password to user
	 * 
	 * @param receiver
	 *            of new password
	 * @param token
	 *            to be sent
	 * @throws MailException
	 */
	void sendPasswordToken(User receiver, String token);

	/**
	 * Sends invitation email to patient
	 * 
	 * @param receiver
	 *            patient
	 * @param code
	 *            code to be confirmed via client UI
	 * @throws MailException
	 */
	void sendPatientInvitation(User receiver, String code);

	/**
	 * Send invitation to user created by Admin
	 * 
	 * @param receiver
	 *            user
	 * @param token
	 *            to be used during password initial setip
	 */
	void sendUserInvitation(User receiver, String token);

	/**
	 * Send Shared Test invitation
	 * 
	 * @param sender
	 *            sharing initiator
	 * @param email
	 *            of the email receiver
	 * @param project
	 *            to access test room
	 */
	void sendShareInvitation(User sender, String email, Project project);

	/**
	 * Send Shared Test notification
	 * 
	 * @param sender
	 *            sharing initiator
	 * @param receiver
	 *            notification receiver
	 * @param project
	 *            to access test room
	 */
	void sendShareNotification(User sender, User receiver, Project project);

	/**
	 * Send email
	 * 
	 * @param receivers
	 *            of mail
	 * @param message
	 *            to be a body
	 * @param url
	 *            to be embedded at the end
	 */
	void sendMessage(List<String> receivers, String message, String url);

}
