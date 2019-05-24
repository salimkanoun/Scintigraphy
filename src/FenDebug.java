import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.calibration.Calibration;
import org.petctviewer.scintigraphy.cardiac.CardiacScintigraphy;
import org.petctviewer.scintigraphy.colonic.ColonicScintigraphy;
import org.petctviewer.scintigraphy.esophageus.application.EsophagealTransit;
import org.petctviewer.scintigraphy.gastric.GastricScintigraphy;
import org.petctviewer.scintigraphy.generic.dynamic.GeneralDynamicScintigraphy;
import org.petctviewer.scintigraphy.generic.statics.StaticScintigraphy;
import org.petctviewer.scintigraphy.hepatic.dynRefactored.HepaticDynScintigraphy;
import org.petctviewer.scintigraphy.lympho.LymphoSintigraphy;
import org.petctviewer.scintigraphy.os.OsScintigraphy;
import org.petctviewer.scintigraphy.platelet.View_Platelet;
import org.petctviewer.scintigraphy.renal.RenalScintigraphy;
import org.petctviewer.scintigraphy.renal.dmsa.DmsaScintigraphy;
import org.petctviewer.scintigraphy.renal.followup.FollowUp;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.preferences.PrefsWindows;
import org.petctviewer.scintigraphy.shunpo.ShunpoScintigraphy;

import ij.plugin.PlugIn;

public class FenDebug extends JFrame {

	private static final long serialVersionUID = -902779990950720955L;

	private JPanel panel;

	private JButton getProgramButton(Class<? extends PlugIn> program) {
		PlugIn vue;
		try {
			vue = program.newInstance();
			JButton btn = new JButton(
					(vue instanceof Scintigraphy ? ((Scintigraphy) vue).getStudyName() : program.getSimpleName()));
			btn.addActionListener(e -> vue.run(""));
			return btn;
		} catch (InstantiationException | IllegalAccessException e1) {
			e1.printStackTrace();
		}
		return null;
	}

	private void registerNewProgram(Class<? extends PlugIn> program) {
		this.panel.add(this.getProgramButton(program));
	}

	public FenDebug() {
		this.setLayout(new BorderLayout());

		JPanel pnl_pref = new JPanel();
		pnl_pref.add(this.getProgramButton(PrefsWindows.class));

		this.panel = new JPanel(new GridLayout(0, 3));

		this.registerNewProgram(CardiacScintigraphy.class);
		this.registerNewProgram(View_Platelet.class);
		this.registerNewProgram(GeneralDynamicScintigraphy.class);
		this.registerNewProgram(RenalScintigraphy.class);
		this.registerNewProgram(DmsaScintigraphy.class);
		this.registerNewProgram(StaticScintigraphy.class);
		this.registerNewProgram(FollowUp.class);
		this.registerNewProgram(Calibration.class);
		this.registerNewProgram(EsophagealTransit.class);
		this.registerNewProgram(ShunpoScintigraphy.class);
		this.registerNewProgram(OsScintigraphy.class);
		this.registerNewProgram(GastricScintigraphy.class);
		this.registerNewProgram(LymphoSintigraphy.class);
		this.registerNewProgram(HepaticDynScintigraphy.class);
		this.registerNewProgram(ColonicScintigraphy.class);

		this.add(this.panel, BorderLayout.CENTER);
		this.add(pnl_pref, BorderLayout.NORTH);

		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

}
