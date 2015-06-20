package org.kinetics.util;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestUtililtyClasses {

	@Test
	public void testConfirmationCodeGenerator() {
		String prev = ConfirmationCodeGenerator.generate();
		for (int i = 0; i < 20; i++) {
			String current = ConfirmationCodeGenerator.generate();
			System.out.println(prev + "/" + current);
			assertNotSame(prev, current);
			prev = current;
			assertTrue(prev.length() > 3);
		}
	}

}
