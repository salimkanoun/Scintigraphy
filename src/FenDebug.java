import ij.plugin.PlugIn;
import org.petctviewer.scintigraphy.calibration.Calibration;
import org.petctviewer.scintigraphy.cardiac.CardiacScintigraphy;
import org.petctviewer.scintigraphy.colonic.ColonicScintigraphy;
import org.petctviewer.scintigraphy.esophageus.application.EsophagealTransitScintigraphy;
import org.petctviewer.scintigraphy.gallbladder.application.GallbladderScintigraphy;
import org.petctviewer.scintigraphy.gastric.GastricScintigraphy;
import org.petctviewer.scintigraphy.generic.dynamic.GeneralDynamicScintigraphy;
import org.petctviewer.scintigraphy.generic.statics.StaticScintigraphy;
import org.petctviewer.scintigraphy.hepatic.HepaticDynScintigraphy;
import org.petctviewer.scintigraphy.hepatic.radioEmbolization.LiverScintigraphy;
import org.petctviewer.scintigraphy.hepatic.scintivol.ScintivolScintigraphy;
import org.petctviewer.scintigraphy.liquid.LiquidScintigraphy;
import org.petctviewer.scintigraphy.lympho.LymphoScintigraphy;
import org.petctviewer.scintigraphy.mibg.MIBGScintigraphy;
import org.petctviewer.scintigraphy.os.OsScintigraphy;
import org.petctviewer.scintigraphy.parathyroid.ParathyroidScintigraphy;
import org.petctviewer.scintigraphy.platelet.PlateletScintigraphy;
import org.petctviewer.scintigraphy.renal.RenalScintigraphy;
import org.petctviewer.scintigraphy.renal.dmsa.DmsaScintigraphy;
import org.petctviewer.scintigraphy.renal.followup.FollowUpScintigraphy;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.preferences.PrefWindow;
import org.petctviewer.scintigraphy.shunpo.ShunpoScintigraphy;
import org.petctviewer.scintigraphy.thyroid.ThyroidScintigraphy;
import org.petctviewer.scintigraphy.salivaryGlands.SalivaryGlandsScintigraphy;


import javax.swing.*;
import java.awt.*;

public class FenDebug extends JFrame {

	private static final long serialVersionUID = -902779990950720955L;

	private final JPanel panel;

	public FenDebug() {
		this.setLayout(new BorderLayout());

		JPanel pnl_pref = new JPanel();
		pnl_pref.add(this.getProgramButton(PrefWindow.class));

		this.panel = new JPanel(new GridLayout(0, 3));

		this.registerNewProgram(CardiacScintigraphy.class);
		this.registerNewProgram(GeneralDynamicScintigraphy.class);
		this.registerNewProgram(RenalScintigraphy.class);
		this.registerNewProgram(DmsaScintigraphy.class);
		this.registerNewProgram(StaticScintigraphy.class);
		this.registerNewProgram(FollowUpScintigraphy.class);
		this.registerNewProgram(Calibration.class);
		this.registerNewProgram(EsophagealTransitScintigraphy.class);
		this.registerNewProgram(ShunpoScintigraphy.class);
		this.registerNewProgram(OsScintigraphy.class);
		this.registerNewProgram(GastricScintigraphy.class);
		this.registerNewProgram(LymphoScintigraphy.class);
		this.registerNewProgram(HepaticDynScintigraphy.class);
		this.registerNewProgram(ColonicScintigraphy.class);
		this.registerNewProgram(LiquidScintigraphy.class);
		this.registerNewProgram(MIBGScintigraphy.class);
		this.registerNewProgram(PlateletScintigraphy.class);
		this.registerNewProgram(LiverScintigraphy.class);
		this.registerNewProgram(ThyroidScintigraphy.class);
		this.registerNewProgram(ParathyroidScintigraphy.class);
		this.registerNewProgram(GallbladderScintigraphy.class);
		this.registerNewProgram(SalivaryGlandsScintigraphy.class);
		this.registerNewProgram(ScintivolScintigraphy.class);

		this.add(this.panel, BorderLayout.CENTER);
		this.add(pnl_pref, BorderLayout.NORTH);

		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

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

}
