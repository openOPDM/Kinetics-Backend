package org.kinetics.dao.extension;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.eclipse.persistence.annotations.CascadeOnDelete;

/**
 * Entity representing single {@link ExtensionType#LIST} value. Mapped directly
 * to specific {@link ExtensionMetaData} instance
 * 
 * @author akaverin
 * 
 */
@Entity
@CascadeOnDelete
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "label",
		"metaData" }))
public class ExtensionListNode {

	@Id
	@GeneratedValue
	private Integer id;

	@ManyToOne(optional = false)
	private ExtensionMetaData metaData;

	private String label;

	public ExtensionListNode() {
	}

	public ExtensionListNode(String label, ExtensionMetaData metaData) {
		this.label = label;
		this.metaData = metaData;
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

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return "ListNode [id=" + id + ", metaData=" + metaData + ", label="
				+ label + "]";
	}

}
