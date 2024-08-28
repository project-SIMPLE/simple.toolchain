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

import java.util.List;

import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

/**
 * The Class WizardPageGeneralInformation.
 */
public class WizardPageGeneralInformation extends UnityWizardPage {

	/** The items D. */
	Composite parent;
	Group groupExperiment;
	Group groupDisplay;

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
	protected WizardPageGeneralInformation(final VRModelGenerator gen) {
		super("GeneralInformation", gen);
		setTitle("Definition of the VR experiment");
		setDescription("Enter information to build the VR experiment");
	}

	/**
	 * Update experiment.
	 */
	public void updateExperiment() {
		createDisplayGroup(parent);
		parent.requestLayout();
	}

	@Override
	public void createControlsIn(final Composite parent) {
		this.parent = parent;
		parent.setLayout(new GridLayout(1, false));

		groupExperiment = new Group(parent, SWT.NONE);
		groupExperiment.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		groupExperiment.setText("Experiment");
		groupExperiment.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
		label(groupExperiment, "Choose the experiment to extend:");

		Combo cXp = new Combo(groupExperiment, SWT.READ_ONLY);
		cXp.setLayoutData(GridDataFactory.fillDefaults().create());
		String[] items = generator.displays.keySet().toArray(new String[0]);
		cXp.setItems(items);
		if (items.length > 0) {
			String name = items[0];
			cXp.setText(name);
			generator.setExperimentName(name);
			updateExperiment();
		}

		cXp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				generator.setExperimentName(cXp.getText());
				updateExperiment();
			}
		});
		label(groupExperiment, "Minimum duration of a cycle in seconds (minimum_cycle_duration):");
		Text tc = text(groupExperiment, generator.getMinimumCycleDuration().toString());
		tc.addModifyListener(e -> {
			Double duration = Double.valueOf(tc.getText());
			if (duration != null) { generator.setMinimumCycleDuration(duration); }
		});
		createGeneralGroup(parent);
		createDisplayGroup(parent);
	}

	private void createDisplayGroup(final Composite parent) {
		List<String> itemsD = generator.displays.get(generator.getExperimentName());
		if (groupDisplay != null) { groupDisplay.dispose(); }
		if (itemsD == null || itemsD.isEmpty()) return;

		groupDisplay = new Group(parent, SWT.NONE);
		groupDisplay.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		groupDisplay.setText("Displays");

		label(groupDisplay, "Main display:");

		Combo cd = new Combo(groupDisplay, SWT.READ_ONLY);
		cd.setItems(itemsD.toArray(new String[itemsD.size()]));
		cd.setText(itemsD.get(0));
		cd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				generator.setMainDisplay(cd.getText());
			}
		});
		generator.setMainDisplay(cd.getText());
		label(groupDisplay, "Displays to hide:");
		Group groupDisplayH = new Group(groupDisplay, SWT.NONE);
		groupDisplayH.setLayout(new GridLayout(2, false));

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

		GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(false).generateLayout(groupDisplay);

	}

	void createGeneralGroup(final Composite parent) {
		Group grpAttributes = new Group(parent, SWT.NONE);
		grpAttributes.setText("General parameters");
		grpAttributes.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		grpAttributes.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
		{
			label(grpAttributes, "Perception radius of players");
			Text text_radius = text(grpAttributes, String.valueOf(generator.getDefaultPlayerAgentsPerceptionRadius()));
			text_radius.addModifyListener(e -> { generator.perceptionRadius = Double.valueOf(text_radius.getText()); });
		}
		{
			label(grpAttributes, "Min distance between agents to send to be considered");
			Text text_distance = text(grpAttributes, String.valueOf(generator.getDefaultPlayerAgentsMinDist()));
			text_distance.addModifyListener(e -> { generator.minDistance = Double.valueOf(text_distance.getText()); });
		}
	}

	@Override
	protected void propertyChanged(final String item) {}

	@Override
	protected String getPropertyNameHint() { return "default"; }

	@Override
	public void pageChanged(final PageChangedEvent event) {}

}
