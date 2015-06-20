package org.kinetics.managers.testsession;

import org.apache.commons.collections.Transformer;
import org.kinetics.dao.testsession.TestSession;

/**
 * Helper {@link Transformer} to remove heavy fields data for summary
 * 
 * @author akaverin
 * 
 */
public final class ClearHeavyDataTransformer implements Transformer {

	private static class SingletonHolder {
		private static final ClearHeavyDataTransformer HOLDER_INSTANCE = new ClearHeavyDataTransformer();
	}

	/**
	 * Thread safe singleton
	 * 
	 * @return
	 */
	public static Transformer instance() {
		return SingletonHolder.HOLDER_INSTANCE;
	}

	private ClearHeavyDataTransformer() {
	}

	@Override
	public Object transform(Object arg0) {
		TestSession test = (TestSession) arg0;
		test.setRawData(null);
		return test;
	}
}