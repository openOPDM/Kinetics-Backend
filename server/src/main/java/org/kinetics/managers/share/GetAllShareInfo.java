package org.kinetics.managers.share;

import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptyList;
import static org.kinetics.rest.Protocol.Arguments.SHARE_DATA;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.Transformer;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.kinetics.dao.project.Project;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.share.SharedTest;
import org.kinetics.dao.share.SharedTestRepository;
import org.kinetics.dao.share.SocialTest;
import org.kinetics.dao.share.SocialTestRepository;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserRepository;
import org.kinetics.managers.testsession.ClearHeavyDataTransformer;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol.Managers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.protocol.core.response.data.GenericResponseData;
import com.lohika.server.core.strategy.RequestDescriptor;

@Component
@RequestDescriptor(target = Managers.SHARING_MANAGER, method = GetAllShareInfo.METHOD)
public class GetAllShareInfo extends AuthKineticsRequestStrategy {

	static final String SHARE_OTHERS = "shareOthers";

	static final String SHARE_MINE = "shareMine";

	static final String SOCIAL = "social";

	static final String METHOD = "getAllShareInfo";

	@Autowired
	private SharedTestRepository sharedTestRepo;
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private SocialTestRepository socialTestRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		// all my collaborators
		List<MyCollaboratorData> myData = buildMySharedItems(session);

		// shared with me
		List<OtherTestRoomData> otherData = buildOtherSharedItems(session);

		// social
		List<SocialTest> socialData = buildSocialItems(session);

		HashMap<String, Object> jsonMap = new HashMap<String, Object>();
		jsonMap.put(SHARE_MINE, myData);
		jsonMap.put(SHARE_OTHERS, otherData);
		jsonMap.put(SOCIAL, socialData);

		GenericResponseData<Object> data = new GenericResponseData<Object>(
				SHARE_DATA, jsonMap);

		return ResponseFactory.makeSuccessDataResponse(data, requestData);
	}

	private List<SocialTest> buildSocialItems(Session session) {
		List<SocialTest> tests = socialTestRepo.findAllByOwnerAndProject(
				session.getUser(), session.getProject());
		if (tests.isEmpty()) {
			return emptyList();
		}
		Transformer transformer = ClearHeavyDataTransformer.instance();
		for (SocialTest socialTest : tests) {
			transformer.transform(socialTest.getTestSession());
		}
		return tests;
	}

	private List<MyCollaboratorData> buildMySharedItems(Session session) {
		Set<String> emails = newHashSet(sharedTestRepo
				.findEmailsByOwnerAndProject(session.getUser(),
						session.getProject()));
		if (emails.isEmpty()) {
			return emptyList();
		}
		List<MyCollaboratorData> myData = newArrayListWithCapacity(emails
				.size());

		List<User> users = userRepo.findAllByEmailIn(emails);
		processNonExistingUsers(emails, myData, users);
		for (User user : users) {
			myData.add(new MyCollaboratorData(user.getEmail(),
					buildOwnerName(user)));
		}
		return myData;
	}

	private static String buildOwnerName(User user) {
		return user.getFirstName() + " " + user.getSecondName();
	}

	private static void processNonExistingUsers(Set<String> emails,
			List<MyCollaboratorData> myData, List<User> users) {
		if (users.size() != emails.size()) {
			for (User user : users) {
				emails.remove(user.getEmail());
			}

			for (String email : emails) {
				myData.add(new MyCollaboratorData(email, null));
			}
		}
	}

	private List<OtherTestRoomData> buildOtherSharedItems(Session session) {
		List<SharedTest> sharedTests = sharedTestRepo
				.findAllByEmailAndActiveProjects(session.getUser().getEmail());
		if (sharedTests.isEmpty()) {
			return emptyList();
		}
		List<OtherTestRoomData> otherData = newArrayListWithCapacity(sharedTests
				.size());
		for (SharedTest sharedTest : sharedTests) {
			otherData.add(new OtherTestRoomData(sharedTest.getOwner(),
					sharedTest.getProject()));
		}
		return otherData;
	}

	@JsonSerialize(include = Inclusion.NON_NULL)
	static final class MyCollaboratorData {
		private final String email;
		private final String name;

		public MyCollaboratorData(String email, String name) {
			this.email = email;
			this.name = name;
		}

		public String getEmail() {
			return email;
		}

		public String getName() {
			return name;
		}

	}

	@JsonSerialize(include = Inclusion.NON_NULL)
	static final class OtherTestRoomData {

		private final Integer projectId;
		private final Integer userId;
		private final String ownerName;
		private final String ownerEmail;
		private final String projectName;

		public OtherTestRoomData(User user, Project project) {
			this.userId = user.getId();
			this.projectId = project.getId();
			this.projectName = project.getName();

			this.ownerEmail = user.getEmail();
			this.ownerName = buildOwnerName(user);
		}

		public String getOwnerName() {
			return ownerName;
		}

		public String getOwnerEmail() {
			return ownerEmail;
		}

		public Integer getProjectId() {
			return projectId;
		}

		public Integer getUserId() {
			return userId;
		}

		public String getProjectName() {
			return projectName;
		}

	}

}
