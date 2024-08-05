using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class GrabPhysic : MonoBehaviour
{
    public Transform handler;
    Rigidbody rb;
    // Start is called before the first frame update
    void Start()
    {
        rb = GetComponent<Rigidbody>();
    }

    // Update is called once per frame
    void Update()
    {
        
    }

    public void StopRigid()
    {
        transform.position = handler.transform.position;
        transform.rotation = handler.transform.rotation;

        Rigidbody rbHandler = handler.GetComponent<Rigidbody>();
        rbHandler.velocity = Vector3.zero;
        rbHandler.angularVelocity = Vector3.zero;
    }
}
