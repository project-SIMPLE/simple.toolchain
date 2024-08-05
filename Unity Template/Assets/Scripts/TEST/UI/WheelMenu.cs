using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class WheelMenu : MonoBehaviour
{
    [SerializeField] private BuildSystem buildingSystem;
    [SerializeField] private BuildSystemManager buildManager;
    public bool isClicked = false;

    public void Lock(){
        buildManager.StartBuilding(0);

        buildingSystem.currentVirtualConstruction = SelectedBuildType.sluiceGate;
        isClicked = true;
        buildingSystem.building = true;
    }  
    public void Turret(){
        buildManager.StartBuilding(1);
        buildingSystem.currentVirtualConstruction = SelectedBuildType.turret;
        isClicked = true;
        buildingSystem.building = true;
    }
    public void Lake(){
        buildingSystem.currentVirtualConstruction = SelectedBuildType.lake;
        isClicked = true;
        buildingSystem.building = true;
    }   
 
    public void SandBag(){
        buildingSystem.currentVirtualConstruction = SelectedBuildType.sandBag;
        isClicked = true;
        buildingSystem.building = true;
    }
    
}
