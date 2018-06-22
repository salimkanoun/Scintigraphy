package org.petctviewer.scintigraphy.CSV;

import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import ij.plugin.PlugIn;

public class CSVComparator implements PlugIn{

	@Override
	public void run(String arg0) {
		
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setDialogTitle("Select CSV File");
		fc.addChoosableFileFilter(new FileNameExtensionFilter("CSV Documents", "csv"));
		fc.setAcceptAllFileFilterUsed(true);
		
		if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
    	{	
    		FenApplication_CVS fen;
			try {
				fen = new FenApplication_CVS(fc.getSelectedFile().getAbsolutePath());
	    		fen.setVisible(true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
	}
}
