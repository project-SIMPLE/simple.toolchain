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

import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import gama.core.metamodel.shape.GamaPoint;
import gama.core.util.GamaColor;
import gaml.extension.unity.commands.wizard.VRModelGenerator.Player;

/**
 * The Class WizardPagePlayer.
 */
public class WizardPagePlayer extends UnityWizardPage {

	String currentPlayer;

	UnityPropertiesEditor propertiesEditor = new UnityPropertiesEditor(this);

	private Text text_x;
	private Text text_y;
	private Text text_z;
	private ColorSelector cs_color;
	private Text text_min;
	private Text text_max;
	private Text text_size;

	private ListViewer playersList;
	private Group grpAttributes;

	/**
	 * Instantiates a new wizard page player.
	 *
	 * @param model
	 *            the model
	 * @param gen
	 *            the gen
	 */

	protected WizardPagePlayer(final VRModelGenerator gen) {
		super("Players", gen);
		setTitle("Define the information about the players");
		setDescription("Please enter information about the players");
		setPageComplete(true);

	}

	@Override
	public void createControlsIn(final Composite compo) {
		Composite globalContainer = new Composite(compo, SWT.NONE);
		globalContainer.setLayout(new GridLayout(2, false));
		globalContainer.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		createPlayersList(globalContainer);
		createPlayersAttributes(globalContainer);
		propertiesEditor.createPropertiesComposite(globalContainer);
		showProperties(false);
	}

	private void createPlayersAttributes(final Composite parent) {
		grpAttributes = new Group(parent, SWT.NONE);
		grpAttributes.setText("Attributes");
		grpAttributes.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		grpAttributes.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());

		{
			Label lblInitLocationOf = new Label(grpAttributes, SWT.NONE);
			lblInitLocationOf.setText("Initial location");
			lblInitLocationOf.setLayoutData(
					GridDataFactory.fillDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER).create());

			Composite grpCoord = new Composite(grpAttributes, SWT.NONE);
			grpCoord.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());

			label(grpCoord, "X:");
			text_x = text(grpCoord, "50");
			label(grpCoord, "Y:");
			text_y = text(grpCoord, "50");
			label(grpCoord, "Z:");
			text_z = text(grpCoord, "0");

			ModifyListener ml = e -> {
				Double x = Double.valueOf(text_x.getText());
				Double y = Double.valueOf(text_y.getText());
				Double z = Double.valueOf(text_z.getText());
				if (x != null && y != null && z != null) {
					generator.getPlayer(currentPlayer).location = new GamaPoint(x, y, z);
				}
			};
			text_x.addModifyListener(ml);
			text_y.addModifyListener(ml);
			text_z.addModifyListener(ml);
			GridLayoutFactory.fillDefaults().numColumns(6).generateLayout(grpCoord);
		}

		{
			label(grpAttributes, "Size in GAMA");
			text_size = text(grpAttributes, String.valueOf(generator.getDefaultPlayerSize()));
			text_size.addModifyListener(
					e -> { generator.getPlayer(currentPlayer).size = Double.valueOf(text_size.getText()); });
		}

		{
			label(grpAttributes, "Color in GAMA");
			cs_color = new ColorSelector(grpAttributes);
			GamaColor c = generator.getDefaultPlayerColor();
			RGB def = new RGB(c.red(), c.green(), c.blue());
			cs_color.setColorValue(def);
			cs_color.addListener(event -> {
				RGB col = cs_color.getColorValue();
				generator.getPlayer(currentPlayer).color = GamaColor.get(col.red, col.green, col.blue);
			});
			cs_color.getButton().setLayoutData(
					GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).create());
		}

		grpAttributes.setVisible(false);

	}

	private void createPlayersList(final Composite parent) {
		Group grpPlayers = new Group(parent, SWT.NONE);
		grpPlayers.setText("Players");
		grpPlayers.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).create());
		grpPlayers.setLayoutData(
				GridDataFactory.fillDefaults().grab(false, true).span(1, 2).hint(200, SWT.DEFAULT).create());
		createNumberOfPlayers(grpPlayers);
		playersList = new ListViewer(grpPlayers, SWT.BORDER | SWT.SINGLE);
		playersList.getList().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		playersList.setContentProvider(ArrayContentProvider.getInstance());
		playersList.setLabelProvider(new LabelProvider() {

			@Override
			public String getText(final Object element) {
				return String.valueOf(element);
			}
		});
		playersList.addPostSelectionChangedListener(e -> {
			Object o = playersList.getStructuredSelection().getFirstElement();
			showProperties(o != null);
			if (o != null) {
				currentPlayer = o.toString();
				updateControlsWithPlayer();
			}
		});
		playersList.setInput(generator.players.keySet());

	}

	private void updateControlsWithPlayer() {
		Player p = generator.getPlayer(currentPlayer);
		text_x.setText("" + p.location.x);
		text_y.setText("" + p.location.y);
		text_z.setText("" + p.location.z);
		// text_distance.setText("" + p.minDistance);
		// text_radius.setText("" + p.perceptionRadius);
		text_size.setText("" + p.size);
		GamaColor c = p.color;
		cs_color.setColorValue(new RGB(c.red(), c.green(), c.blue()));
		propertiesEditor.setSelection(p.property, true);
	}

	private void createNumberOfPlayers(final Composite parent) {
		Composite grpNumberOfPlayers = new Composite(parent, SWT.NONE);
		grpNumberOfPlayers.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		grpNumberOfPlayers.setLayout(GridLayoutFactory.fillDefaults().numColumns(4).create());

		label(grpNumberOfPlayers, "Min: ");
		text_min = text(grpNumberOfPlayers, generator.getMin_num_player() + "");
		text_min.addModifyListener(e -> {
			Integer tami = Integer.valueOf(text_min.getText());
			if (tami != null) { generator.setMin_num_player(tami); }
			updatePlayers();
		});

		Button btnHasAMax = check(grpNumberOfPlayers, "Max: ");
		btnHasAMax.setSelection(true);
		btnHasAMax.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent event) {
				generator.setHas_max_num_player(btnHasAMax.getSelection());
				text_max.setEnabled(btnHasAMax.getSelection());
				updatePlayers();
			}
		});

		text_max = text(grpNumberOfPlayers, generator.getMax_num_player() + "");
		text_max.addModifyListener(e -> {
			Integer tmaai = Integer.valueOf(text_max.getText());
			if (tmaai != null) { generator.setMax_num_player(tmaai); }
			updatePlayers();
		});

	}

	private void updatePlayers() {
		int number = Integer.parseInt(text_min.getText());
		if (text_max.isEnabled()) { number = Math.max(number, Integer.parseInt(text_max.getText())); }
		int size = generator.players.size();
		if (number == 0) {
			generator.players.clear();
		} else if (number > size) {
			for (int i = size; i < number; i++) { generator.addPlayer(i); }
		} else {
			for (int i = size - 1; i >= number; i--) { generator.removePlayer(i); }
		}
		playersList.refresh();
	}

	@Override
	protected void propertyChanged(final String item) {
		if (currentPlayer != null) { generator.getPlayer(currentPlayer).property = item; }
	}

	@Override
	protected String getPropertyNameHint() { return currentPlayer; }

	void showProperties(final boolean b) {
		grpAttributes.setVisible(b);
		propertiesEditor.setVisible(b);
		getControl().requestLayout();
	}

	@Override
	public void pageChanged(final PageChangedEvent event) {
		propertiesEditor.propertiesChanged();
	}

}
