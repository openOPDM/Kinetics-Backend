package org.kinetics.dao.extension;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.eclipse.persistence.annotations.CascadeOnDelete;

/**
 * Values added to specific {@link ExtendedEntity} instance. Description of this
 * extension can be found in {@link ExtensionMetaData}.
 * 
 * @author akaverin
 * 
 */
@Entity
@CascadeOnDelete
public class ExtensionData {

	@Id
	@GeneratedValue
	@JsonIgnore
	private Integer id;

	/**
	 * Descriptor reference
	 */
	@JsonIgnore
	@ManyToOne(optional = false)
	private ExtensionMetaData metaData;

	/**
	 * manually managed foreign key to {@link ExtensionType} instance
	 */
	@JsonIgnore
	private Integer entityId;

	private String value;

	/**
	 * Used only in JSON from/to clients for mapping to metadata. Exposed via
	 * JsonProperty
	 */
	@Transient
	@JsonIgnore
	private String name;

	/**
	 * Used only in JSON from/to clients for mapping to metadata. Exposed via
	 * JsonProperty
	 */
	@Transient
	@JsonIgnore
	private Integer metaId;

	public ExtensionData() {
	}

	public ExtensionData(Integer metaId, String value) {
		this.metaId = metaId;
		this.value = value;
	}

	public ExtensionData(ExtensionMetaData metaData, Integer entityId,
			String value) {
		this.metaData = metaData;
		this.entityId = entityId;
		this.value = value;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public ExtensionMetaData getMetaData() {
		return metaData;
	}

	public void setMetaData(ExtensionMetaData metaData) {
		this.metaData = metaData;
	}

	public Integer getEntityId() {
		return entityId;
	}

	public void setEntityId(Integer entityId) {
		this.entityId = entityId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	@JsonProperty("name")
	public String getNameFromMetaData() {
		return metaData.getName();
	}

	@JsonProperty
	public void setName(String name) {
		this.name = name;
	}

	public Integer getMetaId() {
		return metaId;
	}

	@JsonProperty("metaId")
	public Integer getIdFromMetaData() {
		return metaData.getId();
	}

	@JsonProperty
	public void setMetaId(Integer metaId) {
		this.metaId = metaId;
	}

	@Override
	public String toString() {
		return "ExtensionData [id=" + id + ", metaData=" + metaData
				+ ", entityId=" + entityId + ", value=" + value + ", name="
				+ name + "]";
	}

}
