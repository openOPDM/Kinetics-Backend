package org.kinetics.util;

import java.text.DecimalFormat;
import java.util.Random;

public final class ConfirmationCodeGenerator {

	private static final int RAND_MAX = 10000;
	private static final DecimalFormat CODE_FORMAT = new DecimalFormat("0000");

	/**
	 * Hide utility class constructor
	 */
	private ConfirmationCodeGenerator() {
	}

	public static String generate() {
		return CODE_FORMAT.format(new Random().nextInt(RAND_MAX));
	}

}
