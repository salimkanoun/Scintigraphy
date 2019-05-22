package org.petctviewer.scintigraphy.scin.library;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JList;

import org.petctviewer.scintigraphy.scin.ControllerWorkflow;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.io.RoiDecoder;
import ij.io.RoiEncoder;
import ij.io.SaveDialog;
import ij.plugin.frame.Recorder;
import ij.plugin.frame.RoiManager;

public class Library_Roi {

	public static final int HEART = 0, INFLAT = 1, KIDNEY = 2, INFLATGAUCHE = 3, INFLATDROIT = 4;

	// cree la roi de bruit de fond
	public static Roi createBkgRoi(Roi roi, ImagePlus imp, int organ) {
		Roi bkg = null;
		RoiManager rm = new RoiManager(true);
		rm.setVisible(true);

		switch (organ) {
		case Library_Roi.KIDNEY:
			// largeur a prendre autour du rein
			int largeurBkg = 1;
			if (imp.getDimensions()[0] >= 128) {
				largeurBkg = 2;
			}

			rm.addRoi(roi);

			rm.select(rm.getCount() - 1);
			IJ.run(imp, "Enlarge...", "enlarge=" + largeurBkg + " pixel");
			rm.addRoi(imp.getRoi());

			rm.select(rm.getCount() - 1);
			IJ.run(imp, "Enlarge...", "enlarge=" + largeurBkg + " pixel");
			rm.addRoi(imp.getRoi());

			rm.setSelectedIndexes(new int[] { rm.getCount() - 2, rm.getCount() - 1 });
			rm.runCommand(imp, "XOR");

			bkg = imp.getRoi();
			break;

		case Library_Roi.HEART:
			// TODO
			break;

		case Library_Roi.INFLATGAUCHE:
			bkg = Library_Roi.createBkgInfLat(roi, imp, -1, rm);
			break;

		case Library_Roi.INFLATDROIT:
			bkg = Library_Roi.createBkgInfLat(roi, imp, 1, rm);
			break;

		default:
			bkg = roi;
			break;
		}

		rm.dispose();

		bkg.setStrokeColor(Color.GRAY);
		return bkg;
	}

	/***************************** Private Static ************************/
	static Roi createBkgInfLat(Roi roi, ImagePlus imp, int xOffset, RoiManager rm) {
		// on recupere ses bounds
		Rectangle bounds = roi.getBounds();

		Roi liver = (Roi) roi.clone();
		rm.addRoi(liver);

		int[] size = { (bounds.width / 4) * xOffset, (bounds.height / 4) * 1 };

		Roi liverShift = (Roi) roi.clone();
		liverShift.setLocation(liverShift.getXBase() + size[0], liverShift.getYBase() + size[1]);
		rm.addRoi(liverShift);

		// renvoi une section de la roi
		rm.setSelectedIndexes(new int[] { 0, 1 });
		rm.runCommand(imp, "XOR");
		rm.runCommand(imp, "Split");

		int x = bounds.x + bounds.width / 2;
		int y = bounds.y + bounds.height / 2;
		int w = size[0] * imp.getWidth();
		int h = size[1] * imp.getHeight();

		// permet de diviser la roi
		Rectangle splitter;

		if (w > 0) {
			splitter = new Rectangle(x, y, w, h);
		} else {
			splitter = new Rectangle(x + w, y, -w, h);
		}

		Roi rect = new Roi(splitter);
		rm.addRoi(rect);

		rm.setSelectedIndexes(new int[] { rm.getCount() - 1, rm.getCount() - 2 });
		rm.runCommand(imp, "AND");

		Roi bkg = (Roi) imp.getRoi().clone();
		// int[] offset = new int[] { size[0] / 4, size[1] / 4 };

		// on deplace la roi pour ne pas qu'elle soit collee
		bkg.setLocation(bkg.getXBase() + xOffset, bkg.getYBase() + 1);

		return bkg;
	}

