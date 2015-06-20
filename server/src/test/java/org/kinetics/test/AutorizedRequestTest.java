package org.kinetics.test;

import org.junit.Before;
import org.kinetics.dao.session.Session;
import org.kinetics.managers.account.DummyUserManager;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AutorizedRequestTest extends TransactionalRequestTest {

	protected Session activeSession;

	@Autowired
	protected DummyUserManager userManager;

	@Before
	public void setUp() {
		activeSession = userManager.createActivatedUserAndLogin();
	}

}
