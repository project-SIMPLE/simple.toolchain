/*******************************************************************************************************
 *
 * GenerateVrModelHandler.java, in gaml.extension.unity, is part of the source code of the GAMA modeling and simulation
 * platform (v.1.9.3).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.extension.unity.commands;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gama.core.kernel.model.IModel;
import gama.ui.shared.utils.WorkbenchHelper;
import gaml.compiler.gaml.validation.GamlModelBuilder;
import gaml.compiler.ui.editor.GamlEditor;
import gaml.extension.unity.commands.wizard.ModelToVRWizard;

/**
 * The Class GenerateVrModelHandler.
 */
public class GenerateVrModelHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final GamlEditor editor =
				(GamlEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

		final IModel model = editor.getDocument()
				.readOnly(state -> GamlModelBuilder.getDefaultInstance().compile(state.getURI(), null));
		if (model == null) return null;
		File m = new File(model.getFilePath());
		final File file = new File(model.getFilePath().replace(".gaml", "-VR.gaml"));
		String path = m.getName();
		createVRModel(path, model, file);
		return null;
	}

	/**
	 * Creates the VR model.
	 *
	 * @param path
	 *            the path
	 * @param model
	 *            the model
	 * @param file
	 *            the file
	 */
	protected void createVRModel(final String path, final IModel model, final File file) {
		Shell shell = WorkbenchHelper.getShell();
		ModelToVRWizard wizard = new ModelToVRWizard(path, model, file);
		WizardDialog dialog = new WizardDialog(shell, wizard);

		dialog.open();
	}
}