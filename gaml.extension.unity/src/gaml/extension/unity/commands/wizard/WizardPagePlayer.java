/*******************************************************************************************************
 *
 * WizardPagePlayer.java, in gaml.extension.unity, is part of the source code of the GAMA modeling and simulation
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

import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import gama.core.kernel.model.IModel;
import gama.core.metamodel.shape.GamaPoint;

/**
 * The Class WizardPagePlayer.
 */
public class WizardPagePlayer extends WizardPage {

	/** The model. */
	IModel model;

	/** The container. */
	private Composite container;

	/** The generator. */
	VRModelGenerator generator;

	/** The items D. */
	List<String> itemsD;
	
	
	private Text text;
	private Text text_1;
	private Text text_2;
	private Text txtPlayerColor;
	private Text text_3;
	private Text text_4;
	private Text text_5;
	private Text text_6;
	private Text text_7;

	private org.eclipse.swt.widgets.List list;
	private org.eclipse.swt.widgets.List list_1;
	/**
	 * Instantiates a new wizard page player.
	 *
	 * @param model
	 *            the model
	 * @param gen
	 *            the gen
	 */
	
	
	
	protected WizardPagePlayer(final IModel model, final VRModelGenerator gen) {
		super("Player");
		setTitle("Define the information about the player");
		setDescription("Please enter information about the player");
		setPageComplete(true);
		this.model = model;
		this.generator = gen;

	}
	
	

	@Override
	public void createControl(final Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);

		setControl(container);
		generator.setLocationInit(new GamaPoint(50.0, 50.0, 0.0));

		Group group = new Group(container, SWT.NONE);
		group.setBounds(31, 130, 752, 36);
		
		Label lblX = new Label(group, SWT.NONE);
		lblX.setBounds(206, 7, 21, 14);
		lblX.setText("X:");
		
		Label lblY = new Label(group, SWT.NONE);
		lblY.setBounds(317, 7, 21, 20);
		lblY.setText("Y:");
		
		Label lblZ = new Label(group, SWT.NONE);
		lblZ.setBounds(426, 7, 15, 14);
		lblZ.setText("Z:");
		
		text = new Text(group, SWT.BORDER);
		text.setBounds(230, 4, 64, 19);
		
		text_1 = new Text(group, SWT.BORDER);
		text_1.setBounds(338, 4, 64, 19);
		
		text_2 = new Text(group, SWT.BORDER);
		text_2.setBounds(447, 4, 64, 19);
		
		text.setText("" + generator.getLocationInit().x);
		text_1.setText("" + generator.getLocationInit().y);
		text_2.setText("" + generator.getLocationInit().z);

		ModifyListener ml = e -> {
			Double x = Double.valueOf(text.getText());
			Double y = Double.valueOf(text_1.getText());
			Double z = Double.valueOf(text_2.getText());
			if (x != null && y != null && z != null) { generator.setLocationInit(new GamaPoint(x, y, z)); }

		};

		text.addModifyListener(ml);
		text_1.addModifyListener(ml);
		text_2.addModifyListener(ml);

		
		Label lblInitLocationOf = new Label(group, SWT.NONE);
		lblInitLocationOf.setBounds(10, 7, 149, 14);
		lblInitLocationOf.setText("Init location of players");
		
		Group grpGamaDisplay = new Group(container, SWT.NONE);
		grpGamaDisplay.setText("GAMA display");
		grpGamaDisplay.setBounds(31, 266, 752, 228);
		
		Label lblNewLabel_1 = new Label(grpGamaDisplay, SWT.NONE);
		lblNewLabel_1.setBounds(21, 10, 59, 14);
		lblNewLabel_1.setText("Player size (Gama display)");
		
		txtPlayerColor = new Text(grpGamaDisplay, SWT.BORDER);
		txtPlayerColor.setText("#red");
		txtPlayerColor.setBounds(311, 7, 64, 19);
		
		txtPlayerColor.setText(generator.getPlayerColor());
		txtPlayerColor.addModifyListener(e -> generator.setPlayerColor(txtPlayerColor.getText()));

		
		Label lblPlayerColor = new Label(grpGamaDisplay, SWT.NONE);
		lblPlayerColor.setBounds(224, 10, 76, 14);
		lblPlayerColor.setText("Player color");
		
