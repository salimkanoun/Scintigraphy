package org.petctviewer.scintigraphy.scin.json;

import com.google.gson.*;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Roi;
import ij.io.RoiDecoder;
import ij.io.RoiEncoder;
import ij.io.SaveDialog;
import ij.plugin.frame.Recorder;
import ij.plugin.frame.RoiManager;
import org.apache.commons.io.FileUtils;
import org.petctviewer.scintigraphy.cardiac.ControllerWorkflowCardiac;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.exceptions.UnauthorizedRoiLoadException;
import org.petctviewer.scintigraphy.scin.exceptions.UnloadRoiException;
import org.petctviewer.scintigraphy.scin.instructions.Instruction;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawLoopInstruction;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawSymmetricalLoopInstruction;
import org.petctviewer.scintigraphy.scin.instructions.execution.ContaminationAskInstruction;
import org.petctviewer.scintigraphy.scin.instructions.generator.DefaultGenerator;
import org.petctviewer.scintigraphy.scin.json.InstructionFromGson.DrawInstructionType;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.model.ModelScin;
import org.petctviewer.scintigraphy.scin.preferences.PrefTabMain;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * This class put together all saving method created for every exams.
 *
 */
public class SaveAndLoad {

	public SaveAndLoad() {
	}

	/**
	 * Permet de realiser l'export du fichier CSV et des ROI contenues dans l'export
	 * Manager vers le repertoire d'export defini dans les options
	 *
	 * @param resultats
	 *            : Resultats a exporter (utiliser le format csv)
	 * @param nomProgramme
	 *            : le studyName du programme (sera utilise comme sous repertoire)
	 * @param imp
	 *            : l'ImagePlus d'une image originale ou de la capture secondaire
	 *            auquel on a ajoute le header, permet de recuperer le studyName,
	 *            l'ID et la date d'examen
	 * @param additionalInfo
	 *            :String qui sera rajoutée à la fin du studyName du fichier
	 */
	public void exportAllWithWorkflow(String resultats, String nomProgramme, ImagePlus imp, String additionalInfo,
			List<ControllerWorkflow> controller) {

		String[] infoPatient = Library_Capture_CSV.getInfoPatient(imp);
		StringBuilder content = this.initCSVVertical(infoPatient);

		content.append(resultats);

		this.saveFiles(imp, content, nomProgramme, infoPatient, additionalInfo, controller);
	}

	/**
	 * Add ""+i+"_" to the begining of names[i]. This method is used to get different name for every Roi.
	 *
	 * @param names
	 *            Array of name
	 * @param i
	 *            Number to add at the begining
	 * @return The name modified
	 */
	private static String getUniqueName(String[] names, int i) {
		return i + "_" + names[i];
	}

