package org.kinetics.dao.testsession;

import java.util.List;

import org.joda.time.LocalDate;
import org.kinetics.dao.audit.AuditData;
import org.kinetics.dao.extension.ExtendedEntity;
import org.kinetics.dao.extension.ExtensionService;
import org.kinetics.dao.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TestSessionService {

	@Autowired
	private TestSessionRepository testRepo;
	@Autowired
	private ExtensionService testSessionService;

	@Transactional
	public void persistTestAndExtension(TestSession newTest, Session session) {
		testRepo.save(newTest);
		testSessionService.persistExtensions(newTest.getId(),
				newTest.getExtension(), ExtendedEntity.TEST_SESSION);
	}

	public List<AuditData> findByCreateBetween(LocalDate from, LocalDate to) {
		return testRepo.findAllByCreatedBetween(from.toDateTimeAtStartOfDay()
				.toDate(), to.toDateTimeAtStartOfDay().toDate());
	}
}
