package org.kinetics.dao.extension;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface ExtensionListNodeRepository extends
		CrudRepository<ExtensionListNode, Integer> {

	@Query("SELECT n.label FROM ExtensionListNode n WHERE n.metaData = ?1")
	List<String> findAllLabelsByMetaData(ExtensionMetaData metaData);

	ExtensionListNode findOneByLabelAndMetaData(String label,
			ExtensionMetaData metaData);

	@Transactional
	@Modifying
	@Query("DELETE FROM ExtensionListNode l WHERE l.metaData = ?1")
	void deleteByMetaData(ExtensionMetaData metaData);

}
