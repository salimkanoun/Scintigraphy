import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.cardiac.Vue_Cardiac;
import org.petctviewer.scintigraphy.platelet.Vue_Plaquettes;
import org.petctviewer.scintigraphy.scin.view.VueScin;

public class FenDebug extends JFrame{

	public FenDebug() {
		JPanel p = new JPanel();
		
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
				Vue_Plaquettes vue = new Vue_Plaquettes();
				vue.run("");
			}
		});
		
		p.add(btn_cardiac);
		p.add(btn_plaquettes);
		this.add(p);
		
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
}
