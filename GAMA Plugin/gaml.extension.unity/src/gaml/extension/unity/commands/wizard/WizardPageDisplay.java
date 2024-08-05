/*******************************************************************************************************
 *
 * WizardPageDisplay.java, in gaml.extension.unity, is part of the source code of the GAMA modeling and simulation
 * platform (v.1.9.3).
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import gama.core.kernel.model.IModel;
import gama.core.outputs.IOutput;

/**
 * The Class WizardPageDisplay.
 */
public class WizardPageDisplay extends WizardPage {

	/** The model. */
	IModel model;

	/** The container. */
	private Composite container;

	/** The generator. */
	VRModelGenerator generator;

	/** The items D. */
	List<String> itemsD;

	/**
	 * Instantiates a new wizard page display.
	 *
	 * @param model
	 *            the model
	 * @param gen
	 *            the gen
	 */
	protected WizardPageDisplay(final IModel model, final VRModelGenerator gen) {
		super("Display");
		setTitle("Define the information about the display");
		setDescription("Please enter information about displays");
		this.model = model;
		this.generator = gen;
	}

	/**
	 * Update experiment.
	 */
	public void updateExperiment() {
		itemsD = new ArrayList<>();
		for (IOutput d : model.getExperiment(generator.getExperimentName()).getOriginalSimulationOutputs()) {
			itemsD.add(d.getOriginalName());
		}

	}

	/** The cd. */
	Combo cd;

	/** The group display H. */
	Group groupDisplayH;

	@Override
	public void createControl(final Composite parent) {
		container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout(SWT.VERTICAL));
		Group groupDisplay = new Group(container, SWT.NONE);
		groupDisplay.setLayout(new GridLayout(2, false));
		groupDisplay.setText("Information about the display");

		Label ld = new Label(groupDisplay, SWT.LEFT);
		ld.setText("Main Display:");

		cd = new Combo(groupDisplay, SWT.READ_ONLY);

		if (itemsD != null && itemsD.size() > 0) {

			cd.setItems(itemsD.toArray(new String[itemsD.size()]));
			if (!itemsD.isEmpty()) {
				cd.setText(itemsD.get(0));
				generator.setMainDisplay(cd.getText());
			}
			groupDisplayH = new Group(container, SWT.NONE);
			groupDisplayH.setLayout(new GridLayout(2, false));
			groupDisplayH.setText("Displays to hide");

			for (String sp : itemsD) {
				Button bt = new Button(groupDisplayH, SWT.CHECK);
				bt.addSelectionListener(new SelectionAdapter() {

					@Override
					public void widgetSelected(final SelectionEvent event) {
						Button btn = (Button) event.getSource();
						if (btn.getSelection()) {
							generator.getDisplaysToHide().add(btn.getText());
						} else {
							generator.getDisplaysToHide().remove(btn.getText());
						}

					}
				});
				bt.setSelection(true);
				bt.setText(sp);
				generator.getDisplaysToHide().add(bt.getText());
				bt.pack();
			}
			cd.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					generator.setMainDisplay(cd.getText());
				}
			});
		}
		/*
		 * Group groupDisplayH = new Group(container, SWT.NONE); groupDisplayH.setLayout(new GridLayout(2, false));
		 * groupDisplayH.setText("Displays to hide");
		 *
		 * for (IExperimentPlan ep : model.getExperiments()) { for (IDisplayOutput d
		 * :ep.getExperimentOutputs().getDisplayOutputs()){ String sp = d.getName(); Button bt = new
		 * Button(groupDisplayH, SWT.CHECK); bt.addSelectionListener(new SelectionAdapter() {
		 *
		 * @Override public void widgetSelected(SelectionEvent event) { Button btn = (Button) event.getSource();
		 * if(btn.getSelection()) { generator.getDisplaysToHide().add(btn.getText()); } else {
		 *
		 * generator.getDisplaysToHide().remove(btn.getText()); }
		 *
		 * } }); bt.setText(sp); bt.pack(); } }
		 */

		setControl(container);

	}

}
