import org.petctviewer.scintigraphy.cardiac.FenResultatCardiac;

import ij.ImageJ;

public class Start_Debug {

	public static void main(String[] args) {
		new ImageJ();
		Read_CD cd = new Read_CD();
		cd.run("");
		new FenDebug();
	}

}
