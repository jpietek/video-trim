package com.cloud.video.editor.model.melt;

import java.util.List;
import java.util.Set;

import org.xembly.Directives;

public class AffineFilter extends Filter implements FilterInterface {

	private Set<AspectRatioTransform> keyframedPositions;
	
	private int clippedWidth;
	private int clippedHeight;
	
	private int sourceWidth;
	private int sourceHeight;
	
	public AffineFilter(int id, Set<AspectRatioTransform> keyframedPositions, int clippedWidth, int clippedHeight,
			int sourceWidth, int sourceHeight) {
		super();
		this.setId(id);
		this.keyframedPositions = keyframedPositions;
		this.clippedWidth = clippedWidth;
		this.clippedHeight = clippedHeight;
		this.sourceWidth = sourceWidth;
		this.sourceHeight = sourceHeight;
	}
	
	public int getClippedWidth() {
		return clippedWidth;
	}

	public void setClippedWidth(int clippedWidth) {
		this.clippedWidth = clippedWidth;
	}

	public int getClippedHeight() {
		return clippedHeight;
	}

	public void setClippedHeight(int clippedHeight) {
		this.clippedHeight = clippedHeight;
	}

	public int getSourceWidth() {
		return sourceWidth;
	}

	public void setSourceWidth(int sourceWidth) {
		this.sourceWidth = sourceWidth;
	}

	public int getSourceHeight() {
		return sourceHeight;
	}

	public void setSourceHeight(int sourceHeight) {
		this.sourceHeight = sourceHeight;
	}

	private String getTransformGeometry() {
		StringBuffer sb = new StringBuffer();
		for(AspectRatioTransform tr: keyframedPositions) {
			long keyframe = tr.getKeyframe();
			
			double relativeXPosition = tr.getxPosition();
			
			int xPos = (int) Math.ceil((double) sourceWidth/2 * (1 + relativeXPosition) - clippedWidth/2);
			int yPos = (int) Math.floor(tr.getyPosition());
			
			sb.append(keyframe + "=" + -xPos + "," + yPos  
					+ ":" + this.sourceWidth + "x" + this.sourceHeight + ";");
		}
		return sb.toString();
	}
	
	@Override
	public Directives toXml() {
		Directives filter = new Directives()
				.add("filter")
				.attr("id", this.getId())
				.add("property")
				.attr("name", "mlt_service")
				.set("filter")
				.up()
				.add("property")
				.attr("name", "mlt_service")
				.set("affine")
				.up()
				.add("property")
				.attr("name", "track")
				.set("0")
				.up()
				.add("property")
				.attr("name", "transition.geometry")
				.set(this.getTransformGeometry());
		return filter;
	}

}
