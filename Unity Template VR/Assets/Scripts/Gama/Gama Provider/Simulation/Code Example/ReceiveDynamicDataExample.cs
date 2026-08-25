using System;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;
using UnityEngine.XR.Interaction.Toolkit;
using UnityEngine.InputSystem;


public class ReceiveDynamicDataExample : SimulationManager
{


    protected override void ManageAttributes(List<Attributes> attributes)
    {
        for (int i = 0; i < infoWorld.names.Count; i++)
        {
            string name = infoWorld.names[i];
            int type = attributes[i].type;
            List<object> o = geometryMap[name];
            GameObject obj = (GameObject)o[0];
            if (type == 0)
            {
                ChangeColor(obj, Color.white);
            }
            else if (type == 1)
            {
                ChangeColor(obj, Color.blue);
            }
            else if (type == 2)
            {
                ChangeColor(obj, Color.red);
            }
        }
        
    }
}