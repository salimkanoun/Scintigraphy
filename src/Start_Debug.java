import org.petctviewer.scintigraphy.shunpo.Vue_Shunpo;

import ij.ImageJ;

public class Start_Debug {
	
	public static void main (String [] args) {
		new ImageJ();
		//Debug.run("Vue_Shunpo", "plugin parameters");
		Read_CD cd= new Read_CD();
		cd.run("");
		Vue_Shunpo vue = new Vue_Shunpo();
		vue.run("");
	}
	
}
