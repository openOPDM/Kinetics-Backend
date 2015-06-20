package org.kinetics.dao.extension;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.eclipse.persistence.annotations.CascadeOnDelete;
import org.eclipse.persistence.annotations.Index;
import org.kinetics.dao.project.Project;

/**
 * Servers as a descriptor for new column added to specific
 * {@link ExtendedEntity} instance. ExtensionMetaData is separated by
 * {@link Project}.
 * 
 * @author akaverin
 * 
 */
@Entity
@CascadeOnDelete
@Table(uniqueConstraints = @UniqueConstraint(name = "uniq_idx", columnNames = {
		"entity", "name", "project" }))
@JsonSerialize(include = Inclusion.NON_NULL)
public class ExtensionMetaData {

	@Id
	@GeneratedValue
	private Integer id;

	@Enumerated
	private ExtendedEntity entity;

	@Index
	private String name;

	@Enumerated
	private ExtensionType type;

	/**
	 * Bitset like mask of properties based on Enums. It should provide
	 * flexibility as properties will extend
	 */
	@ElementCollection
	@CascadeOnDelete
	@Enumerated
	private Set<ExtensionProperty> properties;

	@ElementCollection
	@CascadeOnDelete
	private Set<ExtensionMetaFilter> filters;

	@Transient
	@JsonIgnore
	List<String> list;

	public ExtensionMetaData() {
	}

	public ExtensionMetaData(Integer id, ExtendedEntity entity, String name,
			ExtensionType type) {
		this.id = id;
		this.entity = entity;
		this.name = name;
		this.type = type;
	}

	public ExtensionMetaData(ExtendedEntity entity, String name,
			ExtensionType type) {
		this.entity = entity;
		this.name = name;
		this.type = type;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public ExtendedEntity getEntity() {
		return entity;
	}

	public void setEntity(ExtendedEntity entity) {
		this.entity = entity;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ExtensionType getType() {
		return type;
	}

	public void setType(ExtensionType type) {
		this.type = type;
	}

	public Set<ExtensionProperty> getProperties() {
		return Collections.unmodifiableSet(properties);
	}

	public void setProperties(EnumSet<ExtensionProperty> properties) {
		this.properties = properties;
	}

	public Set<ExtensionMetaFilter> getFilters() {
		return Collections.unmodifiableSet(filters);
	}

	public void setFilters(Set<ExtensionMetaFilter> filters) {
		this.filters = filters;
	}

	@JsonProperty
	public List<String> getList() {
		return list;
	}

	public void setList(List<String> list) {
		this.list = list;
	}

	@Override
	public String toString() {
		return "ExtensionMetaData [id=" + id + ", entity=" + entity + ", name="
				+ name + ", type=" + type + ", properties=" + properties + "]";
	}

}
