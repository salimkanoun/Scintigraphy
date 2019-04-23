package org.petctviewer.scintigraphy.scin;

public abstract class ModelOrgans extends ModeleScin {

	public final boolean FIRST_ORIENTATION_POST;

	public ModelOrgans(ImageSelection[] selectedImages, String studyName, boolean firstOrientationPost) {
		super(selectedImages, studyName);
		this.FIRST_ORIENTATION_POST = firstOrientationPost;
	}

}
