import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.cardiac.Vue_Cardiac;
import org.petctviewer.scintigraphy.dynamic.Vue_Dynamic;
import org.petctviewer.scintigraphy.dynamic.Vue_GeneralDyn;
import org.petctviewer.scintigraphy.hepatic.dyn.Vue_HepaticDyn;
import org.petctviewer.scintigraphy.hepatic.statique.Vue_Hepatic;
import org.petctviewer.scintigraphy.liver.Vue_Liver;
import org.petctviewer.scintigraphy.platelet.Vue_Plaquettes;
import org.petctviewer.scintigraphy.scin.VueScin;

public class FenDebug extends JFrame{

	private static final long serialVersionUID = -902779990950720955L;

	public FenDebug() {
		JPanel p = new JPanel(new GridLayout(3,3));
		
		JButton btn_cardiac = new JButton("Cardiac");
		btn_cardiac.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				FenDebug.this.dispose();
				VueScin vue = new Vue_Cardiac();
				vue.run("");
			}
		});
		
		JButton btn_plaquettes = new JButton("Plaquettes");
		btn_plaquettes.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				FenDebug.this.dispose();
				VueScin vue = new Vue_Plaquettes();
				vue.run("");
			}
		});
		
		JButton btn_hepatic = new JButton("Hepatique");
		btn_hepatic.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				FenDebug.this.dispose();
				VueScin vue = new Vue_Hepatic();
				vue.run("");
			}
		});
		
		JButton btn_hepaticdyn = new JButton("Hepatique Dyn");
		btn_hepaticdyn.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				FenDebug.this.dispose();
				VueScin vue = new Vue_HepaticDyn();
				vue.run("");
			}
		});
		
		JButton btn_dyn = new JButton("Scintigraphy Dyn");
		btn_dyn.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				FenDebug.this.dispose();
				VueScin vue = new Vue_GeneralDyn();
				vue.run("");
			}
		});
		
		JButton btn_liver = new JButton("Liver");
		btn_liver.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				FenDebug.this.dispose();
				VueScin vue = new Vue_Liver();
				vue.run("");
			}
		});
		
		p.add(btn_cardiac);
		p.add(btn_plaquettes);
		p.add(btn_hepatic);
		p.add(btn_hepaticdyn);
		p.add(btn_dyn);
		p.add(btn_liver);
		this.add(p);
		
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
}
