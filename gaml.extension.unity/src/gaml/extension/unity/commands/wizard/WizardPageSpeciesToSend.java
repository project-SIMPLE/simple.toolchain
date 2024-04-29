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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import gama.core.kernel.model.IModel;

/**
 * The Class WizardPageDisplay.
 */
public class WizardPageSpeciesToSend extends WizardPage {

	/** The model. */
	IModel model;

	List<String> itemsP;
	List<Combo> combos;
	List<Button> speciesBtn;
	/** The generator. */
	VRModelGenerator generator;
	Map<String, Map<String, String>> speciesToSend;
	Map<String, Boolean> geometryB;
	

	/**
	 * Instantiates a new wizard page display.
	 *
	 * @param model the model
	 * @param gen   the gen
	 */
	protected WizardPageSpeciesToSend(final IModel model, final VRModelGenerator gen) {
		super("Properties");
		setTitle("Define the information about the species of agents to send to Unity");
		setDescription("Please enter information about the species to send");
		this.model = model;
		this.generator = gen;
	}

	@Override
	public void createControl(final Composite parent) {
		// container = new Composite(parent, SWT.NONE);
		// container.setLayout(new FillLayout(SWT.VERTICAL));
		speciesToSend = generator.getSpeciesToSend();
		ScrolledComposite scroll = new ScrolledComposite(parent, SWT.V_SCROLL);
		scroll.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		scroll.setAlwaysShowScrollBars(false);
		scroll.setExpandVertical(true);
		scroll.setExpandHorizontal(true);

		scroll.setMinHeight(600);
		scroll.setLayout(new GridLayout(1, false));
	
		Composite group = new Composite(scroll, SWT.NONE);
		scroll.setContent(group);
		group.setLayout(new GridLayout(2, false));
		// group.setText("Species of agents to send to Unity as static geometries");
		Label lma = new Label(group, SWT.LEFT);
		lma.setText("Species of agents to send to Unity");
		Label lmae = new Label(group, SWT.LEFT);
		lmae.setText("");
		combos = new ArrayList<>();
		speciesBtn = new ArrayList<>();
		for (String sp : model.getAllSpecies().keySet()) {

			if (sp.equals(model.getName())) {
				continue;
			}
			Button bt = new Button(group, SWT.CHECK);
			bt.setText(sp);
			bt.setEnabled(false);
			speciesBtn.add(bt);
			Map<String, String> data =  new Hashtable<>();
			speciesToSend.put(sp,data);
			data.put("keep", "false");
			
			bt.pack();

			Group groupSp = new Group(group, SWT.NONE);

			groupSp.setLayout(new GridLayout(2, false));
			Button staticBtn = addBooleanProperty(groupSp, "static", "Static?", data);
			staticBtn.setEnabled(false);
			data.put("static", "" + staticBtn.getSelection());
			
			Label ld = new Label(groupSp, SWT.LEFT);
			ld.setText("Unity Properties:");

			Combo cd = new Combo(groupSp, SWT.READ_ONLY);
			combos.add(cd);

			
			data.put("properties", cd.getText());
			
			Text buffer = addStringProperty(groupSp, "buffer", "Buffer to apply to the geometry", data);
			buffer.setEnabled(false);
			buffer.setText("0.0");
			data.put("buffer", buffer.getText());
			
			

			final Text when = addStringProperty(groupSp, "when", "When updating the agent list:", data);
			when.setText("every(1 #cycle)");
			data.put("when", when.getText());
			when.setEnabled(false);
			
			staticBtn.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent event) {
					Button btn = (Button) event.getSource();
					data.put("static", btn.getSelection() +"");
					when.setEnabled(!cd.getText().isBlank() && !btn.getSelection() );
					
				}
			});
			

			bt.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent event) {
					Button btn = (Button) event.getSource();
					cd.setEnabled(btn.getSelection());
					staticBtn.setEnabled(btn.getSelection());
					buffer.setEnabled(!cd.getText().isBlank() && btn.getSelection() && geometryB.get(cd.getText()));
					when.setEnabled(!cd.getText().isBlank() && btn.getSelection()  && !staticBtn.getSelection());
					
					data.put("keep", ""+btn.getSelection());

				}
			});

			cd.setEnabled(false);
			cd.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(final SelectionEvent e) {
					data.put("properties", cd.getText());
					
					buffer.setEnabled(!cd.getText().isBlank() && bt.getSelection() && geometryB.get(cd.getText()));
					when.setEnabled(!cd.getText().isBlank() && bt.getSelection()  && !staticBtn.getSelection());
					
					
				}
			});
			

		}

		setControl(scroll);
	}

	Button addBooleanProperty(Group groupProperties, String name, String legend, Map<String, String> data) {
		Button bt = new Button(groupProperties, SWT.CHECK);
		bt.setText(legend);
		bt.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent event) {
				Button btn = (Button) event.getSource();
				data.put(name, btn.getSelection() + "");
			}
		});
		// booleanAreas.put(name, bt);
		Label lpc = new Label(groupProperties, SWT.LEFT);
		lpc.setText("");
		return bt;

	}

	Text addStringProperty(Composite groupProperties, String name, String legend, Map<String,String> data) {
		Label lpc = new Label(groupProperties, SWT.LEFT);
		lpc.setText(legend);
		Text tpc = new Text(groupProperties, SWT.BORDER);
		tpc.setText("");
		 tpc.addModifyListener(e -> data.put(name, tpc.getText()));
		return tpc;
	}

	public List<String> getItemsP() {
		return itemsP;
	}

	public void setItemsP(Map<String, Map<String, String>> items) {
		this.itemsP = new ArrayList<>(items.keySet());
		this.geometryB = new Hashtable<>();
		
		for (String p: items.keySet()) {
			 Map<String, String> it = items.get(p);
			 geometryB.put(p, ! ("true".equals(it.get("has_prefab"))));
		}
		if (!itemsP.isEmpty()) {
			for (Button bt : speciesBtn) {
				bt.setEnabled(true);
			}
			for (Combo cd : combos) {
				cd.setItems(itemsP.toArray(new String[itemsP.size()]));
				if (!itemsP.isEmpty()) {
					cd.setText(itemsP.get(0));
					for (Map<String,String> data : speciesToSend.values()) {
						data.put("properties", itemsP.get(0));
					}
					

				}
			}
		} else {
			for (Button bt : speciesBtn) {
				bt.setEnabled(false);
			}
		}
	}

}
