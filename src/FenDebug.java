import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.PrefsWindow;
import org.petctviewer.scintigraphy.cardiac.Vue_Cardiac;
import org.petctviewer.scintigraphy.dynamic.Vue_GeneralDyn;
import org.petctviewer.scintigraphy.hepatic.dyn.Vue_HepaticDyn;
import org.petctviewer.scintigraphy.hepatic.statique.Vue_Hepatic;
import org.petctviewer.scintigraphy.platelet.Vue_Plaquettes;
import org.petctviewer.scintigraphy.renal.Vue_Renal;
import org.petctviewer.scintigraphy.renal.dmsa.Vue_Dmsa;
import org.petctviewer.scintigraphy.scin.VueScin;

public class FenDebug extends JFrame{

	private static final long serialVersionUID = -902779990950720955L;

	public FenDebug() {
		this.setLayout(new BorderLayout());
		
		JPanel pnl_pref = new JPanel();
		
		JButton btn_pref = new JButton("Preferences");
		btn_pref.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PrefsWindow pref = new PrefsWindow();
				pref.run("");
			}
		});
		
		pnl_pref.add(btn_pref);
		
		JPanel p = new JPanel(new GridLayout(4,3));
		
		JButton btn_cardiac = new JButton("Cardiac");
		btn_cardiac.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				VueScin vue = new Vue_Cardiac();
				vue.run("");
			}
		});
		
		JButton btn_plaquettes = new JButton("Plaquettes");
		btn_plaquettes.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				VueScin vue = new Vue_Plaquettes();
				vue.run("");
			}
		});
		
		JButton btn_hepatic = new JButton("Hepatique");
		btn_hepatic.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				VueScin vue = new Vue_Hepatic();
				vue.run("");
			}
		});
		
		JButton btn_hepaticdyn = new JButton("Hepatique Dyn");
		btn_hepaticdyn.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				VueScin vue = new Vue_HepaticDyn();
				vue.run("");
			}
		});
		
		JButton btn_dyn = new JButton("Scintigraphy Dyn");
		btn_dyn.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				VueScin vue = new Vue_GeneralDyn();
				vue.run("");
			}
		});
		
		JButton btn_liver = new JButton("Renal");
		btn_liver.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				VueScin vue = new Vue_Renal();
				vue.run("");
			}
		});
	
		JButton btn_dmsa = new JButton("DMSA");
		btn_dmsa.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				VueScin vue = new Vue_Dmsa();
				vue.run("");
			}
		});
		
		p.add(btn_cardiac);
		p.add(btn_plaquettes);
		p.add(btn_hepatic);
		p.add(btn_hepaticdyn);
		p.add(btn_dyn);
		p.add(btn_liver);
		p.add(btn_dmsa);
		
		this.add(p, BorderLayout.CENTER);
		this.add(pnl_pref, BorderLayout.NORTH);
		
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
}
