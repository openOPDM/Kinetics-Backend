package org.kinetics.managers.share;

import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.collect.Sets.newHashSet;

import java.util.List;
import java.util.Set;

import org.kinetics.dao.session.Session;
import org.kinetics.dao.share.SharedTest;
import org.kinetics.dao.share.SharedTestRepository;
import org.kinetics.dao.share.SocialTest;
import org.kinetics.dao.share.SocialTestRepository;
import org.kinetics.dao.testsession.TestSession;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserRepository;
import org.kinetics.service.RoleService;
import org.kinetics.util.mail.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ShareService {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ShareService.class);

	@Autowired
	private SharedTestRepository sharedTestRoomRepo;
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private MailService mailService;
	@Autowired
	private RoleService roleService;
	@Autowired
	private SocialTestRepository socialTestRepo;

	@Transactional
	void addTestShareData(List<String> emails, Session session) {

		Set<String> newEmails = newHashSet(emails);
		Set<String> current = newHashSet(sharedTestRoomRepo
				.findEmailsByOwnerAndProject(session.getUser(),
						session.getProject()));
		Set<String> addedEmails = newHashSet(newEmails);

		// define delta
		addedEmails.removeAll(current);

		if (!addedEmails.isEmpty()) {
			cleanupAdminMails(addedEmails);
			if (addedEmails.isEmpty()) {
				return;
			}
			// add relations to both email groups
			createShareTestsRelations(addedEmails, session);

			// separate existing and not existing
			List<User> registeredUsers = userRepo.findAllByEmailIn(addedEmails);
			for (User user : registeredUsers) {
				addedEmails.remove(user.getEmail());
			}
			sendMail(registeredUsers, addedEmails, session);
		}
	}
	
	@Transactional
	SocialTest getOrGenerateToken(TestSession testSession){		
		SocialTest socialTest = socialTestRepo
				.findOneByTestSession(testSession);
		if (socialTest == null) {
			socialTest = socialTestRepo.save(new SocialTest(testSession));
		}
		return socialTest;		
	}

	private void createShareTestsRelations(Set<String> addedEmails,
			Session session) {
		List<SharedTest> sharedTests = newArrayListWithCapacity(addedEmails
				.size());
		for (String email : addedEmails) {
			sharedTests.add(new SharedTest(session.getUser(), session
					.getProject(), email));
		}
		sharedTestRoomRepo.save(sharedTests);
	}

	private void cleanupAdminMails(Set<String> addedEmails) {
		List<String> adminMails = userRepo.findEmailsByEmailInAndRole(
				addedEmails, roleService.getSiteAdminRole());
		addedEmails.removeAll(adminMails);
	}

	private void sendMail(List<User> registeredUsers,
			Set<String> unregisteredEmails, Session session) {

		for (User user : registeredUsers) {
			try {
				mailService.sendShareNotification(session.getUser(), user,
						session.getProject());
			} catch (MailException e) {
				LOGGER.error("Failed to send sharing notification", e);
			}
		}

		for (String email : unregisteredEmails) {
			try {
				mailService.sendShareInvitation(session.getUser(), email,
						session.getProject());
			} catch (MailException e) {
				LOGGER.error("Failed to send sharing invitation", e);
			}
		}
	}

}
