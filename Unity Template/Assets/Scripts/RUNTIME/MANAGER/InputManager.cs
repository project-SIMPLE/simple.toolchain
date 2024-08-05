using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.XR.Interaction.Toolkit;
using UnityEngine.InputSystem;

public class InputManager : MonoBehaviour
{
    [Header("Teleportation Input--------------------")]
    public GameObject teleportationRay;
    public InputActionProperty teleportActive;

    [Header("Building Input-------------------------")]
    public BuildSystemManager buildManager;
    public BuildUI buildUI;
    public InputActionProperty buildActive;
    public GameObject buildRay;
    public InputActionProperty buildAction;

    void Update()
    {
        // teleportation interaction
        teleportationRay.SetActive(teleportActive.action.ReadValue<Vector2>() != Vector2.zero);

        // build system interaction
        if (buildActive.action.triggered)
        {
            if (buildManager.IsBuilding) buildManager.FinishBuilding();
            else
            {
                buildUI.ToggleMenu();
                buildUI.ToggleRemoveConstruction(false);
            }
            
        }
        buildRay.SetActive(buildManager.IsBuilding);
        if (buildAction.action.ReadValue<float>() >= 0.5f)
        {
            buildManager.Build();
        }
    }

}
