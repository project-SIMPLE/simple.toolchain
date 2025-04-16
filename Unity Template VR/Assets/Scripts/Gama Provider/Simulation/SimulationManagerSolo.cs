using System;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;
using UnityEngine.XR.Interaction.Toolkit; 
using UnityEngine.InputSystem;



public class SimulationManagerSolo : SimulationManager
{
    protected bool isNight = false;

    public GameObject lightObject;

    protected override void TriggerMainButton()
    {
        isNight = !isNight;

        lightObject.SetActive(!isNight);
    } 
     
    protected override void AdditionalInitAfterGeomLoading()
    {
        if (parameters.hotspots != null && parameters.hotspots.Count > 0)
        {
            GameObject[] blocks = GameObject.FindGameObjectsWithTag("selectable");

            foreach (GameObject gameObj in blocks)
            {
                if (parameters.hotspots.Contains(gameObj.name))
                {
                    SelectedObjects.Add(gameObj);
                    SimulationManagerSolo.ChangeColor(gameObj, Color.red);
                }
            }
        }
    }



    protected override void ManageOtherInformation()
    {

    }

    protected override void OtherUpdate()
    {

    }

    protected override void ManageOtherMessages(string content)
    {

    }
    protected override void HoverEnterInteraction(HoverEnterEventArgs ev)
    {

        GameObject obj = ev.interactableObject.transform.gameObject;
        if (obj.tag.Equals("selectable") || obj.tag.Equals("car") || obj.tag.Equals("moto"))
            SimulationManagerSolo.ChangeColor(obj, Color.blue);
    }

    protected override void HoverExitInteraction(HoverExitEventArgs ev)
    {
        GameObject obj = ev.interactableObject.transform.gameObject;
        if (obj.tag.Equals("selectable"))
        {
            bool isSelected = SelectedObjects.Contains(obj);

            SimulationManagerSolo.ChangeColor(obj, isSelected ? Color.red : Color.gray);
        }
        else if (obj.tag.Equals("car") || obj.tag.Equals("moto"))
        {
            SimulationManagerSolo.ChangeColor(obj, Color.white);
        }


    }

    protected override void SelectInteraction(SelectEnterEventArgs ev)
    {

        if (remainingTime <= 0.0)
        {
            GameObject grabbedObject = ev.interactableObject.transform.gameObject;

            if (("selectable").Equals(grabbedObject.tag))
            {
                Dictionary<string, string> args = new Dictionary<string, string> {
                         {"id", grabbedObject.name }
                    };
                ConnectionManager.Instance.SendExecutableAsk("update_hotspot", args);
                bool newSelection = !SelectedObjects.Contains(grabbedObject);
                if (newSelection)
                    SelectedObjects.Add(grabbedObject);
                else
                    SelectedObjects.Remove(grabbedObject);
                SimulationManagerSolo.ChangeColor(grabbedObject, newSelection ? Color.red : Color.gray);

                remainingTime = timeWithoutInteraction;
            }
            else if (grabbedObject.tag.Equals("car") || grabbedObject.tag.Equals("moto"))
            {
                Dictionary<string, string> args = new Dictionary<string, string> {
                         {"id", grabbedObject.name }
                    };
                ConnectionManager.Instance.SendExecutableAsk("remove_vehicle", args);
                grabbedObject.SetActive(false);
                //toDelete.Add(grabbedObject);

            }


        }

    }
}