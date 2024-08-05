/*******************************************************************************************************
 *
 * ModelToVRWizard.java, in gaml.extension.unity, is part of the source code of the GAMA modeling and simulation
 * platform (v.1.9.3).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.extension.unity.commands.wizard;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import gama.core.kernel.model.IModel;

/**
 * The Class ModelToVRWizard.
 */
public class ModelToVRWizard extends Wizard {

	/** The model. */
	IModel model;

	/** The generator. */
	VRModelGenerator generator;

	/** The file. */
	File file;

	/** The path. */
	String path;

	/** The final page. */
	IWizardPage finalPage;

	/**
	 * Instantiates a new model to VR wizard.
	 *
	 * @param path
	 *            the path
	 * @param model
	 *            the model
	 * @param file
	 *            the file
	 */
	public ModelToVRWizard(final String path, final IModel model, final File file) {
		generator = new VRModelGenerator();
		setWindowTitle("VR Experiment generation");
		// setNeedsProgressMonitor(true);
		this.model = model;
		this.file = file;
		this.path = path;

	}

	@Override
	public void addPages() {

		WizardPageGeneralInformation wizI = new WizardPageGeneralInformation(path, model, generator);
		addPage(wizI);
		WizardPageDisplay wizD = new WizardPageDisplay(model, generator);
		wizI.setwDisplay(wizD);
		addPage(wizD);
		WizardPageProperties wpp = new WizardPageProperties(model, generator);
		addPage(wpp);
		WizardPageSpeciesToSend wpSTS = new WizardPageSpeciesToSend(model, generator);
		wpp.setWpStS(wpSTS);
		addPage(wpSTS);
		
		//addPage(new WizardPageAgentsToSend(model, generator));
		//addPage(new WizardPageGeometries(model, generator));
		finalPage = new WizardPagePlayer(model, generator);
		wpp.setPlayerPage((WizardPagePlayer) finalPage);
		
		addPage(finalPage);
		getShell().setSize(820, 650);
	}

	@Override
	public boolean canFinish() {
		if (getContainer().getCurrentPage() == finalPage) return true;
		return false;
	}

	@Override
	public boolean performFinish() {
		String modelVRStr = generator.BuildVRModel();

		try {
			FileWriter fw = new FileWriter(file);

			fw.write(modelVRStr);
			fw.close();
			/*
			 * IFileStore fileStore = EFS.getLocalFileSystem().getStore(file.toURI()); IWorkbenchPage page =
			 * PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(); try { IDE.openEditorOnFileStore(
			 * page, fileStore ); } catch ( PartInitException e ) { e.printStackTrace();
			 * System.out.println("An Error occured while loading the file."); }
			 */

		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}

}
