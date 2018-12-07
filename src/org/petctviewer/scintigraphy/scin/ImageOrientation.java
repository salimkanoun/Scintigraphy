package org.petctviewer.scintigraphy.scin;

import ij.ImagePlus;

public class ImageOrientation {
	
	public static final int ANT=0;
	public static final int POST=1;
	public static final int ANT_POST=2;
	public static final int POST_ANT=3;
	public static final int DYNAMIC_ANT=4;
	public static final int DYNAMIC_POST=5;
	public static final int DYNAMIC_ANT_POST=6;
	public static final int DYNAMIC_POST_ANT=7;
	
	private int imageOrientation;
	private ImagePlus imp;
	
	public ImageOrientation(int imageOrientation, ImagePlus imp) {
		this.imageOrientation=imageOrientation;
		this.imp=imp;
	}
	
	public ImagePlus getImagePlus() {
		return imp;
	}
	
	public int getImageOrientation() {
		return imageOrientation;
	}

}
