package org.kinetics.util.secure;

import org.junit.Assert;
import org.junit.Test;
import org.kinetics.managers.shared.UTConsts;

public class TestHashUtils {

	@Test
	public void testGenerateAndIsValid() {

		HashData data = HashUtils.generate(UTConsts.DUMMY_PASS);
		Assert.assertNotNull(data);

		Assert.assertTrue(HashUtils.isValid(UTConsts.DUMMY_PASS, data));
	}

}
