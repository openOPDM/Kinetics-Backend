package org.kinetics.dao.user;

import static com.lohika.server.core.test.TestUtils.makeUniqueId;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.kinetics.dao.audit.AuditData;
import org.kinetics.managers.account.DummyUserManager;
import org.kinetics.managers.account.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.lohika.server.core.test.SpringAwareTest;

@Transactional
public class TestUserService extends SpringAwareTest {

	@Autowired
	private UserService userService;
	@Autowired
	private DummyUserManager userManager;

	@Test
	public void testFoo() {
		userManager.createActivatedUser(makeUniqueId());

		LocalDate today = new LocalDate();

		List<AuditData> stats = userService.findTotalCreatedBetween(
				today.minusMonths(1), today.plusMonths(1));

		assertNotNull(stats);
		assertFalse(stats.isEmpty());

		LocalDate prevDate = today.minusMonths(2);
		long prevTotal = 0;
		for (AuditData stat : stats) {
			assertTrue(prevDate.isBefore(stat.getDate()));
			assertTrue(prevTotal < stat.getTotal());

			prevDate = stat.getDate();
			prevTotal = stat.getTotal();
		}

		System.err.println(stats);
		// assertEquals(2, stats.get(0).g)
	}

}
