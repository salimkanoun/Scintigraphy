package org.petctviewer.scintigraphy.hepatic.dynRefactored.SecondExam;

import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.library.Library_Roi;

import ij.ImagePlus;
import ij.gui.Roi;

public class FenApplicationSecondHepaticDyn extends FenApplication {

	private static final long serialVersionUID = -910237891674972798L;

	JButton buttonTest;

	public FenApplicationSecondHepaticDyn(ImagePlus imp, String nom) {
		super(imp, nom);

		// mise en place des boutons
		Panel btns_instru = new Panel();
		btns_instru.setLayout(new GridLayout(1, 2));
		buttonTest = new JButton("Load Roi");
		buttonTest.addActionListener(this);
		btns_instru.add(buttonTest);
		this.getPanel_Instructions_btns_droite().add(btns_instru);
		this.pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		FenApplicationSecondHepaticDyn.importRoiList(this, this.getControleur().getModel(), this.getControleur());
	}

	public static void importRoiList(Frame frame, ModeleScin modele, ControleurScin controller) {
		// List<Roi> rois = new ArrayList<>();
		// JFileChooser fc = new JFileChooser();
		// fc.setCurrentDirectory(new File("./"));
		// fc.setDialogTitle("Choose Roi List");
		// int returnVal = fc.showOpenDialog(frame);
		// if (returnVal == JFileChooser.APPROVE_OPTION) {
		//
		// JList list;
		// list = new JList();
		// DefaultListModel listModel = new DefaultListModel();
		// list.setModel(listModel);
		// FileInputStream fis = null;
		// try {
		// fis = new FileInputStream(fc.getSelectedFile().getPath());
		// } catch (FileNotFoundException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }
		// ObjectInputStream ois = null;
		// try {
		// ois = new ObjectInputStream(fis);
		// } catch (IOException e1) {
		// e1.printStackTrace();
		// }
		// try {
		// rois = (List<Roi>) ois.readObject();
		// } catch (ClassNotFoundException | IOException e1) {
		// e1.printStackTrace();
		// System.out.println("erreur");
		// }
		// try {
		// ois.close();
		// } catch (IOException e1) {
		// e1.printStackTrace();
		// }
		//
		// for (Roi r : rois)
		// System.out.println(r);
		//
		// modele.getRoiManager().removeAll();
		// for (Roi r : rois)
		// modele.getRoiManager().addRoi(r);

		// controller.end();

		// IJ.runMacro("roiManager(\"Open\", getArgument());",
		// fc.getSelectedFile().getPath());
		//
		//
		// RoiManager newRoiManager = new RoiManager();
		// for (Roi r : newRoiManager.getRoisAsArray())
		// System.out.println(r);

		// ZipInputStream in = null;
		// ByteArrayOutputStream out = null;
		// rois.clear();
		// int nRois = 0;
		// try {
		// in = new ZipInputStream(new FileInputStream(fc.getSelectedFile().getPath()));
		// byte[] buf = new byte[1024];
		// int len;
		// ZipEntry entry = in.getNextEntry();
		// while (entry!=null) {
		// String name = entry.getName();
		// if (name.endsWith(".roi")) {
		// out = new ByteArrayOutputStream();
		// while ((len = in.read(buf)) > 0)
		// out.write(buf, 0, len);
		// out.close();
		// byte[] bytes = out.toByteArray();
		// RoiDecoder rd = new RoiDecoder(bytes, name);
		// Roi roi = rd.getRoi();
		// if (roi!=null) {
		// name = name.substring(0, name.length()-4);
		// listModel.addElement(name);
		// rois.add(roi);
		// nRois++;
		// }
		// }
		// entry = in.getNextEntry();
		// }
		// in.close();
		// } catch (IOException e) {
		// System.out.println(e.toString());
		// } finally {
		// if (in!=null)
		// try {in.close();} catch (IOException e) {}
		// if (out!=null)
		// try {out.close();} catch (IOException e) {}
		// }
		// if(nRois==0)
		// System.out.println("This ZIP archive does not appear to contain \".roi\"
		// files");

		List<Roi> rois = Library_Roi.getRoiFromZipWithWindow(frame);

		int result = JOptionPane.YES_OPTION;
		if (modele.getRoiManager().getCount() > 0) {
			String message = "Do you want to delete the ROIs already registred ??";
			result = JOptionPane.showConfirmDialog(frame, message, "ROIs already registred", JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);

			if (result == JOptionPane.YES_OPTION) {
				modele.getRoiManager().removeAll();
				for (Roi roi : rois)
					modele.getRoiManager().addRoi(roi);
			} else if (result == JOptionPane.NO_OPTION)
				for (int index = modele.getRoiManager().getCount(); index < rois.size(); index++)
					modele.getRoiManager().addRoi(rois.get(index));

		} else
			for (Roi roi : rois)
				modele.getRoiManager().addRoi(roi);
		// }
	}
}