package gaml.extension.unity.commands.wizard;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import gama.core.util.GamaColor;
import gama.dev.DEBUG;
import gaml.extension.unity.commands.wizard.VRModelGenerator.UnityProperties;

class UnityPropertiesEditor {

	/**
	 *
	 */
	private final UnityWizardPage wizardPage;

	/**
	 * @param wp
	 */
	UnityPropertiesEditor(final UnityWizardPage wp) {
		wizardPage = wp;
		wizardPage.generator.addPropertiesListener(this);
	}

	ListViewer propertiesList;
	Group propertiesComposite;
	private Text text_tag;
	private Text text_scale;
	private Text text_path;
	private Text text_height;
	private Text text_buffer;
	private Text text_rotationCoeff;
	private Text text_offset;
	private Text text_rotationOffset;
	private Text text_material;
	private ColorSelector colorSelector;

	private Button btnHasACollider;
	private Button btnIsInteractable;
	private Button btnIsGrabable;
	private Button btnFollow;
	private Button bPrefab, bGeom;

	String currentProperties;

	void setVisible(final boolean visible) {
		propertiesComposite.setVisible(visible);
	}

	private void createTagInput(final Composite grpDefinition) {
		UnityWizardPage.label(grpDefinition, "Tag:");
		text_tag = UnityWizardPage.text(grpDefinition, "");
		text_tag.addModifyListener(e -> getUnityProperties().tag = text_tag.getText());
	}

