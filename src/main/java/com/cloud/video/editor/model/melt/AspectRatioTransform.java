package com.cloud.video.editor.model.melt;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.cloud.video.editor.model.Video;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
public class AspectRatioTransform {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer arId;
	
	private double xRelativePosition;
	private double yRelativePosition;
	private long keyframe;
	
	@ManyToOne
	@JoinColumn
	@JsonBackReference
	private Video video;

	public AspectRatioTransform() {

	}

	public AspectRatioTransform(double xPosition, double yPosition, long keyframe) {
		super();
		this.xRelativePosition = xPosition;
		this.yRelativePosition = yPosition;
		this.keyframe = keyframe;
	}

	public double getxPosition() {
		return xRelativePosition;
	}

	public void setxPosition(double xPosition) {
		this.xRelativePosition = xPosition;
	}

	public double getyPosition() {
		return yRelativePosition;
	}

	public void setyPosition(double yPosition) {
		this.yRelativePosition = yPosition;
	}

	public long getKeyframe() {
		return keyframe;
	}

	public void setKeyframe(long keyframe) {
		this.keyframe = keyframe;
	}

	public Integer getArId() {
		return arId;
	}

	public void setArId(Integer arId) {
		this.arId = arId;
	}

	public double getxRelativePosition() {
		return xRelativePosition;
	}

	public void setxRelativePosition(double xRelativePosition) {
		this.xRelativePosition = xRelativePosition;
	}

	public double getyRelativePosition() {
		return yRelativePosition;
	}

	public void setyRelativePosition(double yRelativePosition) {
		this.yRelativePosition = yRelativePosition;
	}
	
}


