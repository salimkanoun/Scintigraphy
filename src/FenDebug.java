import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.PrefsWindow;
import org.petctviewer.scintigraphy.calibration.Calibration;
import org.petctviewer.scintigraphy.cardiac.CardiacScintigraphy;
import org.petctviewer.scintigraphy.dynamic.GeneralDynamicScintigraphy;
import org.petctviewer.scintigraphy.hepatic.dyn.HepaticDynamicScintigraphy;
import org.petctviewer.scintigraphy.hepatic.statique.HepaticScintigraphy;
import org.petctviewer.scintigraphy.gastric.Vue_VG_Dynamique;
import org.petctviewer.scintigraphy.platelet.Vue_Plaquettes;
import org.petctviewer.scintigraphy.renal.RenalScintigraphy;
import org.petctviewer.scintigraphy.renal.dmsa.DmsaScintigraphy;
import org.petctviewer.scintigraphy.renal.followup.FollowUp;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.statics.StaticScintigraphy;

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
		
		JPanel p = new JPanel(new GridLayout(5,3));
		
		JButton btn_cardiac = new JButton("Cardiac");
		btn_cardiac.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				Scintigraphy vue = new CardiacScintigraphy();
				vue.run("");
			}
		});
		
		JButton btn_plaquettes = new JButton("Plaquettes");
		btn_plaquettes.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				Scintigraphy vue = new Vue_Plaquettes();
				vue.run("");
			}
		});
		
		JButton btn_hepatic = new JButton("Hepatique");
		btn_hepatic.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				Scintigraphy vue = new HepaticScintigraphy();
				vue.run("");
			}
		});
		
		JButton btn_hepaticdyn = new JButton("Hepatique Dyn");
		btn_hepaticdyn.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				Scintigraphy vue = new HepaticDynamicScintigraphy();
				vue.run("");
			}
		});
		
		JButton btn_dyn = new JButton("Scintigraphy Dyn");
		btn_dyn.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				Scintigraphy vue = new GeneralDynamicScintigraphy();
				vue.run("");
			}
		});
		
		JButton btn_liver = new JButton("Renal");
		btn_liver.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				Scintigraphy vue = new RenalScintigraphy();
				vue.run("");
			}
		});
	
		JButton btn_dmsa = new JButton("DMSA");
		btn_dmsa.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Scintigraphy vue = new DmsaScintigraphy();
				vue.run("");
			}
		});
		
		JButton btn_vgDyn = new JButton("Gastric Dyn");
		btn_vgDyn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Vue_VG_Dynamique vue = new Vue_VG_Dynamique();
				vue.run("");
			}
		});
		
		JButton btn_genStatic = new JButton("Static gen");
		btn_genStatic.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Scintigraphy scin = new StaticScintigraphy();
				scin.run("");
			}
		});
		
		JButton btn_FollowUp = new JButton("Follow-up");
		btn_FollowUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FollowUp cvsComparator = new FollowUp();
				cvsComparator.run("");
			}
		});
		
		JButton btn_Calibration = new JButton("Calibration");
		btn_Calibration.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Calibration calibration = new Calibration();
				calibration.run("");
			}
		});
		
		p.add(btn_cardiac);
		p.add(btn_plaquettes);
		p.add(btn_hepatic);
		p.add(btn_hepaticdyn);
		p.add(btn_dyn);
		p.add(btn_liver);
		p.add(btn_dmsa);
		p.add(btn_vgDyn);
		p.add(btn_genStatic);
		p.add(btn_FollowUp);
		p.add(btn_Calibration);
		
		this.add(p, BorderLayout.CENTER);
		this.add(pnl_pref, BorderLayout.NORTH);
		
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
}
