import org.petctviewer.scintigraphy.gastric.Vue_VG_Dynamique;
import org.petctviewer.scintigraphy.gastric.Vue_VG_Roi;
import ij.ImageJ;

public class Start_Debug {
	
	public static void main (String [] args) {
		new ImageJ();
		//Debug.run("Vue_Shunpo", "plugin parameters");
		Read_CD cd= new Read_CD();
		cd.run("");
		//Vue_VG_Dynamique vg=new Vue_VG_Dynamique();
		Vue_VG_Roi vg=new Vue_VG_Roi();
		vg.run("");
		//Vue_Plaquettes vue = new Vue_Plaquettes();
		//vue.run("");
		
	}
	
}
