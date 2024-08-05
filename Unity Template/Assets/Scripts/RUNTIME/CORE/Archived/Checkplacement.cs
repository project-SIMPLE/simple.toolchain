using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Checkplacement : MonoBehaviour
{
    BuildSystem buildSystem;
    int collidingObjects = 0;
    // Start is called before the first frame update
    void Start()
    {
        buildSystem = GameObject.Find("Building System").GetComponent<BuildSystem>();
    }

    private void OnTriggerEnter(Collider other)
    {
        //Debug.Log(other.gameObject.name);
        if(other.gameObject.CompareTag("Construction"))
        {
            Debug.Log("cant place");
            collidingObjects++;
            Debug.Log("Enter collidingObjects: " + collidingObjects);
            buildSystem.canPlace = false;
        }
    }

    private void OnTriggerExit(Collider other)
    {
        if (other.gameObject.CompareTag("Construction"))
        {
            collidingObjects--;
            Debug.Log("Exit collidingObjects: " + collidingObjects);

            if (collidingObjects <= 0) 
            {
                buildSystem.canPlace = true;
            }
        }
    }
}