	/**
	 * Permet de realiser l'export du fichier CSV vers le repertoire d'export defini
	 * dans les options
	 *
	 * @param resultats
	 *            : Resultats a exporter (utiliser le format csv)
	 * @param nomProgramme
	 *            : Le studyName du programme (sera utilise comme sous repertoire)
	 * @param imp
	 *            : L'ImagePlus d'une image originale ou de la capture secondaire
	 *            auquel on a ajoute le header, permet de recuperer le studyName,
	 *            l'ID et la date d'examen
	 * @param additionalInfo
	 *            : String qui sera rajoutée à la fin du studyName du fichier
	 */
	public void exportAllWithoutWorkflow(String resultats, String nomProgramme, ImagePlus imp, String additionalInfo) {

		String[] infoPatient = Library_Capture_CSV.getInfoPatient(imp);
		StringBuilder csv = this.initCSVVertical(infoPatient);

		csv.append(resultats);

		// On recupere le path de sauvegarde
		String path = Prefs.get(PrefTabMain.PREF_SAVE_DIRECTORY, null);
		boolean testEcriture = false;

		// On verifie que le path est writable si il existe
		if (path != null) {
			File testPath = new File(path);
			testEcriture = testPath.canWrite();
		}

		if (path != null && !testEcriture) {
			// Si pas de repertoire defini on notifie l'utilisateur
			IJ.showMessage("CSV Path not writable, CSV/ZIP export has failed");
		}
		if (path != null && testEcriture) {
			// On construit le sous repertoire avecle studyName du programme et l'ID du
			// Patient
			String pathFinal = path + File.separator + nomProgramme + File.separator + infoPatient[1];
			File subDirectory = new File(pathFinal);
			if (subDirectory.mkdirs()) {

				String nomFichier = infoPatient[1] + "_" + infoPatient[2] + additionalInfo;

				File f = new File(subDirectory + File.separator + nomFichier + ".csv");

				// On ecrit les CSV
				PrintWriter pw;
				try {
					pw = new PrintWriter(f);
					pw.write(csv.toString());
					pw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				// On sauve l'image en jpeg
				IJ.saveAs(imp, "Jpeg", pathFinal + File.separator + nomFichier + ".jpg");
			} else {
				System.err.println("An error occurred while trying to create the directories for the path: " + pathFinal
						+ ". Aborting creation of CSV, ZIP and JPEG.");
			}
		}
	}

	/**
	 * Save the ROIs from a RoiManager. Do the same than the macro, but associate
	 * name to respect the initial order of the RoiManager, to be able to get them
	 * back in the same order. Will be saved in a .zip, with the workflow.json.
	 *
	 * @param controller
	 *            Associated ControllerWorkflow, to save the workflow in Json format
	 * @param path
	 *            Path to save the .zip
	 */
	public void saveRois(ControllerWorkflow controller, String path) {

		RoiManager roiManager = controller.getRoiManager();

		List<Roi> rois = Arrays.asList(roiManager.getRoisAsArray());

		int nbRoi = roiManager.getCount();

		if (path == null) {
			SaveDialog sd = new SaveDialog("Save ROIs...", "RoiSet", ".zip");
			String name = sd.getFileName();
			if (name == null)
				return;
			if (!(name.endsWith(".zip") || name.endsWith(".ZIP")))
				name = name + ".zip";
			String dir = sd.getDirectory();
			path = dir + name;
		}
		DataOutputStream out = null;
		IJ.showStatus("Saving " + nbRoi + " ROIs " + " to " + path);
		long t0 = System.currentTimeMillis();
		String[] names = new String[roiManager.getCount()];
		String[] label = new String[names.length];
		for (int i = 0; i < roiManager.getCount(); i++)
			names[i] = roiManager.getRoi(i).getName();
		try {
			ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(path)));
			out = new DataOutputStream(new BufferedOutputStream(zos));
			RoiEncoder re = new RoiEncoder(out);
			for (int i = 0; i < nbRoi; i++) {
				IJ.showProgress(i, nbRoi);
				label[i] = getUniqueName(names, i);
				Roi roi = rois.get(i);
				if (IJ.debugMode)
					IJ.log("saveMultiple: " + i + "  " + Arrays.toString(label) + "  " + roi);
				if (roi == null)
					continue;

				// roi.setName(label[i]);
				if (!label[i].endsWith(".roi"))
					label[i] += ".roi";
				zos.putNextEntry(new ZipEntry(label[i]));
				re.write(roi);
				out.flush();
			}

			Gson gson = new GsonBuilder().create();

			zos.putNextEntry(new ZipEntry("workflow.json"));
			System.out.println("Json sauvegarde : ");
			// System.out.println(gson.toJson(this.saveWorkflowToJson(controller, label)));
			out.writeBytes(gson.toJson(this.saveWorkflowToJson(controller, label)));
			out.flush();
			zos.closeEntry();
			zos.close();
			out.close();
		} catch (IOException e) {
			System.out.println("" + e);
			return;
		} finally {
			if (out != null)
				try {
					out.close();
				} catch (IOException ignored) {
				}
		}
		double time = (System.currentTimeMillis() - t0) / 1000.0;
		IJ.showProgress(1.0);
		IJ.showStatus(IJ.d2s(time, 3) + " seconds, " + nbRoi + " ROIs, " + path);
		if (Recorder.record && !IJ.isMacro())
			Recorder.record("roiManager", "Save", path);
	}

	/**
	 * Save the different files on the disk
	 *
	 * @param imp
	 *            ImagePlus to be saved as .jpg
	 * @param csv
	 *            CSV to be saved
	 * @param programName
	 * @param infoPatient
	 * @param additionalInfo
	 * @param controller
	 *            Controller to be transformed as Json
	 */
	public void saveFiles(ImagePlus imp, StringBuilder csv, String programName, String[] infoPatient,
						  String additionalInfo, List<ControllerWorkflow> controller) {

		RoiManager roiManager = controller.get(0).getRoiManager();

		// On recupere le path de sauvegarde
		String path = Prefs.get(PrefTabMain.PREF_SAVE_DIRECTORY, null);
		boolean testEcriture = false;

		// On verifie que le path est writable si il existe
		if (path != null) {
			File testPath = new File(path);
			testEcriture = testPath.canWrite();
		}

		if (path != null && !testEcriture) {
			// Si pas de repertoire defini on notifie l'utilisateur
			IJ.showMessage("CSV Path not writable, CSV/ZIP export has failed");
		}
		if (path != null && testEcriture) {
			// On construit le sous repertoire avecle studyName du programme et l'ID du
			// Patient
			String pathFinal = path + File.separator + programName + File.separator + infoPatient[1] + File.separator +
					infoPatient[2];
			String nomFichier = infoPatient[1] + "_" + infoPatient[2] + additionalInfo;
			File subDirectory = new File(pathFinal);
			if (subDirectory.isDirectory()) {
				try {
					FileUtils.cleanDirectory(subDirectory); // clean out directory (this is optional -- but good know)
					FileUtils.forceDelete(subDirectory); // delete directory
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (subDirectory.mkdirs()) {

				File f = new File(subDirectory + File.separator + nomFichier + ".csv");

				// On ecrit les CSV
				PrintWriter pw;
				try {
					pw = new PrintWriter(f);
					pw.write(csv.toString());
					pw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				// On ecrit le ZIP contenant la sauvegarde des ROIs
				Roi[] rois2 = roiManager.getRoisAsArray();
				int[] tab = new int[rois2.length];
				for (int i = 0; i < rois2.length; i++)
					tab[i] = i;
				roiManager.setSelectedIndexes(tab);

				for (ControllerWorkflow currentController : controller)
					this.saveRois(currentController, pathFinal + File.separator + nomFichier + "_" +
							currentController.getClass().getSimpleName() + ".zip");

				// On sauve l'image en jpeg
				IJ.saveAs(imp, "Jpeg", pathFinal + File.separator + nomFichier + ".jpg");
			} else {
				System.err.println(
						"An error occurred while trying to create the directories for the path: " + pathFinal +
								". Aborting creation of CSV, ZIP and JPEG.");
			}
		}
	}

	/**
	 * This method creats a .json file. It scans every existing instructions of
	 * every existing workflow, take only instructions using a ROI, and add them to
	 * the Json.<br/>
	 * It's a {@link WorkflowsFromGson} object, containing a list of
	 * {@link WorkflowFromGson} objects, each containing a list of
	 * {@link InstructionFromGson} objects, in the form of a Json file.<br/>
	 * Add at the end the information about the patient, as a
	 * {@link PatientFromGson}, to be able to do an integrity test.
	 * 
	 * @param label
	 *            They are the name of the ROIs to save. Usefeull when you want to
	 *            give special names to your Intruction list.
	 * @return JsonElement containing a tree of every Workflow, and every
	 *         Instruction drawing a ROI
	 */
	public JsonElement saveWorkflowToJson(ControllerWorkflow controller, String[] label) {

		// Pretty print
		// Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls()
		// .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

		// Formal data sending
		Gson gson = new GsonBuilder().serializeNulls().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
				.create();

		JsonObject workflowsObject = new JsonObject();
		JsonArray workflowsArray = new JsonArray();

		for (Workflow workflow : controller.getWorkflows()) {
			JsonObject currentWorkflow = new JsonObject();
			JsonArray instructionsArray = new JsonArray();
			for (Instruction instruction : workflow.getInstructions()) {
				if (instruction.saveRoi() || instruction.isRoiVisible()) {
					JsonObject currentInstruction = new JsonObject();
					currentInstruction.addProperty("InstructionType", instruction.getClass().getSimpleName());
					currentInstruction.addProperty("IndexRoiToEdit", instruction.getRoiIndex());

					if (controller.getModel().getRoiManager().getRoi(instruction.getRoiIndex()) != null)
						currentInstruction.addProperty("NameOfRoi",
								controller.getModel().getRoiManager().getRoi(instruction.getRoiIndex()).getName());
					else
						currentInstruction.addProperty("NameOfRoi", instruction.getRoiName());
					if (instruction.getRoiIndex() != -1) {
						if (label[instruction.getRoiIndex()].endsWith(".roi"))
							label[instruction.getRoiIndex()] = label[instruction.getRoiIndex()].substring(0,
									label[instruction.getRoiIndex()].length() - 4);
						currentInstruction.addProperty("NameOfRoiFile", label[instruction.getRoiIndex()]);
					} else
						currentInstruction.addProperty("NameOfRoiFile", "null");
					// instructionsArray.add((JsonObject) gson.toJsonTree(instruction));
					instructionsArray.add(gson.toJsonTree(currentInstruction));
				}
			}
			currentWorkflow.add("Intructions", instructionsArray);
			workflowsArray.add(currentWorkflow);
		}

		workflowsObject.add("Workflows", workflowsArray);

		JsonObject patientObject = new JsonObject();

		String[] infoPatient = Library_Capture_CSV.getInfoPatient(controller.getModel().getImagePlus());
		// HashMap<String, String> patientInfo =
		// Library_Capture_CSV.getPatientInfo(this.getModel().getImagePlus());

		patientObject.addProperty("Name", infoPatient[0]);
		patientObject.addProperty("ID", infoPatient[1]);
		patientObject.addProperty("Date", infoPatient[2]);
		patientObject.addProperty("AccessionNumber", infoPatient[3]);
		patientObject.addProperty("ControllerName", controller.getClass().getSimpleName());

		// patientObject.addProperty("Name",
		// patientInfo.get(Library_Capture_CSV.PATIENT_INFO_NAME));
		// patientObject.addProperty("ID",
		// patientInfo.get(Library_Capture_CSV.PATIENT_INFO_ID));
		// patientObject.addProperty("Date",
		// patientInfo.get(Library_Capture_CSV.PATIENT_INFO_DATE));
		// patientObject.addProperty("AccessionNumber",
		// patientInfo.get(Library_Capture_CSV
		// .PATIENT_INFO_ACCESSION_NUMBER));

		workflowsObject.add("Patient", patientObject);
		// System.out.println("\n\n\n --------------------------- TEST
		// --------------------------- \n");
		// System.out.println(gson.toJson(workflowsObject));
		// System.out.println("\n --------------------------- Supposed
		// --------------------------- \n");
		// System.out.println(gson.toJson(controller.getWorkflows()));
		// System.out.println("\n\n\n");

		// try (FileWriter writer = new FileWriter(path)) {
		// gson.toJson(workflowsObject, writer);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		//
		// try (ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(new
		// FileOutputStream(path)));
		// Writer writer = new OutputStreamWriter(zip);) {
		// zip.putNextEntry(new ZipEntry("workflow.json"));
		// gson.toJson(workflowsObject, writer);
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

		// this.loadWorkflow(path);

		return workflowsObject;
	}

	/**
	 * Take a Json file representing a Workflow array, and transform it to a
	 * {@link WorkflowsFromGson} object, containing a list of
	 * {@link WorkflowFromGson} objects, each containing a list of
	 * {@link InstructionFromGson} objects.<br/>
	 * It also do an integrity test with the current patient, and the
	 * {@link PatientFromGson} in the Json.<br/>
	 * <br/>
	 * This method check InstructionType differences, patient information
	 * differences, and auto-increment the DrawLoop and DrawSymmetricalLoop
	 * instructions.
	 * 
	 * @param string
	 *            The String object representing the Json file.
	 * @return The {@link WorkflowsFromGson} object.
	 * @throws UnloadRoiException
	 */
	public WorkflowsFromGson loadWorkflows(ControllerWorkflow controller, String string) throws UnloadRoiException {

		Gson gson = new GsonBuilder().serializeNulls().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
				.setPrettyPrinting().create();
		WorkflowsFromGson workflowsFromGson;

		// String path = "D:\\Bureau\\IUT\\Oncopole\\workflow.json";
		// try (Reader reader = new FileReader(path)) {
		//
		// // Convert JSON to WorkflowsFromGson
		// workflowsFromGson = gson.fromJson(reader, WorkflowsFromGson.class);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

		workflowsFromGson = gson.fromJson(string, WorkflowsFromGson.class);
		PatientFromGson patientFromGson = workflowsFromGson.getPatient();

		if (workflowsFromGson != null) {
			if (workflowsFromGson.getWorkflows().size() != controller.getWorkflows().length) {
				System.out.println("DIFFERENT NUMBER OF WORKFLOWS, CANNOT LOAD BACKUP");
				return null;
			}

			String[] currentPatient = Library_Capture_CSV.getInfoPatient(controller.getModel().getImagePlus());

			String currentPatientDate = "";
			try {
				currentPatientDate = new SimpleDateFormat("yyyyMMdd").parse(currentPatient[2]).toString();
			} catch (ParseException e) {
				e.printStackTrace();
			}

			int differenceNumber = 0;

			if (!patientFromGson.getAccessionNumber().equals(currentPatient[3]))
				differenceNumber++;
			if (!patientFromGson.getName().equals(currentPatient[0]))
				differenceNumber++;
			if (!patientFromGson.getID().equals(currentPatient[1]))
				differenceNumber++;
			if (!patientFromGson.getDate().toString().equals(currentPatientDate))
				differenceNumber++;

			Object[][] difference = new Object[differenceNumber][3];

			int indexDifference = 0;
			if (!patientFromGson.getAccessionNumber().equals(currentPatient[3])) {
				difference[indexDifference] = new String[] { "Accession Number",
						"" + patientFromGson.getAccessionNumber(), "" + currentPatient[3] };
				indexDifference++;
			}
			if (!patientFromGson.getName().equals(currentPatient[0])) {
				difference[indexDifference] = new String[] { "Name", "" + patientFromGson.getName(),
						"" + currentPatient[0] };
				indexDifference++;
			}
			if (!patientFromGson.getID().equals(currentPatient[1])) {
				difference[indexDifference] = new String[] { "ID", "" + patientFromGson.getID(),
						"" + currentPatient[1] };
				indexDifference++;
			}

			if (!patientFromGson.getDate().toString().equals(currentPatientDate))
				difference[indexDifference] = new String[] { "Date", "" + patientFromGson.getDate(),
						"" + currentPatientDate };

			if (differenceNumber != 0) {

				JPanel flow = new JPanel(new GridLayout(4, 1));

				flow.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
				flow.add(new JLabel("There is more than one patient ID"));

				JPanel panel = new JPanel(new BorderLayout());

				JTable differences = new JTable(difference,
						new String[] { "Conflict", "From Json", " Current patient" });

				panel.add(differences.getTableHeader(), BorderLayout.NORTH);
				panel.add(differences, BorderLayout.CENTER);
				flow.add(panel);

				flow.add(new JLabel("Do you want to still process the exam ?"));
				// WindowDifferentPatient fen = new WindowDifferentPatient(difference);
				// fen.setModal(true);
				// fen.setVisible(true);
				// fen.setAlwaysOnTop(true);
				// fen.setLocationRelativeTo(null);
				int result = JOptionPane.showConfirmDialog(null, flow, "My custom dialog", JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.NO_OPTION) {
					throw new UnloadRoiException();
				}
			}

			// int nbDrawInstruction = 0;
			// for (Workflow workflow : controller.getWorkflows())
			// for(Instruction instruction : workflow.getInstructions())
			// if(instruction.saveRoi() || instruction.isRoiVisible())
			// nbDrawInstruction++;
			//
			// if (nbDrawInstruction != workflowsFromGson.Workflows.size()) {
			// System.out.println("NOMBRE D'INSTRUCTION DIFFERENTE, IMPOSSIBLE DE CHARGER LA
			// SAUVEGARDE");
			// return false;
			// }

			for (int index = 0; index < controller.getWorkflows().length; index++) {
				int specialIndex = 0;

				/*
				 * Because Cardiac has special behavior. In fact, the DrawLoop doesn't exist at
				 * start, but you choose to create one. With this << if >>, I sumilate this
				 * creation if necessary.
				 */
				if (patientFromGson.getControllerName().toString().equals(ControllerWorkflowCardiac.simpleName)
						&& controller.getWorkflows()[index]
								.getInstructionAt(0) instanceof ContaminationAskInstruction) {
					if (controller.getWorkflows()[index].getInstructions().size() > 1)
						controller.getWorkflows()[index].addInstruction(
								((ContaminationAskInstruction) controller.getWorkflows()[index].getInstructionAt(0))
										.getInstructionToGenerate());
					((ContaminationAskInstruction) controller.getWorkflows()[index].getInstructionAt(0))
							.setInstructionValidated();
				}

				for (int j = 0; j < controller.getWorkflows()[index].getInstructions().size(); j++) {
					if (controller.getWorkflows()[index].getInstructionAt(j).saveRoi()) {
						// System.out.println(" j : " + j);
						// System.out.println("specialIndex" + specialIndex);
						if (workflowsFromGson.getWorkflowAt(index).getInstructions().size() != 0) {
							InstructionFromGson intructionFromGson = workflowsFromGson.getWorkflowAt(index)
									.getInstructionAt(specialIndex);
							String typeOfIntructionFromGson = intructionFromGson.getInstructionType();

							if (!controller.getWorkflows()[index].getInstructionAt(j).getClass().getSimpleName()
									.equals(typeOfIntructionFromGson)) {
								System.out
										.println("LES INSTRUCTIONS NE SONT PAS LES MÊMES, IMPOSSIBLE DE CHARGER LA"
												+ " " + "SAUVEGARDE (" + controller.getWorkflows()[index]
														.getInstructionAt(j).getClass().getSimpleName()
												+ ", " + typeOfIntructionFromGson + ")");
								return null;
							}

							// if
							// (!this.getModel().getRoiManager().getRoi(controller.getWorkflows()[index]
							// .getInstructionAt(j)
							// .roiToDisplay()).getName()
							// .equals(intructionFromGson.getNameOfRoi())) {
							// System.out.println(
							// "LES INSTRUCTIONs NE SONT PAS DU MÊME TYPE, IMPOSSIBLE DE CHARGER LA
							// SAUVEGARDE");
							// System.out.println(controller.getWorkflows()[index].getInstructionAt(j).getClass()
							// .getSimpleName());
							// System.out.println(typeOfIntructionFromGson);
							// return null;
							// }

							// If it's a DrawLoop
							if ((typeOfIntructionFromGson.equals(DrawInstructionType.DRAW_LOOP.getName()))
									|| typeOfIntructionFromGson
											.equals(DrawInstructionType.DRAW_SYMMETRICAL_LOOP.getName())) {
								// If this DrawLoop is not the last
								if (workflowsFromGson.getWorkflowAt(index).getInstructions().size() > specialIndex
										+ 1) {
									InstructionFromGson nextIntructionFromGson = workflowsFromGson.getWorkflowAt(index)
											.getInstructionAt(specialIndex + 1);
									String typeOfNextIntructionFromGson = nextIntructionFromGson.getInstructionType();

									// Generate next DrawLoop
									if (typeOfNextIntructionFromGson.equals(DrawInstructionType.DRAW_LOOP.getName()))
										controller.getWorkflows()[index].getInstructions().add(j + 1,
												((DrawLoopInstruction) controller.getWorkflows()[index]
														.getInstructionAt(j)).generate());

									else if (typeOfNextIntructionFromGson
											.equals(DrawInstructionType.DRAW_SYMMETRICAL_LOOP.getName()))
										controller.getWorkflows()[index].getInstructions().add(j + 1,
												((DrawSymmetricalLoopInstruction) controller.getWorkflows()[index]
														.getInstructionAt(j)).generate());
								}
								// In any case, we stop the current DrawLoop
								((DefaultGenerator) controller.getWorkflows()[index].getInstructionAt(j)).stop();
							}

							controller.getWorkflows()[index].getInstructionAt(j)
									.setRoi(intructionFromGson.getIndexRoiToEdit());

							specialIndex++;

						}
					}
				}
			}

			for (Workflow workflow : controller.getWorkflows())
				workflow.restart();
			controller.getWorkflows()[0].setCurrent(controller.getWorkflows()[0].getInstructionAt(0));

			controller.getVue().setNbInstructions(controller.allInputInstructions().size());

			String jsonInString = gson.toJson(workflowsFromGson);
			System.out.println(jsonInString);
			//
			//
			String workflowInString = gson.toJson(controller.getWorkflows());
			System.out.println(workflowInString);

			// System.out.println("\n\n\n");

		}

		return workflowsFromGson;

	}

	public StringBuilder initCSVVertical(String[] infoPatient) {
		// Realisation du string builder qui sera ecrit en CSV
		StringBuilder content = new StringBuilder();
		// Ajout titre colonne
		content.append("Patient's Name");
		content.append(',');
		content.append(infoPatient[0]);
		content.append('\n');

		content.append("Patient's ID");
		content.append(',');
		content.append(infoPatient[1]);
		content.append('\n');

		content.append("Study Date");
		content.append(',');
		content.append(infoPatient[2]);
		content.append('\n');

		return content;
	}

	/**
	 * Open a zip file and return all the .roi files inside as a List<Roi>, using
	 * {@link ZipInputStream}, {@link ByteArrayOutputStream} and {@link RoiDecoder}
	 * to process.<br/>
	 * This method needs, from the same .zip file, to find the associated .json
	 * file.<br/>
	 * This file will be needed to get the order of ROIs saved in the .zip file.
	 *
	 *
	 * @param path
	 *            The system-dependent file name.
	 * @return A list of ROIs, contained in the zip file.
	 * @throws UnloadRoiException
	 */
	public List<Roi> getRoiFromZip(String path, ControllerWorkflow controller)
			throws UnauthorizedRoiLoadException, UnloadRoiException {

		List<Roi> rois = new ArrayList<>();
		ZipInputStream in = null;
		ByteArrayOutputStream out = null;
		int nRois = 0;
		WorkflowsFromGson workflowsFromGson = null;

		try {
			in = new ZipInputStream(new FileInputStream(path));
			ZipEntry entry = in.getNextEntry();
			while (entry != null) {
				String name = entry.getName();
				if (name.endsWith(".json")) {

					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					int b = in.read();

					while (b >= 0) {
						baos.write(b);
						b = in.read();
					}
					SaveAndLoad saveAndLoad = new SaveAndLoad();
					workflowsFromGson = saveAndLoad.loadWorkflows(controller, baos.toString());
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
				} catch (IOException ignored) {
				}
		}

		Roi[] ROIsArray = new Roi[workflowsFromGson.getNbROIs()];

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

						int indexRoi = workflowsFromGson.getIndexRoiOfInstructionFromGson(name);
						if (indexRoi == -1)
							throw new UnauthorizedRoiLoadException(roi, name);

						ROIsArray[indexRoi] = roi;
						// rois.add(roi);
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
				} catch (IOException ignored) {
				}
			if (out != null)
				try {
					out.close();
				} catch (IOException ignored) {
				}
		}

		for (Roi roi : ROIsArray) {
			rois.add(roi);
			// System.out.println(roi);
			// System.out.println(roi.getName() + "\n");
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
	 * @throws UnloadRoiException
	 */
	public List<Roi> getRoiFromZipWithWindow(Component frame, ControllerWorkflow controller)
			throws UnauthorizedRoiLoadException, UnloadRoiException {

		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(new File(Prefs.get(PrefTabMain.PREF_SAVE_DIRECTORY, "./")));
		fc.setDialogTitle("Choose .zip containing rois");
		int returnVal = fc.showOpenDialog(frame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return this.getRoiFromZip(fc.getSelectedFile().getPath(), controller);
		}
		return null;
	}

	/**
	 * Import the ROIs list from a .json file
	 *
	 * @param frame
	 *            Frame in order to take reference for the JFileChooser
	 * @param model
	 *            Model to put ROIs in
	 * @param controller
	 *            Controller to update
	 * @return
	 * @throws UnauthorizedRoiLoadException
	 * @throws UnloadRoiException
	 */
	public boolean importRoiList(Frame frame, ModelScin model, ControllerWorkflow controller)
			throws UnauthorizedRoiLoadException, UnloadRoiException {

		List<Roi> rois = this.getRoiFromZipWithWindow(frame, controller);
		if (rois == null)
			return false;
		model.getRoiManager().removeAll();

		for (Roi roi : rois)
			model.getRoiManager().addRoi(roi);

		for (Workflow workflow : controller.getWorkflows())
			for (Instruction instruction : workflow.getInstructions())
				if (instruction.getRoiIndex() != -1)
					System.out.println(controller.getRoiManager().getRoi(instruction.getRoiIndex()).getName() + " : "
							+ controller.getRoiManager().getRoi(instruction.getRoiIndex()));
		System.out.println("\n\n");

		return true;

	}
}