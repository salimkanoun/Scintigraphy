package org.petctviewer.scintigraphy.lympho;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.ModeleScin;

public class ModeleLympho extends ModeleScin{

	private boolean locked;

	public ModeleLympho(ImageSelection[] selectedImages, String studyName) {
		super(selectedImages, studyName);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void calculerResultats() {
		// TODO Auto-generated method stub
		
	}
	
	
	public boolean isLocked() {
		return locked;
	}
	
	/************** Getter *************/

	/************** Setter *************/	
	public void setLocked(boolean locked) {
		this.locked = locked;
	}

}
