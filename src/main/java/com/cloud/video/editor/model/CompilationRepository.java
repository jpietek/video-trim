package com.cloud.video.editor.model;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompilationRepository extends CrudRepository<Compilation, Integer> {
	Compilation findFirst1CompliationByUsers_EmailOrderByModifiedDesc(String email);
	Compilation findByName(String name);
	Compilation findById(Integer id);
	@Query("SELECT c FROM Compilation c WHERE ?1 member of c.users")
	List<Compilation> findByUser(User u);
	List<Compilation> findAll();
}
