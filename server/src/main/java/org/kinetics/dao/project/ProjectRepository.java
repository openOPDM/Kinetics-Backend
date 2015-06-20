package org.kinetics.dao.project;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface ProjectRepository extends CrudRepository<Project, Integer> {

	Project findOneByName(String name);

	// TODO: workaround till
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=349477 is fixed!
	@Query("SELECT p from Project p WHERE p.id IN ?1")
	List<Project> findAllByIdIn(Collection<Integer> ids);

}
