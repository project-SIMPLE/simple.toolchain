package gaml.extensions.unity.commands.wizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import msi.gama.kernel.experiment.IExperimentPlan;
import msi.gama.kernel.model.IModel;

public class WizardPageGeneralInformation extends WizardPage {

	IModel model;

    private Composite container;
    
    VRModelGenerator generator;
    
    WizardPageDisplay wDisplay;
    
	protected WizardPageGeneralInformation(String path, IModel model, VRModelGenerator gen) {
		 super("GeneralInformation");
		 setTitle("Define the general information to define the VR experiment");
		 setDescription("Please enter information about VR experiment");
		 this.model = model;
		 this.generator = gen;
		 gen.setModelName(model.getName() + "_VR");
		 gen.setModelPath(path);
	}
	

	@Override
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		 container.setLayout(new FillLayout(SWT.VERTICAL));
		 Group groupConnection = new Group(container, SWT.NONE);
		 groupConnection.setLayout(new GridLayout(2, false));
		 groupConnection.setText("Information about the connection");
			
		 Label lp = new Label(groupConnection, SWT.LEFT);
			lp.setText("Port:" );
			Text tp =  new Text(groupConnection, SWT.BORDER);
			tp.setText(generator.getPort().toString());
		    tp.addModifyListener(new ModifyListener() {
				
				@Override
			
				public void modifyText(ModifyEvent e) {
					Integer port = Integer.decode(tp.getText());
					if (port != null)
						generator.setPort(port);
				}
		    });
			
			 Group groupExperiment = new Group(container, SWT.NONE);
			 groupExperiment.setLayout(new GridLayout(2, false));
			 groupExperiment.setText("Information about the experiment");
				

				Label lc = new Label(groupExperiment, SWT.LEFT);
				lc.setText("Minimium duration of a cycle in s (minimum_cycle_duration):" );
				Text tc =  new Text(groupExperiment, SWT.BORDER);
				tc.setText(generator.getMinimumCycleDuration().toString());
				tc.addModifyListener(new ModifyListener() {
					
					@Override
				
					public void modifyText(ModifyEvent e) {
						Double duration = Double.valueOf(tc.getText());
						if (duration != null)
							generator.setMinimumCycleDuration(duration);
					}
			    });
				Label lxp = new Label(groupExperiment, SWT.LEFT);
				lxp.setText("Main Experiment:" );
			
				Combo cXp = new Combo(groupExperiment, SWT.READ_ONLY);
				 List<String> items = new ArrayList<String>();
				 for (IExperimentPlan ep : model.getExperiments()) {
					 items.add(ep.getName());
				 }
				 
				 cXp.setItems((String[]) items.toArray(new String[items.size()]));
				 if (! items.isEmpty()) {
					 cXp.setText(items.get(0));
					  generator.setExperimentName(cXp.getText());
					  wDisplay.updateExperiment();
				    	
				 }

				 cXp.addSelectionListener(new SelectionAdapter() {
				      public void widgetDefaultSelected(SelectionEvent e) {
				    	  generator.setExperimentName(cXp.getText());
				    	  wDisplay.updateExperiment();
				      }
				    });

		
					 
		 setControl(container);
	       
		
	}


	public WizardPageDisplay getwDisplay() {
		return wDisplay;
	}


	public void setwDisplay(WizardPageDisplay wDisplay) {
		this.wDisplay = wDisplay;
	}
	
	

}
