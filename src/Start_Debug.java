
import org.petctviewer.scintigraphy.renal.gui.TabPostMict;
import org.petctviewer.scintigraphy.scin.VueScin;
import org.petctviewer.scintigraphy.scin.basic.Vue_Basic;

import ij.ImageJ;

public class Start_Debug {

	public static void main(String[] args) {
		new ImageJ();
		Read_CD cd = new Read_CD();
		cd.run("");
		
		new FenDebug();
	}

}