		text_7 = new Text(grpGamaDisplay, SWT.BORDER);
		text_7.setBounds(111, 5, 64, 19);
		text_7.setText(generator.getPlayerSize().toString());
		text_7.addModifyListener(e -> {
			Double ps = Double.valueOf(text_7.getText());
			if (ps != null) { generator.setPlayerSize(ps); }
		});
		
		Label lblNewLabel_2 = new Label(grpGamaDisplay, SWT.NONE);
		lblNewLabel_2.setBounds(21, 44, 364, 14);
		lblNewLabel_2.setText("Unity Properties used to represent the other players");
		
		
		Label lblListOfDefined = new Label(grpGamaDisplay, SWT.NONE);
		lblListOfDefined.setBounds(57, 64, 180, 14);
		lblListOfDefined.setText("list of defined properties");
		
		Label lblListOfProperties = new Label(grpGamaDisplay, SWT.NONE);
		lblListOfProperties.setText("list of properties used for players");
		lblListOfProperties.setBounds(320, 64, 180, 14);
		
		
		ListViewer listViewer = new ListViewer(grpGamaDisplay, SWT.BORDER | SWT.V_SCROLL);
		list = listViewer.getList();
		
		list.setBounds(31, 84, 216, 82);
		
		ListViewer listViewer_1 = new ListViewer(grpGamaDisplay, SWT.BORDER | SWT.V_SCROLL);
		list_1 = listViewer_1.getList();
		list_1.setBounds(305, 84, 216, 80);
		
		Button btnRemove = new Button(grpGamaDisplay, SWT.NONE);
		btnRemove.setBounds(444, 170, 76, 27);
		btnRemove.setText("remove");
		btnRemove.addListener(SWT.Selection, new Listener() {
		      public void handleEvent(Event e) {
		    	  if ((e.type ==  SWT.Selection) && (list_1.getSelectionIndex() >= 0)) {
		    		  list_1.remove(list_1.getSelectionIndex());
		    		  updatePlayerProperties();
		          }
		      } 
		    });
		
		
		Button btnDown = new Button(grpGamaDisplay, SWT.NONE);
		btnDown.setText("down");
		btnDown.setBounds(365, 170, 59, 27);
		btnDown.addListener(SWT.Selection, new Listener() {
		      public void handleEvent(Event e) {
		    	  if ((e.type ==  SWT.Selection) && (list_1.getSelectionIndex() >= 0)) {
		    		  int index = list_1.getSelectionIndex();
		    		  if (index < (list_1.getItemCount() - 1)) {
		    			  String v = list_1.getItem(index);
		    			  String v2 = list_1.getItem(index+1);
		    			  list_1.setItem(index+1, v);
		    			  list_1.setItem(index, v2);
		    			  list_1.setSelection(index+1);
			    		  updatePlayerProperties();
		    		  } 
		          }
		      } 
		    });
		
		
		Button btnUp = new Button(grpGamaDisplay, SWT.NONE);
		btnUp.setText("up");
		btnUp.setBounds(311, 170, 59, 27);
		btnUp.addListener(SWT.Selection, new Listener() {
		      public void handleEvent(Event e) {
		    	  if ((e.type ==  SWT.Selection) && (list_1.getSelectionIndex() >= 0)) {
		    		  int index = list_1.getSelectionIndex();
		    		  if (index > 0) {
		    			  String v = list_1.getItem(index);
		    			  String v2 = list_1.getItem(index-1);
		    			  list_1.setItem(index-1, v);
		    			  list_1.setItem(index, v2);

		    			  list_1.setSelection(index-1);

			    		  updatePlayerProperties();
		    		  } 
		          }
		      } 
		    });
		
		Button btnNewButton = new Button(grpGamaDisplay, SWT.NONE);
		btnNewButton.setBounds(57, 172, 64, 27);
		btnNewButton.setText("add");
		btnNewButton.addListener(SWT.Selection, new Listener() {
		      public void handleEvent(Event e) {
		    	  if ((e.type ==  SWT.Selection) && (list.getSelectionIndex() >= 0)) {
		    		 list_1.add(list.getItem(list.getSelectionIndex()));

		    		  updatePlayerProperties();
		          }
		      } 
		    });
		
