package com.cloud.video.editor.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Video extends VideoPojo {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@Column(length = 512)
	private String directContentLink;

	@Column(length = 512)
	private String webContentLink;

	@ManyToOne
	@JoinColumn
	@JsonBackReference
	private Compilation compilation;

	public static Video fromPojo(VideoPojo videoPojo) {
		return (Video) videoPojo;
	}

	public double cutInSeconds() {
		return this.cutIn * this.duration / 1000;
	}

	public long cutInMillis() {
		return (long) Math.ceil(this.cutIn * this.duration);
	}

	public long cutOutMillis() {
		return (long) Math.ceil(this.cutOut * this.duration);
	}

	public double cutOutSeconds() {
		return this.cutOut * this.duration / 1000;
	}
}