	private void createInteractionGroup(final Composite grpDefinition) {
		Group grpInteraction = new Group(grpDefinition, SWT.NONE);
		grpInteraction.setLayoutData(GridDataFactory.fillDefaults().span(4, 1).grab(true, false).create());

		btnHasACollider = UnityWizardPage.check(grpInteraction, "has a collider");
		btnHasACollider.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				Button btn = (Button) event.getSource();
				getUnityProperties().collider = btn.getSelection();
				btnIsInteractable.setEnabled(btn.getSelection());
				btnIsGrabable.setEnabled(btn.getSelection() && btnIsInteractable.getSelection());
			}
		});

		btnFollow = UnityWizardPage.check(grpInteraction, "synchronize with GAMA");
		btnFollow.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				Button btn = (Button) event.getSource();
				getUnityProperties().follow = btn.getSelection();
			}
		});

		btnIsInteractable = UnityWizardPage.check(grpInteraction, "is interactable");
		btnIsInteractable.setEnabled(false);
		btnIsInteractable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				Button btn = (Button) event.getSource();
				getUnityProperties().interactable = btn.getSelection();
				btnIsGrabable.setEnabled(btn.getSelection() && btnIsInteractable.getSelection());
			}
		});

		btnIsGrabable = UnityWizardPage.check(grpInteraction, "is grabable");
		btnIsGrabable.setEnabled(false);
		btnIsGrabable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				Button btn = (Button) event.getSource();
				getUnityProperties().grabable = btn.getSelection();
			}
		});

		GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(false).generateLayout(grpInteraction);
	}

	public void createPropertiesComposite(final Composite globalContainer) {
		propertiesComposite = new Group(globalContainer, SWT.NONE);
		propertiesComposite.setLayout(new GridLayout(2, false));
		propertiesComposite.setLayoutData(
				GridDataFactory.fillDefaults().span(1, 1).grab(true, true).align(SWT.FILL, SWT.BOTTOM).create());
		propertiesComposite.setText("Aspect in Unity");
		propertiesComposite.setLayout(GridLayoutFactory.swtDefaults().equalWidth(false).numColumns(2).create());
		propertiesComposite.addDisposeListener(e -> {
			wizardPage.generator.removePropertiesListener(UnityPropertiesEditor.this);
		});
		Composite grpUnityProperties = new Composite(propertiesComposite, SWT.NONE);
		grpUnityProperties
				.setLayout(GridLayoutFactory.swtDefaults().equalWidth(true).spacing(0, 0).numColumns(2).create());
		grpUnityProperties.setLayoutData(GridDataFactory.fillDefaults().create());

		propertiesList = new ListViewer(grpUnityProperties, SWT.BORDER | SWT.SINGLE);
		propertiesList.setContentProvider(ArrayContentProvider.getInstance());
		propertiesList.setInput(wizardPage.generator.properties.keySet());
		propertiesList.getList().setLayoutData(
				GridDataFactory.fillDefaults().grab(true, true).span(2, 1).hint(100, SWT.DEFAULT).create());
		propertiesList.addPostSelectionChangedListener(e -> {
			Object o = propertiesList.getStructuredSelection().getFirstElement();
			if (o == null) { propertiesList.getList().select(0); }
			String item = o == null ? "default" : o.toString();
			setSelection(item, false);
			wizardPage.propertyChanged(item);
		});

		Button btnAdd = new Button(grpUnityProperties, SWT.FLAT);
		btnAdd.setLayoutData(GridDataFactory.fillDefaults().create());
		btnAdd.setText("+");
		Button btnRemove = new Button(grpUnityProperties, SWT.FLAT);
		btnRemove.setLayoutData(GridDataFactory.fillDefaults().create());
		btnRemove.setText("-");

		btnRemove.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			Object o = propertiesList.getStructuredSelection().getFirstElement();
			if ("default".equals(o)) return;
			if (o != null) {
				propertiesList.setSelection(new StructuredSelection("default"));
				propertiesList.remove(o);
				wizardPage.generator.properties.remove(o);
				wizardPage.generator.notifyPropertiesChanged();
			}
		}));

		btnAdd.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			String original = wizardPage.getPropertyNameHint();
			int counter = 0;
			String def = original;
			List<String> existing = Arrays.asList(propertiesList.getList().getItems());
			while (existing.contains(def)) { def = original + ++counter; }

			InputDialog d = new InputDialog(null, "Name of the Unity properties",
					"Enter a name for the set of properties used in Unity", def,
					newText -> existing.contains(newText) ? "This name is already defined" : null);

			if (d.open() != Window.OK) return;
			def = d.getValue();
			propertiesList.add(def);
			UnityProperties prop = wizardPage.generator.createUnityProperties(def);
			prop.tag = "";
			wizardPage.generator.notifyPropertiesChanged();
			propertiesList.setSelection(new StructuredSelection(def), true);

		}));

		Composite grpDefinition = new Composite(propertiesComposite, SWT.NONE);
		grpDefinition.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());

		UnityWizardPage.label(grpDefinition, "Uses");
		GridData layoutDataPrefab = GridDataFactory.fillDefaults().span(4, 1).grab(true, false).create();
		GridData layoutDataGeom = GridDataFactory.copyData(layoutDataPrefab);
		Composite radioGroup = new Composite(grpDefinition, SWT.NONE);
		radioGroup.setLayout(RowLayoutFactory.fillDefaults().create());
		radioGroup.setLayoutData(GridDataFactory.fillDefaults().span(3, 1).create());
		bPrefab = new Button(radioGroup, SWT.RADIO);
		bPrefab.setText("A prefab defined in Unity");
		bGeom = new Button(radioGroup, SWT.RADIO);
		bGeom.setText("The geometry of the agent in GAMA");
		bGeom.setSelection(true);

		Group grpGeom = new Group(grpDefinition, SWT.NONE);
		Group grpPrefab = new Group(grpDefinition, SWT.NONE);

		SelectionListener radio = SelectionListener.widgetSelectedAdapter(e -> {
			boolean isPrefab = bPrefab.getSelection();
			getUnityProperties().has_prefab = isPrefab;
			layoutDataPrefab.exclude = !isPrefab;
			grpPrefab.setVisible(isPrefab);
			layoutDataGeom.exclude = isPrefab;
			grpGeom.setVisible(!isPrefab);
			grpDefinition.requestLayout();
		});
		bPrefab.addSelectionListener(radio);
		bGeom.addSelectionListener(radio);

		grpGeom.setLayoutData(layoutDataGeom);

		UnityWizardPage.label(grpGeom, "Color:");
		colorSelector = new ColorSelector(grpGeom);
		GamaColor c = wizardPage.generator.getDefaultSpeciesColor();
		RGB def = new RGB(c.red(), c.green(), c.blue());
		colorSelector.setColorValue(def);
		colorSelector.addListener(event -> {
			RGB col = colorSelector.getColorValue();
			getUnityProperties().color = GamaColor.get(col.red, col.green, col.blue);
		});

		UnityWizardPage.label(grpGeom, "Material:");
		text_material = new Text(grpGeom, SWT.BORDER);
		text_material.addModifyListener(e -> getUnityProperties().material = text_material.getText());

		UnityWizardPage.label(grpGeom, "Height/depth:");
		text_height = new Text(grpGeom, SWT.BORDER);
		text_height.addModifyListener(e -> getUnityProperties().height = Double.valueOf(text_height.getText()));

		UnityWizardPage.label(grpGeom, "Buffer:");
		text_buffer = new Text(grpGeom, SWT.BORDER);
		text_buffer.addModifyListener(e -> getUnityProperties().buffer = Double.valueOf(text_buffer.getText()));

		GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(false).generateLayout(grpGeom);

		grpPrefab.setLayoutData(layoutDataPrefab);
		layoutDataPrefab.exclude = true;
		grpPrefab.setVisible(false);

		UnityWizardPage.label(grpPrefab, "Scale of the Prefab:");
		text_scale = new Text(grpPrefab, SWT.BORDER);
		text_scale.addModifyListener(e -> getUnityProperties().size = Double.valueOf(text_scale.getText()));

		UnityWizardPage.label(grpPrefab, "Path:");
		text_path = new Text(grpPrefab, SWT.BORDER);
		text_path.addModifyListener(e -> getUnityProperties().path = text_path.getText());

		UnityWizardPage.label(grpPrefab, "Rotation coefficient (Unity Y-axis):");
		text_rotationCoeff = new Text(grpPrefab, SWT.BORDER);
		text_rotationCoeff.addModifyListener(
				e -> getUnityProperties().rotationCoeff = Double.valueOf(text_rotationCoeff.getText()));

		UnityWizardPage.label(grpPrefab, "Rotation offset (Unity Y-axis):");
		text_rotationOffset = new Text(grpPrefab, SWT.BORDER);
		text_rotationOffset.addModifyListener(
				e -> getUnityProperties().rotationOffset = Double.valueOf(text_rotationOffset.getText()));

		UnityWizardPage.label(grpPrefab, "Offset along the Unity Y-Axis:");
		text_offset = new Text(grpPrefab, SWT.BORDER);
		text_offset.addModifyListener(e -> getUnityProperties().offset = Double.valueOf(text_offset.getText()));

		GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(false).generateLayout(grpPrefab);

		createInteractionGroup(grpDefinition);
		createTagInput(grpDefinition);
		GridLayoutFactory.swtDefaults().numColumns(4).equalWidth(false).generateLayout(grpDefinition);

		propertiesList.setSelection(new StructuredSelection("default"));
	}

	private UnityProperties getUnityProperties() { return wizardPage.generator.getUnityProperties(currentProperties); }

	private void updateControlsWithProperty(final String s) {
		UnityProperties prop = wizardPage.generator.getUnityProperties(s);
		text_tag.setText(prop.tag);
		text_scale.setText(String.valueOf(prop.size));
		colorSelector.setColorValue(new RGB(prop.color.getRed(), prop.color.getGreen(), prop.color.getBlue()));
		text_material.setText(prop.material);
		text_buffer.setText(String.valueOf(prop.buffer));
		text_path.setText(prop.path);
		text_height.setText(String.valueOf(prop.height));
		text_rotationCoeff.setText(String.valueOf(prop.rotationCoeff));
		text_offset.setText(String.valueOf(prop.offset));
		text_rotationOffset.setText(String.valueOf(prop.rotationOffset));

		btnHasACollider.setSelection(prop.collider);
		btnIsInteractable.setSelection(prop.interactable);
		btnIsInteractable.setEnabled(prop.collider);
		btnIsGrabable.setSelection(prop.grabable);
		btnIsGrabable.setEnabled(prop.collider && prop.interactable);
		btnFollow.setSelection(prop.follow);
		bPrefab.setSelection(prop.has_prefab);
		bGeom.setSelection(!bPrefab.getSelection());

		wizardPage.notify(btnHasACollider);
		wizardPage.notify(btnIsInteractable);
		wizardPage.notify(btnIsGrabable);
		wizardPage.notify(btnFollow);
		wizardPage.notify(bPrefab);
		wizardPage.notify(bGeom);
	}

	public void setSelection(final String property, final boolean selectInUI) {
		currentProperties = property;
		if (currentProperties == null) { currentProperties = "default"; }
		DEBUG.OUT("Setting " + currentProperties + " as the new properties");
		if (selectInUI) { propertiesList.setSelection(new StructuredSelection(currentProperties)); }
		updateControlsWithProperty(currentProperties);
	}

	public void propertiesChanged() {
		propertiesList.refresh();
		setSelection(currentProperties, true);
	}
}