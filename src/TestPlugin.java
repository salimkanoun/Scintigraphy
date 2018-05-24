import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.petctviewer.scintigraphy.scin.VueScin;

import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Overlay;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;

public class TestPlugin implements PlugIn {

	public static void main(String[] args) {
		TestPlugin plugin = new TestPlugin();
		plugin.run("");
	}

	private RoiManager rm;
	private ImagePlus imp;

	@Override
	public void run(String arg) {
		new ImageJ();
		Read_CD cd = new Read_CD();
		cd.run("");

		JFrame frame = new JFrame();
		frame.setLayout(new FlowLayout());
		JButton b = new JButton("start");
		b.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				start();
			}

		});
		
		JButton add = new JButton("add");
		add.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				add();
			}

		});
		
		frame.add(b);
		frame.add(add);
		frame.pack();
		frame.setVisible(true);

	}

	private void start() {
		this.rm = new RoiManager(); 

		String titles[] = WindowManager.getImageTitles();
		this.imp = WindowManager.getImage(titles[0]);

		Overlay overlay = new Overlay();
		Font font = new Font("Arial", Font.PLAIN, 10);

		overlay.setLabelFont(font, true);

		overlay.drawLabels(true);
		overlay.drawNames(true);
		overlay.selectable(false);

		imp.show();
	}
	
	private void add() {
		rm.addRoi(this.imp.getRoi());
		this.imp.getOverlay().add(this.imp.getRoi());
	}
}
