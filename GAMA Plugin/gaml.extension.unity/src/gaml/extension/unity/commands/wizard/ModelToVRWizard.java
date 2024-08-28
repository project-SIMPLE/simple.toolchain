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

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import gama.core.kernel.model.IModel;

/**
 * The Class ModelToVRWizard.
 */
public class ModelToVRWizard extends Wizard {

	/** The generator. */
	VRModelGenerator generator;

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
	public ModelToVRWizard(final IModel model) {
		generator = new VRModelGenerator(model);
		setWindowTitle("VR Experiment generation");
	}

	@Override
	public void addPages() {
		WizardPageGeneralInformation wizI = new WizardPageGeneralInformation(generator);
		addPage(wizI);
		WizardPageSpecies speciesPage = new WizardPageSpecies(generator);
		addPage(speciesPage);
		finalPage = new WizardPagePlayer(generator);

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
		generator.buildAndSaveVRModel();
		return true;
	}

}
