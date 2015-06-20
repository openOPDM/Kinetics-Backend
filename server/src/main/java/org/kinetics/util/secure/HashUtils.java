package org.kinetics.util.secure;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.codec.digest.DigestUtils.sha512;

import java.security.SecureRandom;
import java.util.Arrays;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Set of helper methods to assist in password or sensitive data hashing
 * 
 * @author akaverin
 * 
 */
// TODO: Spring Security contains PasswordEncoder interface implementation which
// can handle this case in more polite way by providing strong hashing and using
// pass field for both SALT and PASSWORD persistance
public final class HashUtils {

	private static final int SALT_BYTES = 24;

	private HashUtils() {
	}

	public static boolean isValid(String password, HashData hashData) {
		checkNotNull(password);
		checkNotNull(hashData);
		byte[] newHash = doHash(password, hashData.getSalt());
		return Arrays.equals(newHash, hashData.getHash());
	}

	public static HashData generate(String password) {
		final byte[] salt = generateSalt();
		final byte[] hash = doHash(password, salt);

		return new HashData(hash, salt);
	}

	public static String generateClientSide(String password, String salt) {
		return DigestUtils.md5Hex(salt + password);
	}

	private static byte[] doHash(String password, byte[] salt) {
		return sha512(new String(salt) + password);
	}

	private static byte[] generateSalt() {
		// Generate a random salt
		SecureRandom random = new SecureRandom();

		byte[] salt = new byte[SALT_BYTES];
		random.nextBytes(salt);

		return salt;
	}

}
