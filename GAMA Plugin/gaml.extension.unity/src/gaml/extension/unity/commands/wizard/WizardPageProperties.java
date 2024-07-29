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

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import gama.core.kernel.model.IModel;

/**
 * The Class WizardPageDisplay.
 */
public class WizardPageProperties extends WizardPage {

	/** The model. */
	IModel model;

	/** The generator. */
	VRModelGenerator generator;

	org.eclipse.swt.widgets.List properiesList;
	
	Map<String, String> currentValues;
	
	Map<String, Map<String, String>> definedProperties;
	
	
	Map<String, Text> textAreas;

	Map<String, Button> booleanAreas;
	
	
	WizardPageSpeciesToSend wpStS;
	
	WizardPagePlayer playerPage;
	
	
	private Text text;
	private Text text_1;
	private Text text_2;
	private Text text_3;
	private Text text_4;
	private Text text_5;
	private Text text_6;
	private Text text_7;
	private Text text_8;
	private Text text_material;
	
	private Button btnHasACollider;
	private Button btnIsInteractable;
	private Button btnIsGrabable;
	private Button btnFollow;
	private Button btnHasAPrefab;
	/**
	 * Instantiates a new wizard page display.
	 *
	 * @param model
	 *            the model
	 * @param gen
	 *            the gen
	 */
	protected WizardPageProperties(final IModel model, final VRModelGenerator gen) {
		super("Properties");
		setTitle("Define the information about the Unity properties to link to geometries to send to Unity");
		setDescription("Please enter information about properties");
		this.model = model;
		this.generator = gen;
	}

