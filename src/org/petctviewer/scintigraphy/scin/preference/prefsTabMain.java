package org.petctviewer.scintigraphy.scin.preference;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ij.IJ;
import ij.Prefs;
import ij.plugin.PlugIn;

public class prefsTabMain extends JPanel implements ActionListener{
	
	private JLabel lut, dir ;
	private JButton btn_choixLut, btn_dir, btn_displut, btn_stRenal ;
	private JFileChooser fc ;
	private JComboBox comboDate;
	
	public prefsTabMain() {
		this.setLayout(new BorderLayout());
		JPanel pnl_titre = new JPanel();
		pnl_titre.add(new JLabel("<html><h3>Main settings</h3></html>"));
		this.add(pnl_titre, BorderLayout.NORTH);
		
		String plut = Prefs.get("lut.preferred", null)==null?"Preferred LUT":Prefs.get("lut.preferred", null) ;
		this.lut = new JLabel(plut) ;
		this.lut.setEnabled(false);
		this.btn_choixLut = new JButton("Open...") ;
		this.btn_choixLut.addActionListener(this);
		
		this.btn_displut = new JButton("Show LUTs");
		this.btn_displut.addActionListener(this);
		
		String pdir = Prefs.get("dir.preferred", null)==null?"Save Directory":Prefs.get("dir.preferred", null) ;
		this.dir = new JLabel(pdir) ;
		this.dir.setEnabled(false);
		this.btn_dir = new JButton("Browse") ;
		this.btn_dir.addActionListener(this);
		this.fc = new JFileChooser() ;
		JPanel pan = new JPanel();
		pan.setLayout(new GridLayout(4,1));
		
		JPanel pan_lut = new JPanel();
		pan_lut.add(this.lut);
		pan_lut.add(this.btn_choixLut);
		pan_lut.add(this.btn_displut);
		pan.add(pan_lut);
		
		JPanel pan_dir = new JPanel() ;
		pan_dir.add(this.dir);
		pan_dir.add(this.btn_dir);
		pan.add(pan_dir);
		
		JPanel pnl_formatDate = new JPanel();
		pnl_formatDate.add(new JLabel("Date format :"));
		this.comboDate = new JComboBox(new String[] { "MM/dd/yyyy", "dd/MM/yyyy" });
		this.comboDate.setSelectedItem(Prefs.get("dateformat.preferred", "MM/dd/yyyy"));
		this.comboDate.addActionListener(this);
		pnl_formatDate.add(comboDate);
		pan.add(pnl_formatDate);
		
		this.add(pan,BorderLayout.CENTER);
		
		
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == this.btn_choixLut) {
			this.fc.setCurrentDirectory(new File("./luts"));
			this.fc.setDialogTitle("Choose Preferred LUT");
			int returnVal = fc.showOpenDialog(prefsTabMain.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					this.lut.setText(file.getPath());
					Prefs.set("lut.preferred", this.lut.getText()+"");
					Prefs.savePreferences();
			}
		}
		
		else if (arg0.getSource() == this.btn_dir) {
			this.fc.setDialogTitle("Export directory");
			this.fc.setCurrentDirectory(this.fc.getFileSystemView().getDefaultDirectory());
			this.fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			this.fc.setAcceptAllFileFilterUsed(false);
			int rval = fc.showOpenDialog(prefsTabMain.this);
			if (rval == JFileChooser.APPROVE_OPTION) {
				this.dir.setText(fc.getSelectedFile().getAbsoluteFile().toString()) ;
				Prefs.set("dir.preferred", this.dir.getText()+"");
				Prefs.savePreferences();
			}
		}
		
		else if (arg0.getSource() == this.btn_displut) {
			IJ.run("Display LUTs");
		}
		

		
		else if (arg0.getSource() == this.comboDate) {
			Prefs.set("dateformat.preferred", (String) this.comboDate.getSelectedItem());
		}
		
		this.fc = new JFileChooser() ;
	}

}
