using System;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;
using UnityEngine.XR.Interaction.Toolkit; 
using UnityEngine.InputSystem;
using TMPro;



public class SimulationManagerMulti : SimulationManager
{
    protected int score = 0 ;
    protected int ranking = 1;
    protected int numTokens = 0;
    private TextMeshProUGUI infoPlayerHUD;


    protected override void ManageOtherInformation()
    {
        string id = ConnectionManager.Instance.getUseMiddleware() ? ConnectionManager.Instance.GetConnectionId() : ("\"" + ConnectionManager.Instance.GetConnectionId() + "\"");
         int index  = infoWorld.players.IndexOf(id);
        ranking = infoWorld.ranking[index];
        numTokens = infoWorld.numTokens;
        updateHUD();


    }

    protected void updateHUD()
    {
        if (infoPlayerHUD == null)
            infoPlayerHUD = GameObject.FindGameObjectWithTag("HUD").GetComponentInChildren<TextMeshProUGUI>();

        if (infoPlayerHUD != null)
            infoPlayerHUD.SetText("Score: " + score + " - Ranking: " + ranking +  " - Token to Find: " + numTokens);
    }

    protected override void TriggerMainButton()
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
        ChangeColor(obj, Color.blue);
    }

    protected override void HoverExitInteraction(HoverExitEventArgs ev)
    {
        GameObject obj = ev.interactableObject.transform.gameObject;
        ChangeColor(obj, Color.white);

    }

    protected override void SelectInteraction(SelectEnterEventArgs ev)
    {

        if (remainingTime <= 0.0)
        {
            GameObject grabbedObject = ev.interactableObject.transform.gameObject;

            string id = ConnectionManager.Instance.getUseMiddleware() ? ConnectionManager.Instance.GetConnectionId() : ("\"" + ConnectionManager.Instance.GetConnectionId() + "\"");

            Dictionary<string, string> args = new Dictionary<string, string> {
                         {"id", grabbedObject.name },
                         {"player", id }
                    };
                ConnectionManager.Instance.SendExecutableAsk("remove_token", args);
                grabbedObject.SetActive(false);
                score = score + 1;
                updateHUD();
               // toDelete.Add(grabbedObject);
        }
        
    }
}