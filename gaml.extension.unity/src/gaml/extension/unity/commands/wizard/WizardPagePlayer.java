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

import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
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
		container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout(SWT.VERTICAL));
		Group groupPlayer = new Group(container, SWT.NONE);
		groupPlayer.setLayout(new GridLayout(2, false));
		groupPlayer.setText("Information about the player");

		Label lma = new Label(groupPlayer, SWT.LEFT);
		lma.setText("Min number of players");
		Text tma = new Text(groupPlayer, SWT.BORDER);
		tma.setText(generator.getMin_num_player() + "");
		tma.addModifyListener(e -> {
			Integer tami = Integer.valueOf(tma.getText());
			if (tami != null) { generator.setMin_num_player(tami); }
		});

		Label lhmaa = new Label(groupPlayer, SWT.LEFT);
		lhmaa.setText("Has a max number of players?");
		Button bhmaa = new Button(groupPlayer, SWT.CHECK);
		bhmaa.setSelection(true);

		Label lmaa = new Label(groupPlayer, SWT.LEFT);
		lmaa.setText("Max number of players");
		Text tmaa = new Text(groupPlayer, SWT.BORDER);
		tmaa.setText(generator.getMax_num_player() + "");
		tmaa.addModifyListener(e -> {
			Integer tmaai = Integer.valueOf(tmaa.getText());
			if (tmaai != null) { generator.setMax_num_player(tmaai); }
		});

		bhmaa.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent event) {
				Button btn = (Button) event.getSource();
				generator.setHas_max_num_player(btn.getSelection());
				if (btn.getSelection()) {
					tmaa.setEnabled(true);

				} else {
					tmaa.setEnabled(false);
				}

			}
		});

		/*
		 * Label lp = new Label(groupPlayer, SWT.LEFT); lp.setText("Add a player agent?" ); Button bt = new
		 * Button(groupPlayer, SWT.CHECK); bt.setSelection(true);
		 */

		Label lic = new Label(groupPlayer, SWT.LEFT);
		lic.setText("Init location of the players");
		generator.setLocationInit(new GamaPoint(50.0, 50.0, 0.0));

		Group groupPlayerLoc = new Group(groupPlayer, SWT.NONE);
		groupPlayerLoc.setLayout(new GridLayout(6, false));

		Label lilx = new Label(groupPlayerLoc, SWT.LEFT);
		lilx.setText("X:");

		Text tilx = new Text(groupPlayerLoc, SWT.BORDER);
		Label lily = new Label(groupPlayerLoc, SWT.LEFT);
		lily.setText("Y:");

		Text tily = new Text(groupPlayerLoc, SWT.BORDER);
		 Label lilz = new Label(groupPlayerLoc, SWT.LEFT);
		 lilz.setText("Z:" );

		 Text tilz = new Text(groupPlayerLoc, SWT.BORDER);

		tilx.setText("" + generator.getLocationInit().x);
		tily.setText("" + generator.getLocationInit().y);
		 tilz.setText("" + generator.getLocationInit().z);

		ModifyListener ml = e -> {
			Double x = Double.valueOf(tilx.getText());
			Double y = Double.valueOf(tily.getText());
			// Double z = Double.valueOf(tilz.getText());
			if (x != null && y != null) { generator.setLocationInit(new GamaPoint(x, y)); }

		};

		tilx.addModifyListener(ml);
		tily.addModifyListener(ml);
		// tilz.addModifyListener(ml);

		Label lpr = new Label(groupPlayer, SWT.LEFT);
		lpr.setText("Perception radius of the player agent:");
		Text tpr = new Text(groupPlayer, SWT.BORDER);
		tpr.setText(generator.getPlayerAgentsPerceptionRadius().toString());
		tpr.addModifyListener(e -> {
			Double papr = Double.valueOf(tpr.getText());
			if (papr != null) { generator.setPlayerAgentsPerceptionRadius(papr); }
		});

		Label lpmd = new Label(groupPlayer, SWT.LEFT);
		lpmd.setText("Min distance between agents to send to be considered:");
		Text tmd = new Text(groupPlayer, SWT.BORDER);
		tmd.setText(generator.getPlayerAgentsPerceptionRadius().toString());
		tmd.addModifyListener(e -> {
			Double pamd = Double.valueOf(tmd.getText());
			if (pamd != null) { generator.setPlayerAgentsMinDist(pamd); }
		});

		Label lps = new Label(groupPlayer, SWT.LEFT);
		lps.setText("Player Size (display):");
		Text tps = new Text(groupPlayer, SWT.BORDER);
		tps.setText(generator.getPlayerSize().toString());
		tps.addModifyListener(e -> {
			Double ps = Double.valueOf(tps.getText());
			if (ps != null) { generator.setPlayerSize(ps); }
		});

		Label lpc = new Label(groupPlayer, SWT.LEFT);
		lpc.setText("Player color (display):");
		Text tpc = new Text(groupPlayer, SWT.BORDER);
		tpc.setText(generator.getPlayerColor());
		tpc.addModifyListener(e -> generator.setPlayerColor(tpc.getText()));

		/*
		 * bt.addSelectionListener(new SelectionAdapter() {
		 * 
		 * public void widgetSelected(SelectionEvent event) { Button btn = (Button) event.getSource();
		 * generator.setHasPlayer(btn.getSelection()); if (btn.getSelection()) { tpc.setEnabled(true);
		 * tps.setEnabled(true); tmd.setEnabled(true); tpr.setEnabled(true); tilx.setEnabled(true);
		 * tily.setEnabled(true); tilz.setEnabled(true); } else { tpc.setEnabled(false); tps.setEnabled(false);
		 * tmd.setEnabled(false); tpr.setEnabled(false); tilx.setEnabled(false); tily.setEnabled(false);
		 * tilz.setEnabled(false); }
		 * 
		 * } });
		 */
		setControl(container);

	}

}
