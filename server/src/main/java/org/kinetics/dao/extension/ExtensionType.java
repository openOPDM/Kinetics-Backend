package org.kinetics.dao.extension;

/**
 * This enum will be persisted into {@link ExtensionMetaData} entity. Make sure
 * not to change the order - as this will break compatibility with existing
 * schemas
 * 
 * @author akaverin
 * 
 */
public enum ExtensionType {

	NUMERIC, TEXT, LIST

}
