using System;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;
using UnityEngine.XR.Interaction.Toolkit;
using UnityEngine.InputSystem;


public class SimulationMultiPlayerExample : SimulationManager
{
    Dictionary<string, GameObject> SelecableObjects = null;
    Dictionary<GameObject, Color> ObjectColor = null;

    protected ChangeColorMessage message = null;

    protected override void ManageOtherMessages(string content)
    {
        if (content.Contains("id"))
            message = ChangeColorMessage.CreateFromJSON(content);

     }

    //action activated at the end of the update phase (every frame)
     protected override void OtherUpdate()
     {

        if (message != null) {
            Color c = new Color32(BitConverter.GetBytes(message.color[0])[0], BitConverter.GetBytes(message.color[1])[0],
                    BitConverter.GetBytes(message.color[2])[0], BitConverter.GetBytes(message.color[3])[0]);

            if (SelecableObjects == null)
            {
                SelecableObjects = new Dictionary<string, GameObject>();
                ObjectColor = new Dictionary<GameObject, Color>();
                GameObject[] objs = GameObject.FindGameObjectsWithTag("selectable");
                foreach (GameObject o in objs)
                {
                    SelecableObjects.Add(o.name, o);
                }
            }
            GameObject obj = SelecableObjects[message.id];
            SimulationMultiPlayerExample.ChangeColor(obj, c);
            if (!ObjectColor.ContainsKey(obj))
                ObjectColor.Add(obj, c);
            else
            {
                ObjectColor[obj] = c;
            }
            message = null;
        }


     }
    
     
    protected override void HoverEnterInteraction(HoverEnterEventArgs ev)
    {
        Debug.Log("HoverEnterInteraction");

        GameObject obj = ev.interactableObject.transform.gameObject;
        if (obj.tag.Equals("selectable"))
            SimulationMultiPlayerExample.ChangeColor(obj, Color.blue);
    }

    protected override void HoverExitInteraction(HoverExitEventArgs ev)
    {

        Debug.Log("HoverExitInteraction");
        GameObject obj = ev.interactableObject.transform.gameObject;
        if (obj.tag.Equals("selectable"))
        {
            SimulationMultiPlayerExample.ChangeColor(obj,(ObjectColor != null &&  ObjectColor.ContainsKey(obj)) ?  ObjectColor[obj] : Color.gray);
        }
    }

    protected override void SelectInteraction(SelectEnterEventArgs ev)
    {

        Debug.Log("SelectInteraction");
        if (remainingTime <= 0.0)
        {
            GameObject obj = ev.interactableObject.transform.gameObject;

            if (("selectable").Equals(obj.tag))
            {
                Dictionary<string, string> args = new Dictionary<string, string> {
                         {"id", obj.name },
                         {"player",ConnectionManager.Instance.getUseMiddleware() ? ConnectionManager.Instance.GetConnectionId()  : ("\"" + ConnectionManager.Instance.GetConnectionId() +  "\"") },

                    };
                ConnectionManager.Instance.SendExecutableAsk("change_color", args);
               
                remainingTime = timeWithoutInteraction;
            }

        }

    }

}



[System.Serializable]
public class ChangeColorMessage
{

   
    public string id;
    public List<int> color;

    public static ChangeColorMessage CreateFromJSON(string jsonString)
    {
        return JsonUtility.FromJson<ChangeColorMessage>(jsonString);
    }

}
