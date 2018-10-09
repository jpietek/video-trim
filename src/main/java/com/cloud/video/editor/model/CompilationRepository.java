package com.cloud.video.editor.model;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompilationRepository extends CrudRepository<Compilation, Integer> {
	Compilation findFirst1CompliationByUser_EmailOrderByModifiedDesc(String userMail);
	Compilation findByName(String name);
	Compilation findById(Integer id);
	List<Compilation> findFirst10ByUserEmailOrderByModifiedDesc(String userMail);
	List<Compilation> findAll();
}
