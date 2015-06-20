package org.kinetics.util.secure;

import javax.persistence.Embeddable;

/**
 * Helper structure to contain chained hash and salt used for its calculation
 * 
 * @author akaverin
 * 
 */
@Embeddable
public class HashData {

	private byte[] hash;

	private byte[] salt;

	HashData() {
	}

	/**
	 * Passed in byte arrays stored directly. So, avoid modification as
	 * {@link HashData} will own them
	 * 
	 * @param hash
	 * @param salt
	 */
	@SuppressWarnings("all")
	public HashData(byte[] hash, byte[] salt) {
		this.hash = hash;
		this.salt = salt;
	}

	public byte[] getHash() {
		return hash;
	}

	public byte[] getSalt() {
		return salt;
	}

}