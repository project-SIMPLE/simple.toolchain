using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.InputSystem;
using UnityEngine.XR.Interaction.Toolkit;

public class ToggleHeight : MonoBehaviour
{
    [SerializeField] private InputActionReference toggleHeightAction;
    [SerializeField] private float maxHeight;
    [SerializeField] private float minHeight;
    [SerializeField] private Transform controllerTransform;

    [Header("Boundaries of the map")]
    [SerializeField] private float minX = 180;
    [SerializeField] private float maxX = 666;
    [SerializeField] private float minY = -1057;
    [SerializeField] private float maxY = -328;

    private bool highPosition = true;

    // ############################################################

    void OnEnable() {
        toggleHeightAction.action.performed += HandleToggleHeight;
    }

    void OnDisable() {
        toggleHeightAction.action.performed -= HandleToggleHeight;
    }

    // ############################################################

    private void HandleToggleHeight(InputAction.CallbackContext obj) {
        if (SimulationManager.Instance.IsGameState(GameState.GAME)) toggleHeight();
    }

    // ############################################################

    private void toggleHeight() {
        if (highPosition) {
            Vector3 controllerPos = controllerTransform.position;
            Vector3 controllerDir = controllerTransform.forward;
            Vector3 planePos = new Vector3(0, 0, 0);
            Vector3 planeNormal = new Vector3(0, 1, 0);
            float t = Vector3.Dot(planeNormal, (planePos - controllerPos)) / Vector3.Dot(planeNormal, controllerDir);
            Vector3 intersectionPoint = controllerPos + t * controllerDir;
            transform.position = new Vector3(Math.Clamp(intersectionPoint.x, minX, maxX), minHeight, Math.Clamp(intersectionPoint.z, minY, maxY));
        } else {
            transform.position = new Vector3(transform.position.x, maxHeight, transform.position.z);
        }
        highPosition = !highPosition;
    }
}
