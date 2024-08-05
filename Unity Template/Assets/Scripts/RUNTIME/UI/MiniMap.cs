using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class MiniMap : MonoBehaviour
{
    [SerializeField] private Transform target;
    [SerializeField] private float offsetY;
    [SerializeField] private bool PlayerPos;
    void Start()
    {
        
    }

    // Update is called once per frame
    void LateUpdate()
    {
        transform.position = new Vector3(target.position.x, target.position.y + offsetY, target.position.z);
        if(PlayerPos)
        {
            transform.rotation = Quaternion.Euler(transform.rotation.eulerAngles.x, target.rotation.eulerAngles.y, transform.rotation.eulerAngles.z);
        }
    }
}
