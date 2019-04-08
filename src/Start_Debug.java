import org.petctviewer.petcttools.reader.Dicom_Reader;

import ij.ImageJ;

public class Start_Debug {

	public static void main(String[] args) {
		new ImageJ();

		Dicom_Reader cd = new Dicom_Reader();
		cd.run("");
		
		new FenDebug();
	}

}
