package org.kinetics.util.mail;

/**
 * @author akaverin
 */
public enum MailType {

	CONFIRMATION_CODE("Kinetics: Account Registration", "confirmationCode.vm"), RESET_PASSWORD(
			"Kinetics: Reset Password Request", "resetPassword.vm"), CUSTOMER_INVITATION(
			"Kinetics: Customer account Registration", "customerInvitation.vm"), PATIENT_INVITATION(
			"Kinetics: Account Registration", "patientInvitation.vm"), USER_INVITATION(
			"Kinetics: Your Account was created by Admin", "userInvitation.vm"), SHARE_INVITATION(
			"Kinetics: Test was shared with you!", "shareInvitation.vm"), SHARE_NOTIFICATION(
			"Kinetics: Test was shared with you!", "shareNotification.vm"), SHARE_BY_MAIL(
			"Kinetics: Test was shared with you!", "shareByMail.vm");

	private String subject;
	private String templateName;

	private MailType(String subject, String templateName) {
		this.subject = subject;
		this.templateName = templateName;
	}

	public String getSubject() {
		return subject;
	}

	public String getTemplateName() {
		return templateName;
	}
}
