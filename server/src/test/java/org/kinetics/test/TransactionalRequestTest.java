package org.kinetics.test;

import org.springframework.transaction.annotation.Transactional;

import com.lohika.server.core.test.RequestTest;

@Transactional
public abstract class TransactionalRequestTest extends RequestTest {

}
