/*******************************************************************************************************
 *
 * WizardPageGeneralInformation.java, in gaml.extension.unity, is part of the source code of the GAMA modeling and
 * simulation platform (v.1.9.3).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.extension.unity.commands.wizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import gama.core.kernel.experiment.IExperimentPlan;
import gama.core.kernel.model.IModel;

/**
 * The Class WizardPageGeneralInformation.
 */
public class WizardPageGeneralInformation extends WizardPage {

	/** The model. */
	IModel model;

	/** The container. */
	private Composite container;

	/** The generator. */
	VRModelGenerator generator;

	/** The w display. */
	WizardPageDisplay wDisplay;

	/**
	 * Instantiates a new wizard page general information.
	 *
	 * @param path
	 *            the path
	 * @param model
	 *            the model
	 * @param gen
	 *            the gen
	 */
	protected WizardPageGeneralInformation(final String path, final IModel model, final VRModelGenerator gen) {
		super("GeneralInformation");
		setTitle("Define the general information to define the VR experiment");
		setDescription("Please enter information about VR experiment");
		this.model = model;
		this.generator = gen;
		gen.setModelName(model.getName() + "_VR");
		gen.setModelPath(path);
	}

	@Override
	public void createControl(final Composite parent) {
		container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout(SWT.VERTICAL));
		/*
		 * Group groupConnection = new Group(container, SWT.NONE); groupConnection.setLayout(new GridLayout(2, false));
		 * groupConnection.setText("Information about the connection");
		 * 
		 * Label lp = new Label(groupConnection, SWT.LEFT); lp.setText("Port:" ); Text tp = new Text(groupConnection,
		 * SWT.BORDER); tp.setText(generator.getPort().toString()); tp.addModifyListener(new ModifyListener() {
		 * 
		 * @Override
		 * 
		 * public void modifyText(ModifyEvent e) { Integer port = Integer.decode(tp.getText()); if (port != null)
		 * generator.setPort(port); } });
		 */

		Group groupExperiment = new Group(container, SWT.NONE);
		groupExperiment.setLayout(new GridLayout(2, false));
		groupExperiment.setText("Information about the experiment");

		Label lc = new Label(groupExperiment, SWT.LEFT);
		lc.setText("Minimium duration of a cycle in s (minimum_cycle_duration):");
		Text tc = new Text(groupExperiment, SWT.BORDER);
		tc.setText(generator.getMinimumCycleDuration().toString());
		tc.addModifyListener(e -> {
			Double duration = Double.valueOf(tc.getText());
			if (duration != null) { generator.setMinimumCycleDuration(duration); }
		});
		Label lxp = new Label(groupExperiment, SWT.LEFT);
		lxp.setText("Main Experiment:");

		Combo cXp = new Combo(groupExperiment, SWT.READ_ONLY);
		List<String> items = new ArrayList<>();
		for (IExperimentPlan ep : model.getExperiments()) { items.add(ep.getName()); }

		cXp.setItems(items.toArray(new String[items.size()]));
		if (!items.isEmpty()) {
			cXp.setText(items.get(0));
			generator.setExperimentName(cXp.getText());
			wDisplay.updateExperiment();

		}

		cXp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				generator.setExperimentName(cXp.getText());
				wDisplay.updateExperiment();
			}
		});

		setControl(container);

	}

	/**
	 * Gets the w display.
	 *
	 * @return the w display
	 */
	public WizardPageDisplay getwDisplay() {
		return wDisplay;
	}

	/**
	 * Sets the w display.
	 *
	 * @param wDisplay
	 *            the new w display
	 */
	public void setwDisplay(final WizardPageDisplay wDisplay) {
		this.wDisplay = wDisplay;
	}

}
