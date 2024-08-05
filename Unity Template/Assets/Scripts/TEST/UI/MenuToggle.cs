using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class MenuToggle : MonoBehaviour
{

    [SerializeField] private GameObject menu;
    public WheelMenu wheelMenu;
    public bool menuVisible = false;

    private void Update()
    {
        if (Input.GetKeyDown(KeyCode.B))
        {
            ToggleMenu();
        }
        if(wheelMenu.isClicked)
        {
            menuVisible = !menuVisible;
            HideMenu();
            wheelMenu.isClicked = false;
        }
    }
    private void ToggleMenu()
    {
        menuVisible = !menuVisible;
        if (menuVisible)
        {
            ShowMenu();
        }
        else
        {
            HideMenu();
        }
    }

    private void ShowMenu()
    {
        menu.SetActive(true);
    }

    private void HideMenu()
    {
        menu.SetActive(false);
    }

}
