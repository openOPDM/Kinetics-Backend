package org.kinetics.test;

import static com.lohika.server.core.test.TestUtils.makeUniqueId;

import java.util.ArrayList;
import java.util.List;

import org.kinetics.dao.session.Session;
import org.kinetics.dao.testsession.TestSession;

public class DaoGenerator {

	public static List<TestSession> generateTestSessions(Session session,
			int size) {
		List<TestSession> itemsToSave = new ArrayList<TestSession>(size);
		for (int i = 0; i < size; ++i) {
			itemsToSave.add(new TestSession(1, makeUniqueId("raw"),
					makeUniqueId("type"), session.getUser(), session
							.getProject()));
		}
		return itemsToSave;
	}

}
