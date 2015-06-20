package org.kinetics.dao.extension;

import javax.persistence.Embeddable;

import org.eclipse.persistence.annotations.CascadeOnDelete;

@Embeddable
@CascadeOnDelete
public class ExtensionMetaFilter {

	private String filterName;
	private String filterData;

	public ExtensionMetaFilter(String filterName, String filterData) {
		this.filterName = filterName;
		this.filterData = filterData;
	}

	public ExtensionMetaFilter() {
	}

	public String getFilterName() {
		return filterName;
	}

	public void setFilterName(String filterName) {
		this.filterName = filterName;
	}

	public String getFilterData() {
		return filterData;
	}

	public void setFilterData(String filterData) {
		this.filterData = filterData;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		ExtensionMetaFilter filter = (ExtensionMetaFilter) o;

		if (!filterData.equals(filter.filterData))
			return false;
		if (!filterName.equals(filter.filterName))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = filterName.hashCode();
		result = 31 * result + filterData.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "ExtensionMetaFilter{" + "filterName='" + filterName + '\''
				+ ", filterData='" + filterData + '\'' + '}';
	}
}