	@Override
	public void createControl(final Composite parent) {
		currentValues = new HashMap<>();
		definedProperties = generator.getDefinedProperties();
		
		Composite container = new Composite(parent, SWT.NONE);

		setControl(container);
		
		Group grpUnityPropertiesDefined = new Group(container, SWT.NONE);
		grpUnityPropertiesDefined.setText("Unity properties");
		grpUnityPropertiesDefined.setBounds(10, 0, 800, 151);
		
		properiesList = new List(grpUnityPropertiesDefined, SWT.BORDER);
		properiesList.setBounds(72, 10, 407, 83);
		
		Button btnEdit = new Button(grpUnityPropertiesDefined, SWT.NONE);
		btnEdit.setBounds(83, 99, 96, 27);
		btnEdit.setText("Edit");
		
		btnEdit.addListener(SWT.Selection, new Listener() {
		      public void handleEvent(Event e) {
		    	  if ((e.type ==  SWT.Selection) && (properiesList.getSelectionIndex() >= 0)) {
		    		 String item = properiesList.getItem(properiesList.getSelectionIndex());
		    		 currentValues = definedProperties.get(item);
		    		for( String id : textAreas.keySet()) {
		    			 textAreas.get(id).setText(currentValues.get(id) == null ? "" : currentValues.get(id));
		    		}
		    		for(String id :  booleanAreas.keySet()) {
		    			booleanAreas.get(id).setSelection(currentValues.get(id) != null && currentValues.get(id).equals("true"));
				    		
		    		}
		    		updateList();
		          }
		      } 
		    });
		Button btnRemove = new Button(grpUnityPropertiesDefined, SWT.NONE);
		btnRemove.setBounds(185, 99, 96, 27);
		btnRemove.setText("Remove");
		
		btnRemove.addListener(SWT.Selection, new Listener() {
		      public void handleEvent(Event e) {
		    	  if ((e.type ==  SWT.Selection) && (properiesList.getSelectionIndex() >= 0)) {
		    		  properiesList.remove(properiesList.getSelectionIndex());
		    		  definedProperties.remove(properiesList.getItem(properiesList.getSelectionIndex()));
		    		  updateList();
		          }
		      } 
		    });
		
		
		Group grpDefinitionOfA = new Group(container, SWT.NONE);
		grpDefinitionOfA.setText("Definition of a Unity properties");
		grpDefinitionOfA.setBounds(10, 160, 800, 344);
		
		Group grpInteraction = new Group(grpDefinitionOfA, SWT.NONE);
		grpInteraction.setText("Interaction");
		grpInteraction.setBounds(10, 50, 500, 51);
		
		btnHasACollider = new Button(grpInteraction, SWT.CHECK);
		btnHasACollider.setBounds(30, 10, 93, 16);
		btnHasACollider.setText("has a collider");
		btnHasACollider.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				Button btn = (Button) event.getSource();
				currentValues.put("collider", btn.getSelection() +"");
				btnIsInteractable.setEnabled(btn.getSelection());
				btnIsGrabable.setEnabled(btn.getSelection() && btnIsInteractable.getSelection());
			}
		});
		
		btnIsInteractable = new Button(grpInteraction, SWT.CHECK);
		btnIsInteractable.setBounds(129, 10, 93, 16);
		btnIsInteractable.setText("is interactable");
		btnIsInteractable.setEnabled(false);
		btnIsInteractable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				Button btn = (Button) event.getSource();
				currentValues.put("interactable", btn.getSelection() +"");
				btnIsGrabable.setEnabled(btn.getSelection() && btnIsInteractable.getSelection());
			}
		});
		
		btnIsGrabable = new Button(grpInteraction, SWT.CHECK);
		btnIsGrabable.setBounds(245, 10, 93, 16);
		btnIsGrabable.setText("is grabable");
		btnIsGrabable.setEnabled(false);
		btnIsGrabable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				Button btn = (Button) event.getSource();
				currentValues.put("grabable", btn.getSelection() +"");
			}
		});
		
		btnFollow = new Button(grpInteraction, SWT.CHECK);
		btnFollow.setBounds(350, 10, 150, 16);
		btnFollow.setText("send back to GAMA");
		btnFollow.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				Button btn = (Button) event.getSource();
				currentValues.put("follow", btn.getSelection() +"");
			}
		});
		
		text = new Text(grpDefinitionOfA, SWT.BORDER);
		text.setBounds(64, 5, 127, 19);
		text.addModifyListener(e -> currentValues.put("name", text.getText()));
		
		Label lblName = new Label(grpDefinitionOfA, SWT.NONE);
		lblName.setBounds(10, 30, 59, 14);
		lblName.setText("Tag:");
		
		Group grpAspect = new Group(grpDefinitionOfA, SWT.NONE);
		grpAspect.setText("Aspect");
		grpAspect.setBounds(10, 108, 780, 181);
		
		btnHasAPrefab = new Button(grpAspect, SWT.CHECK);
		btnHasAPrefab.setBounds(12, 77, 93, 16);
		btnHasAPrefab.setText("has a prefab");
		btnHasAPrefab.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				Button btn = (Button) event.getSource();
				currentValues.put("has_prefab", btn.getSelection() +"");
				if (btn.getSelection()) {
					text_2.setEnabled(true);
					text_3.setEnabled(false);
					text_4.setEnabled(true);
					text_5.setEnabled(false);
					text_6.setEnabled(true);
					text_7.setEnabled(true);
					text_8.setEnabled(true);
					text_material.setEnabled(false);
					
				} else {
					text_2.setEnabled(false);
					text_3.setEnabled(true);
					text_4.setEnabled(false);
					text_5.setEnabled(true);
					text_6.setEnabled(false);
					text_7.setEnabled(false);
					text_8.setEnabled(false);
					text_material.setEnabled(true);
				}
			}
		});
		
		Group grpGeometry = new Group(grpAspect, SWT.NONE);
		grpGeometry.setText("Geometry");
		grpGeometry.setBounds(460, 11, 300, 107);
		
		text_3 = new Text(grpGeometry, SWT.BORDER);
		text_3.setBounds(170, 25, 109, 19);
		text_3.addModifyListener(e -> currentValues.put("color", text_3.getText()));
		
		text_material = new Text(grpGeometry, SWT.BORDER);
		text_material.setBounds(170, 50, 109, 19);
		text_material.addModifyListener(e -> currentValues.put("material", text_material.getText()));
		
		
		text_5 = new Text(grpGeometry, SWT.BORDER);
		text_5.setBounds(170, 0, 109, 19);
		text_5.addModifyListener(e -> currentValues.put("height", text_5.getText()));
		
		Label lblHeight = new Label(grpGeometry, SWT.NONE);
		lblHeight.setBounds(4, 3, 165, 14);
		lblHeight.setText("Height/depth of the geometry:");
		
		Label lblColor = new Label(grpGeometry, SWT.NONE);
		lblColor.setBounds(4, 28, 165, 14);
		lblColor.setText("Color of the geometry:");
		
		Label lblMaterial = new Label(grpGeometry, SWT.NONE);
		lblMaterial.setBounds(4, 53, 165, 14);
		lblMaterial.setText("Material for the geometry:");
		
		
		Group grpPrefav = new Group(grpAspect, SWT.NONE);
		grpPrefav.setText("Prefab");
		grpPrefav.setBounds(111, 11, 350, 145);
		
		text_2 = new Text(grpPrefav, SWT.BORDER);
		text_2.setBounds(220, 23, 109, 19);
		text_2.addModifyListener(e -> currentValues.put("size", text_2.getText()));
		
		Label lblPrefabPath = new Label(grpPrefav, SWT.NONE);
		lblPrefabPath.setBounds(4, 3, 200, 14);
		lblPrefabPath.setText("Path to the Prefab:");
		
		Label lblSize = new Label(grpPrefav, SWT.NONE);
		lblSize.setText("Scale of the Prefab:");
		lblSize.setBounds(4, 26, 200, 14);
		
		text_4 = new Text(grpPrefav, SWT.BORDER);
		text_4.setBounds(220, 0, 109, 19);
		text_4.addModifyListener(e -> currentValues.put("prefab", text_4.getText()));
		
		text_6 = new Text(grpPrefav, SWT.BORDER);
		text_6.setBounds(220, 48, 109, 19);
		text_6.addModifyListener(e -> currentValues.put("rotation_coeff", text_6.getText()));
		
		text_7 = new Text(grpPrefav, SWT.BORDER);
		text_7.setBounds(220, 98, 109, 19);
		text_7.addModifyListener(e -> currentValues.put("y-offset", text_7.getText()));
		
		text_8 = new Text(grpPrefav, SWT.BORDER);
		text_8.setBounds(220, 73, 109, 19);
		text_8.addModifyListener(e -> currentValues.put("rotation_offset", text_8.getText()));
		
		Label lblRotationCoeff = new Label(grpPrefav, SWT.NONE);
		lblRotationCoeff.setBounds(4, 51, 200, 14);
		lblRotationCoeff.setText("Rotation coefficient (Y-Unity axis):");
		
		Label lblRotationOffset = new Label(grpPrefav, SWT.NONE);
		lblRotationOffset.setBounds(4, 78, 200, 14);
		lblRotationOffset.setText("Rotation offset (Y-Unity axis):");
		
		Label lblOffset = new Label(grpPrefav, SWT.NONE);
		lblOffset.setBounds(4, 101, 200, 14);
		lblOffset.setText("Offset along the Y-Unity Axis:");
		
		Button btnAddupdate = new Button(grpDefinitionOfA, SWT.NONE);
		btnAddupdate.setBounds(95, 297, 96, 27);
		btnAddupdate.setText("Add/Update");
		btnAddupdate.addListener(SWT.Selection, new Listener() {
		      public void handleEvent(Event e) {
		    	  if ((e.type ==  SWT.Selection)) {
		    		  String name = currentValues.get("name");
		    		  if (!name.isBlank()) {
		    			 for (int i = 0; i < properiesList.getItemCount(); i ++) {
		    				String it = properiesList.getItem(i);
		    				if (name.equals(it)) {
		    					properiesList.remove(i);
		    					break;
		    				}
		    			 }
		    			 properiesList.add(name);
		    			 definedProperties.put(name, new HashMap<String, String>(currentValues));
		    			 initValue();
		    			 updateList() ;
		    		 }
		    		  
		          }
		      } 
		    });
		
		Button btnReset = new Button(grpDefinitionOfA, SWT.NONE);
		btnReset.setBounds(197, 297, 96, 27);
		btnReset.setText("Reset");
		btnReset.addListener(SWT.Selection, new Listener() {
		      public void handleEvent(Event e) {
		    	  if ((e.type ==  SWT.Selection)) {
		    		  initValue();
		          }
		      } 
		    });
		
		Label lblName_1 = new Label(grpDefinitionOfA, SWT.NONE);
		lblName_1.setText("Name:");
		lblName_1.setBounds(10, 8, 59, 14);
		
		text_1 = new Text(grpDefinitionOfA, SWT.BORDER);
		text_1.setBounds(64, 25, 127, 19);
		text_1.addModifyListener(e -> currentValues.put("tag", text_1.getText()));
		
		
		textAreas = new Hashtable<String, Text>();
		textAreas.put("name", text);
		textAreas.put("tag", text_1);
		textAreas.put("size", text_2);
		textAreas.put("prefab", text_4);
		textAreas.put("rotation_coeff", text_6);
		textAreas.put("y-offset", text_7);
		textAreas.put("rotation_offset", text_8);
		textAreas.put("color", text_3);
		textAreas.put("material", text_material);
		textAreas.put("height", text_5);
		
		booleanAreas = new Hashtable<>();
		booleanAreas.put("has_prefab", btnHasAPrefab);
		booleanAreas.put("grabable", btnIsGrabable);
		booleanAreas.put("interactable", btnIsInteractable);
		booleanAreas.put("collider", btnHasACollider);
		booleanAreas.put("follow", btnFollow);
		
		initValue();
	
	}

	
	void initValue(){
		currentValues.clear();
		
		text_2.setEnabled(false);
		text_3.setEnabled(true);
		text_material.setEnabled(true);
		text_4.setEnabled(false);
		text_5.setEnabled(true);
		text_6.setEnabled(false);
		text_7.setEnabled(false);
		text_8.setEnabled(false);
		text.setText("");
		text_1.setText("");
		text_2.setText("1.0");
		text_3.setText("gray");
		text_material.setText("");
		text_4.setText("Prefabs/Visual Prefabs/City/Vehicles/Car");
		text_5.setText("1.0");
		text_6.setText("1.0");
		text_7.setText("0.0");
		text_8.setText("0.0");
		
		btnHasACollider.setSelection(false);
		btnIsInteractable.setSelection(false);
		btnIsGrabable.setSelection(false);
		btnFollow.setSelection(false);
		btnHasAPrefab.setSelection(false);
		
		for( String id : textAreas.keySet()) {
			currentValues.put(id,textAreas.get(id).getText());
		}
		for(String id :  booleanAreas.keySet()) {
			currentValues.put(id,booleanAreas.get(id).getSelection() + "");
	    		
		}
		
		
	}
	
	
	void updateList() {
		wpStS.setItemsP(definedProperties);
		playerPage.updateUnityProperties();
		
	}
	public WizardPageSpeciesToSend getWpStS() {
		return wpStS;
	}

	public void setWpStS(WizardPageSpeciesToSend wpStS) {
		this.wpStS = wpStS;
	}

	public void setPlayerPage(WizardPagePlayer playerPage) {
		this.playerPage = playerPage;
		
	}

	

	

	

}
