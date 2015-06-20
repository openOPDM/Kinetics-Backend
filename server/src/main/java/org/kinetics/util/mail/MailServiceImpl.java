package org.kinetics.util.mail;

import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.kinetics.rest.Protocol.Arguments.MESSAGE;
import static org.kinetics.rest.Protocol.Arguments.URL;
import static org.springframework.util.StringUtils.hasText;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.velocity.app.VelocityEngine;
import org.kinetics.dao.project.Project;
import org.kinetics.dao.user.User;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.ui.velocity.VelocityEngineUtils;

/**
 * Default {@link MailService} implementation. Send real mails to the web
 * 
 * @author akaverin
 * 
 */
public class MailServiceImpl implements MailService {

	private static final String TOKEN_B64 = "tokenB64";

	private static final String EMAIL_B64 = "emailB64";

	private static final String USER_B64 = "userB64";

	private static final String PROJECT_B64 = "projectB64";

	private static final String MAIL_TEMPLATES_PATH = "/mail-templates/";

	private JavaMailSender mailSender;

	private VelocityEngine velocityEngine;

	private String adminEmail;

	private Properties properties;

	public MailServiceImpl() {
		try {
			properties = new Properties();
			InputStream in = getClass().getResourceAsStream("/site.properties");
			properties.load(in);
			in.close();
		} catch (IOException e) {
			throw new RuntimeException("Failed to load site.properties file", e);
		}
	}

	@Override
	public void sendConfirmationCode(User receiver, String code, String token) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("user", receiver);
		model.put("code", code);
		if (hasText(token)) {
			model.put(TOKEN_B64, encodeBase64String(token.getBytes()));
		} else {
			model.put(TOKEN_B64, "");
		}
		setupEncodedData(receiver.getEmail(), code, model);
		setupUrlParams(model);
		sendMimeMessage(receiver.getEmail(), MailType.CONFIRMATION_CODE, model);
	}

	@Override
	public void sendPasswordToken(User receiver, String token) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("user", receiver);
		setupEncodedData(receiver.getEmail(), token, model);
		setupUrlParams(model);
		sendMimeMessage(receiver.getEmail(), MailType.RESET_PASSWORD, model);
	}

	@Override
	public void sendPatientInvitation(User receiver, String code) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("user", receiver);
		setupEncodedData(receiver.getEmail(), code, model);
		setupUrlParams(model);
		sendMimeMessage(receiver.getEmail(), MailType.PATIENT_INVITATION, model);
	}

	@Override
	public void sendUserInvitation(User receiver, String token) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("user", receiver);
		setupEncodedData(receiver.getEmail(), token, model);
		setupUrlParams(model);
		sendMimeMessage(receiver.getEmail(), MailType.USER_INVITATION, model);

	}

	@Override
	public void sendShareInvitation(User sender, String email, Project project) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("user", sender);

		String encodedMail = encodeBase64String(email.getBytes());
		String encodedUserId = encodeBase64String(sender.getId().toString()
				.getBytes());
		String encodedProjectId = encodeBase64String(project.getId().toString()
				.getBytes());

		model.put(EMAIL_B64, encodedMail);
		model.put(USER_B64, encodedUserId);
		model.put(PROJECT_B64, encodedProjectId);

		setupUrlParams(model);
		sendMimeMessage(email, MailType.SHARE_INVITATION, model);
	}

	@Override
	public void sendShareNotification(User sender, User receiver,
			Project project) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("sender", sender);
		model.put("receiver", receiver);

		String encodedUserId = encodeBase64String(sender.getId().toString()
				.getBytes());
		String encodedProjectId = encodeBase64String(project.getId().toString()
				.getBytes());
		model.put(USER_B64, encodedUserId);
		model.put(PROJECT_B64, encodedProjectId);

		setupUrlParams(model);
		sendMimeMessage(receiver.getEmail(), MailType.SHARE_NOTIFICATION, model);
	}

	@Override
	public void sendMessage(List<String> receivers, final String text,
			String url) {

		Map<String, Object> model = new HashMap<String, Object>();
		model.put(MESSAGE, text);
		model.put(URL, url);

		for (String email : receivers) {
			sendMimeMessage(email, MailType.SHARE_BY_MAIL, model);
		}
	}

	private void setupEncodedData(String email, String token,
			Map<String, Object> model) {
		String encodedMail = encodeBase64String(email.getBytes());
		String encodedCode = encodeBase64String(token.getBytes());

		model.put(EMAIL_B64, encodedMail);
		model.put("codeB64", encodedCode);
	}

	public void setMailSender(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void setVelocityEngine(VelocityEngine velocityEngine) {
		this.velocityEngine = velocityEngine;
	}

	public void setEmail(String adminEmail) {
		this.adminEmail = adminEmail;
	}

	void setupUrlParams(Map<String, Object> model) {
		model.put("scheme", properties.getProperty("site.scheme"));
		model.put("domain", properties.getProperty("site.domain"));
		model.put("port", properties.getProperty("site.port"));
	}

	private void sendMimeMessage(final String email, final MailType type,
			final Map<String, Object> model) {

		final MimeMessagePreparator preparator = new MimeMessagePreparator() {
			public void prepare(MimeMessage mimeMessage)
					throws MessagingException {
				MimeMessageHelper message = new MimeMessageHelper(mimeMessage,
						"UTF-8");
				message.setTo(email);
				message.setFrom(adminEmail);
				String text = VelocityEngineUtils.mergeTemplateIntoString(
						velocityEngine,
						MAIL_TEMPLATES_PATH + type.getTemplateName(), model);

				message.setText(text, true);
				message.setSubject(type.getSubject());
			}
		};

		this.mailSender.send(preparator);
	}

}
