using System;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;
using UnityEngine.XR.Interaction.Toolkit; 
using UnityEngine.InputSystem;



public class SimulationManagerInteraction : SimulationManager
{

    //Defines what happens when a ray passes over an object 
    protected override void HoverEnterInteraction(HoverEnterEventArgs ev)
    {
         GameObject obj = ev.interactableObject.transform.gameObject;
        Debug.Log("HoverEnterInteraction : " + obj);
        if (obj.tag.Equals("pedestrian"))
        {
             Debug.Log("HoverEnterInteraction : " + obj);
             SimulationManagerSolo.ChangeColor(obj, Color.blue);
        } 
        
            
    }


    //Defines what happens when a ray passes not anymore over an object 
    protected override void HoverExitInteraction(HoverExitEventArgs ev)
    {
        GameObject obj = ev.interactableObject.transform.gameObject;
        if (obj.tag.Equals("pedestrian"))
        {
             SimulationManagerSolo.ChangeColor(obj, Color.white);
        }
    }

    //Defines what happens when a object is selected
    protected override void SelectInteraction(SelectEnterEventArgs ev)
    {

        if (remainingTime <= 0.0)
        {
            GameObject grabbedObject = ev.interactableObject.transform.gameObject;
 Debug.Log("grabbedObject : " + grabbedObject);
       
            if (grabbedObject.tag.Equals("pedestrian") )
            {
                Dictionary<string, string> args = new Dictionary<string, string> {
                         {"id", grabbedObject.name }
                    };
                ConnectionManager.Instance.SendExecutableAsk("remove_pedestrian", args);

            } 


        }

    }

    //Defines what happens when the main button (of the right controller) is trigger 
    protected override void TriggerMainButton()
    {
       
    }

    //Defines what happens when a non-standard message is received from GAMA. 
    protected override void ManageOtherMessages(string content)
    {

    }

    //Processes additional information contained in WorldJSONInfo - sent by GAMA at each simulation step.  
    protected override void ManageOtherInformation()
    {

    }


    //Adds extra actions to be performed for each new frame.
    protected override void OtherUpdate()
    {

    }

   
}