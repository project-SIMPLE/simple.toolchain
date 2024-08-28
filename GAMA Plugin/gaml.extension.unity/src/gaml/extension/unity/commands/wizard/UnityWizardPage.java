package gaml.extension.unity.commands.wizard;

import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public abstract class UnityWizardPage extends WizardPage implements IPageChangedListener {

	VRModelGenerator generator;

	protected UnityWizardPage(final String pageName, final VRModelGenerator gen) {
		super(pageName);
		this.generator = gen;
	}

	@Override
	public void createControl(final Composite parent) {
		WizardDialog dialog = (WizardDialog) getContainer();
		dialog.addPageChangedListener(this);

		ScrolledComposite scroll = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		setControl(scroll);
		scroll.setAlwaysShowScrollBars(false);
		scroll.setExpandVertical(true);
		scroll.setExpandHorizontal(true);
		scroll.setLayout(new FillLayout(SWT.VERTICAL));

		Composite container = new Composite(scroll, SWT.NONE);
		container.setLayout(new GridLayout(1, false));

		scroll.setContent(container);
		scroll.addControlListener(ControlListener.controlResizedAdapter(e -> {
			scroll.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		}));
		createControlsIn(container);
	}

	public void notify(final Control c) {
		c.notifyListeners(SWT.Selection, new Event());
	}

	abstract protected void createControlsIn(Composite container);

	protected abstract void propertyChanged(String item);

	protected abstract String getPropertyNameHint();

	static Label label(final Composite parent, final String text) {
		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(
				GridDataFactory.fillDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER).create());
		label.setText(text);
		return label;
	}

	static Text text(final Composite parent, final String init) {
		Text text = new Text(parent, SWT.BORDER);
		text.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).create());
		text.setText(init);
		return text;
	}

	static Button check(final Composite parent, final String text) {
		Button b = new Button(parent, SWT.CHECK);
		b.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).create());
		b.setText(text);
		return b;
	}

}
