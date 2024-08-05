using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class GateUI : MonoBehaviour
{
    private Gate gate;
    private Animator animator;

    // Start is called before the first frame update
    void Start()
    {
        gate = GetComponent<Gate>();
        animator = GetComponentInParent<Animator>();
    }

    // Update is called once per frame
    void Update()
    {
        var  renderer = gameObject.GetComponent<Renderer>();
        if (gate.Active)
        {
            //renderer.material.SetColor("_BaseColor", Color.green);
            if (animator) animator.SetBool("CloseGate",true);
        }   
        else
        {
            //renderer.material.SetColor("_BaseColor", Color.red);
            if (animator) animator.SetBool("CloseGate",false);
        } 
    }
}