		Group grpNumberOfPlayers = new Group(container, SWT.NONE);
		grpNumberOfPlayers.setText("Number of players");
		grpNumberOfPlayers.setBounds(31, 10, 752, 113);
		
		Label lblMinNumberOf = new Label(grpNumberOfPlayers, SWT.NONE);
		lblMinNumberOf.setBounds(23, 11, 137, 20);
		lblMinNumberOf.setText("Min number of players");
		
		text_3 = new Text(grpNumberOfPlayers, SWT.BORDER);
		//text_3.setText("0");
		text_3.setBounds(230, 8, 64, 19);
		text_3.setText(generator.getMin_num_player() + "");
		text_3.addModifyListener(e -> {
			Integer tami = Integer.valueOf(text_3.getText());
			if (tami != null) { generator.setMin_num_player(tami); }
		});

		
		Button btnHasAMax = new Button(grpNumberOfPlayers, SWT.CHECK);
		btnHasAMax.setBounds(23, 37, 200, 16);
		btnHasAMax.setText("Has a max number of players?");
		btnHasAMax.setSelection(true);
		
		btnHasAMax.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent event) {
				Button btn = (Button) event.getSource();
				generator.setHas_max_num_player(btn.getSelection());
				if (btn.getSelection()) {
					text_4.setEnabled(true);

				} else {
					text_4.setEnabled(false);
				}

			}
		});


		Label lblMaxNumberOf = new Label(grpNumberOfPlayers, SWT.NONE);
		lblMaxNumberOf.setBounds(51, 63, 137, 20);
		lblMaxNumberOf.setText("Max number of players");
		
		text_4 = new Text(grpNumberOfPlayers, SWT.BORDER);
		//text_4.setText("0");
		text_4.setBounds(230, 60, 64, 19);
		text_4.setText(generator.getMax_num_player() + "");
		text_4.addModifyListener(e -> {
			Integer tmaai = Integer.valueOf(text_4.getText());
			if (tmaai != null) { generator.setMax_num_player(tmaai); }
		});
		
		Group grpFilteringOfThe = new Group(container, SWT.NONE);
		grpFilteringOfThe.setText("Filtering of the geometries sent to Unity");
		grpFilteringOfThe.setBounds(31, 178, 752, 82);
		
		Label lblPerceptionRadiusOf = new Label(grpFilteringOfThe, SWT.NONE);
		lblPerceptionRadiusOf.setBounds(23, 10, 214, 14);
		lblPerceptionRadiusOf.setText("Perception radius of the player agent");
		
		Label lblNewLabel = new Label(grpFilteringOfThe, SWT.NONE);
		lblNewLabel.setBounds(23, 38, 332, 14);
		lblNewLabel.setText("Min distance between agents to send to be considered");
		
		text_5 = new Text(grpFilteringOfThe, SWT.BORDER);
		text_5.setBounds(358, 5, 64, 19);
		text_5.setText(generator.getPlayerAgentsPerceptionRadius().toString());
		text_5.addModifyListener(e -> {
			Double papr = Double.valueOf(text_5.getText());
			if (papr != null) { generator.setPlayerAgentsPerceptionRadius(papr); }
		});
		
		
		text_6 = new Text(grpFilteringOfThe, SWT.BORDER);
		text_6.setBounds(358, 35, 64, 19);
		text_6.setText(generator.getPlayerAgentsPerceptionRadius().toString());
		text_6.addModifyListener(e -> {
			Double pamd = Double.valueOf(text_6.getText());
			if (pamd != null) { generator.setPlayerAgentsMinDist(pamd); }
		});
		


	}
	
	public void updatePlayerProperties() {
		List<String> pp = new ArrayList<>();
		for (String p: list_1.getItems()) {
			pp.add(p);
		}
		generator.setPlayerProperties(pp);
	}
	
	
	public void updateUnityProperties() {
		list.removeAll();
		for (String up : generator.getDefinedProperties().keySet())
			list.add(up);
		
	}

}