	/**
	 * Open a zip file and return all the .roi files inside as a List<Roi>, using
	 * {@link ZipInputStream}, {@link ByteArrayOutputStream} and {@link RoiDecoder}
	 * to process.
	 * 
	 * @param path
	 *            the system-dependent file name.
	 * @return A list of ROIs, contained in the zip file.
	 */
	public static List<Roi> getRoiFromZip(String path) {

		JList list;
		list = new JList();
		DefaultListModel listModel = new DefaultListModel();
		list.setModel(listModel);

		List<Roi> rois = new ArrayList<>();
		ZipInputStream in = null;
		ByteArrayOutputStream out = null;
		rois.clear();
		int nRois = 0;
		try {
			in = new ZipInputStream(new FileInputStream(path));
			byte[] buf = new byte[1024];
			int len;
			ZipEntry entry = in.getNextEntry();
			while (entry != null) {
				String name = entry.getName();
				if (name.endsWith(".roi")) {
					out = new ByteArrayOutputStream();
					while ((len = in.read(buf)) > 0)
						out.write(buf, 0, len);
					out.close();
					byte[] bytes = out.toByteArray();
					RoiDecoder rd = new RoiDecoder(bytes, name);
					Roi roi = rd.getRoi();
					if (roi != null) {
						name = name.substring(0, name.length() - 4);
						listModel.addElement(name);
						rois.add(roi);
						nRois++;
					}
				}
				entry = in.getNextEntry();
			}
			in.close();
		} catch (IOException e) {
			System.out.println(e.toString());
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
				}
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
				}
		}
		if (nRois == 0)
			System.out.println("This ZIP archive does not appear to contain \".roi\" files");

		return rois;
	}

	/**
	 * This method open a JFileChooser to select a .zip contening ROI files, and
	 * return the ROIs as a list.
	 * 
	 * @param frame
	 *            - the parent component of the dialog, can be null ;
	 * @return A list of ROIs.
	 * 
	 * @see {@link Library_Roi#getRoiFromZip(String)}
	 */
	public static List<Roi> getRoiFromZipWithWindow(Component frame) {

		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(new File("./"));
		fc.setDialogTitle("Choose .zip containing rois");
		int returnVal = fc.showOpenDialog(frame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return Library_Roi.getRoiFromZip(fc.getSelectedFile().getPath());
		}
		return null;
	}

	
	
	/**
	 * Save the ROIs from a RoiManager.
	 * @param model
	 * @param path
	 * @return
	 */
	public static boolean saveRois(RoiManager roiManager, String path) {
		
		ArrayList rois = (ArrayList) Arrays.asList(roiManager.getRoisAsArray());
		
		int nbRoi = roiManager.getCount();
		
		if (path == null) {
			SaveDialog sd = new SaveDialog("Save ROIs...", "RoiSet", ".zip");
			String name = sd.getFileName();
			if (name == null)
				return false;
			if (!(name.endsWith(".zip") || name.endsWith(".ZIP")))
				name = name + ".zip";
			String dir = sd.getDirectory();
			path = dir + name;
		}
		DataOutputStream out = null;
		IJ.showStatus("Saving " + nbRoi + " ROIs " + " to " + path);
		long t0 = System.currentTimeMillis();
		String[] names = new String[roiManager.getCount()];
		for (int i = 0; i < roiManager.getCount() ; i++)
			names[i] = (String) roiManager.getRoi(i).getName();
		try {
			ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(path)));
			out = new DataOutputStream(new BufferedOutputStream(zos));
			RoiEncoder re = new RoiEncoder(out);
			for (int i = 0; i < nbRoi; i++) {
				IJ.showProgress(i, nbRoi);
				String label = getUniqueName(names, i);
				Roi roi = (Roi) rois.get(i);
				if (IJ.debugMode)
					IJ.log("saveMultiple: " + i + "  " + label + "  " + roi);
				if (roi == null)
					continue;
				if (!label.endsWith(".roi"))
					label += ".roi";
				zos.putNextEntry(new ZipEntry(label));
				re.write(roi);
				out.flush();
			}
			out.close();
		} catch (IOException e) {
			System.out.println("" + e);
			return false;
		} finally {
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
				}
		}
		double time = (System.currentTimeMillis() - t0) / 1000.0;
		IJ.showProgress(1.0);
		IJ.showStatus(IJ.d2s(time, 3) + " seconds, " + nbRoi + " ROIs, " + path);
		if (Recorder.record && !IJ.isMacro())
			Recorder.record("roiManager", "Save", path);
		return true;
	}
	
	
	public static boolean saveRois(RoiManager roiManager, String path, ControllerWorkflow controller) {
		
		
		
		return true;
	}
	
	
	private static String getUniqueName(String[] names,int  i) {
		return null;
	}

}
