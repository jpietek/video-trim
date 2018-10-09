package com.cloud.video.editor.model;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
public class User {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	private String name;
	private String gender;
	private String picture;
	private String profile;
	private String email;
	
	@OneToMany(cascade = {CascadeType.ALL}, mappedBy = "user")
	@JsonManagedReference
	private Set<Compilation> compilations;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getPicture() {
		return picture;
	}
	public void setPicture(String picture) {
		this.picture = picture;
	}
	public String getProfile() {
		return profile;
	}
	public void setProfile(String profile) {
		this.profile = profile;
	}
	public Integer getUserId() {
		return id;
	}
	public void setUserId(Integer userId) {
		this.id = userId;
	}
	public Set<Compilation> getCompilations() {
		return compilations;
	}
	public void setCompilations(Set<Compilation> compilations) {
		this.compilations = compilations;
	}
	
}
