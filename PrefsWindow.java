/**
Copyright (C) 2017 MOHAND Mathis and KANOUN Salim
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public v.3 License as published by
the Free Software Foundation;
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ij.IJ;
import ij.Prefs;
import ij.plugin.PlugIn;

public class PrefsWindow extends JPanel implements PlugIn, ActionListener {
	
	private static final long serialVersionUID = 1148288454969248518L;
	private JFrame frame ;
	private JLabel lut, dir ;
	private JButton btn_choixLut, btn_savelut, btn_dir, btn_savedir, btn_displut ;
	private JFileChooser fc ;
	
	@Override
	public void run(String arg0) {
		this.frame = new JFrame("Preferences Window");
		String plut = Prefs.get("lut.preferred", null)==null?"Preferred LUT":Prefs.get("lut.preferred", null) ;
		this.lut = new JLabel(plut) ;
		this.lut.setEnabled(false);
		this.btn_choixLut = new JButton("Open...") ;
		this.btn_choixLut.addActionListener(this);
		this.btn_savelut = new JButton("Save") ;
		this.btn_savelut.addActionListener(this);
		
		this.btn_displut = new JButton("Show LUTs");
		this.btn_displut.addActionListener(this);
		
		this.btn_savedir = new JButton("Save") ;
		this.btn_savedir.addActionListener(this);
		
		String pdir = Prefs.get("dir.preferred", null)==null?"Save Directory":Prefs.get("dir.preferred", null) ;
		this.dir = new JLabel(pdir) ;
		this.dir.setEnabled(false);
		this.btn_dir = new JButton("Browse") ;
		this.btn_dir.addActionListener(this);
		this.fc = new JFileChooser() ;
		JPanel pan = new JPanel();
		pan.setLayout(new GridLayout(2,1));
		
		JPanel pan_lut = new JPanel();
		pan_lut.add(this.lut);
		pan_lut.add(this.btn_choixLut);
		pan_lut.add(this.btn_displut);
		pan_lut.add(this.btn_savelut);
		pan.add(pan_lut);
		
		JPanel pan_dir = new JPanel() ;
		pan_dir.add(this.dir);
		pan_dir.add(this.btn_dir);
		pan_dir.add(this.btn_savedir);
		pan.add(pan_dir);
		this.add(pan);
		 //Create and set up the window.
        this.frame.setSize(this.frame.getPreferredSize());
        //Add content to the window.
        this.frame.add(this);
 
        //Display the window.
        this.frame.pack();
        this.frame.setVisible(true);
        this.frame.setSize(frame.getPreferredSize());
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == this.btn_choixLut) {
			this.fc.setCurrentDirectory(new File("./luts"));
			this.fc.setDialogTitle("Choose Preferred LUT");
			int returnVal = fc.showOpenDialog(PrefsWindow.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					this.lut.setText(file.getPath());
			}
		}
		if (arg0.getSource() == this.btn_savelut) {
			Prefs.set("lut.preferred", this.lut.getText()+"");
			Prefs.savePreferences();
		}
		if (arg0.getSource() == this.btn_dir) {
			this.fc.setDialogTitle("Export directory");
			this.fc.setCurrentDirectory(this.fc.getFileSystemView().getDefaultDirectory());
			this.fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			this.fc.setAcceptAllFileFilterUsed(false);
			int rval = fc.showOpenDialog(PrefsWindow.this);
			if (rval == JFileChooser.APPROVE_OPTION) {
				this.dir.setText(fc.getSelectedFile().getAbsoluteFile().toString()) ;
			}
		}
		if (arg0.getSource() == this.btn_savedir) {
			Prefs.set("dir.preferred", this.dir.getText()+"");
			Prefs.savePreferences();
		}
		if (arg0.getSource() == this.btn_displut)
			IJ.run("Display LUTs");
		this.fc = new JFileChooser() ;
	}

}
