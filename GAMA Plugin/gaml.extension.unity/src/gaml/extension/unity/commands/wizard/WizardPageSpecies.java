package gaml.extension.unity.commands.wizard;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import gama.dev.DEBUG;
import gaml.extension.unity.commands.wizard.VRModelGenerator.Species;

public class WizardPageSpecies extends UnityWizardPage {

	static {
		DEBUG.ON();
	}

	UnityPropertiesEditor propertiesEditor = new UnityPropertiesEditor(this);

	CheckboxTableViewer speciesList;

	// private Button bExport;
	private Composite updateComposite;

	String currentSpecies;
	String currentProperties;
	private Text when;
	Button bUpdate = null;

	public WizardPageSpecies(final VRModelGenerator gen) {
		super("Species", gen);
		setTitle("Export species");
		setDescription("Specify the species to export to Unity, and their aspect in Unity");
	}

	@Override
	public void createControlsIn(final Composite parent) {
		Composite globalContainer = new Composite(parent, SWT.NONE);
		globalContainer.setLayout(new GridLayout(2, false));
		globalContainer.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		createSpeciesList(globalContainer);
		// createSpeciesAttributes(globalContainer);
		createUpdateGroup(globalContainer);
		propertiesEditor.createPropertiesComposite(globalContainer);
		showProperties(false);
	}

	private void createSpeciesList(final Composite globalContainer) {
		Group grpSpecies = new Group(globalContainer, SWT.NONE);
		grpSpecies.setText("Species");
		grpSpecies.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).create());
		grpSpecies.setLayoutData(
				GridDataFactory.fillDefaults().grab(false, true).span(1, 3).hint(200, SWT.DEFAULT).create());
		speciesList = CheckboxTableViewer.newCheckList(grpSpecies, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		speciesList.getTable().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		speciesList.setContentProvider(ArrayContentProvider.getInstance());
		speciesList.setLabelProvider(new LabelProvider() {

			@Override
			public String getText(final Object element) {
				String s = element.toString();
				StringBuilder name = new StringBuilder().append(element.toString());
				Species species = generator.getSpecies(s);
				String pf = "";
				if (species.keep) {
					pf = "exported";
					if (!species.dynamic) {
						pf = pf + ", static";
					} else {
						pf = pf + ", dynamic";
					}
				} else {
					pf = "not exported";
				}

				if (!pf.isBlank()) { name.append(" (").append(pf).append(")"); }
				return name.toString();
			}
		});
		speciesList.setInput(generator.species.keySet());

		speciesList.addSelectionChangedListener(event -> {
			// DEBUG.OUT("Selected: " + event.getStructuredSelection().getFirstElement());
			Object selected = event.getStructuredSelection().getFirstElement();
			currentSpecies = selected == null ? null : selected.toString();
			updateControlsWithCurrentSpecies();
		});

		speciesList.addCheckStateListener(event -> {
			String species = String.valueOf(event.getElement());
			Species s = generator.getSpecies(species);
			if (s != null) { s.keep = event.getChecked(); }
			speciesList.refresh();
			speciesList.setSelection(new StructuredSelection(species));
			currentSpecies = species;
			updateControlsWithCurrentSpecies();
		});

	}

	private void createUpdateGroup(final Composite globalContainer) {
		updateComposite = new Composite(globalContainer, SWT.NONE);
		updateComposite.setLayoutData(GridDataFactory.fillDefaults().create());
		updateComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).create());
		Label spacer = new Label(updateComposite, SWT.NONE);
		spacer.setText(" ");
		spacer.setLayoutData(GridDataFactory.fillDefaults().span(3, 1).create());
		bUpdate = check(updateComposite, "Update every");
		bUpdate.addSelectionListener(widgetSelectedAdapter(e -> {
			when.setEnabled(bUpdate.getSelection());
			when.setText(generator.getSpecies(currentSpecies).when);
			generator.getSpecies(currentSpecies).dynamic = bUpdate.getSelection();
			speciesList.refresh();
		}));
		when = text(updateComposite, "");
		when.addModifyListener(e -> generator.getSpecies(currentSpecies).when = when.getText());
		label(updateComposite, " simulation cycles");
	}

	void showProperties(final boolean b) {
		updateComposite.setVisible(b);
		propertiesEditor.setVisible(b);
		getControl().requestLayout();
	}

	private void updateControlsWithCurrentSpecies() {
		if (currentSpecies != null && generator.getSpecies(currentSpecies).keep) {
			showProperties(true);
			Species species = generator.getSpecies(currentSpecies);
			bUpdate.setSelection(species.dynamic);
			notify(bUpdate);
			propertiesEditor.setSelection(species.property, true);
			when.setText(species.when);
		} else {
			showProperties(false);
		}
	}

	@Override
	protected void propertyChanged(final String item) {
		if (currentSpecies != null) { generator.getSpecies(currentSpecies).property = item; }
	}

	@Override
	protected String getPropertyNameHint() { return currentSpecies; }

	@Override
	public void pageChanged(final PageChangedEvent event) {
		propertiesEditor.propertiesChanged();
	}

}
