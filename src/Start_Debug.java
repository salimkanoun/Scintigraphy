import org.petctviewer.scintigraphy.cardiac.Vue_Cardiac;
import org.petctviewer.scintigraphy.scin.view.VueScin;

import ij.ImageJ;

public class Start_Debug {

	public static void main(String[] args) {
		new ImageJ();
		// Debug.run("Vue_Shunpo", "plugin parameters");
		Read_CD cd = new Read_CD();
		cd.run("");
		new FenDebug();
	}

}
