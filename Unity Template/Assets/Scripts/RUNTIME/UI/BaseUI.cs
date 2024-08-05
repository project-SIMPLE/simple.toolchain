using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class BaseUI : MonoBehaviour
{
    [SerializeField] private Transform head;
    [SerializeField] private float spawnDistance;


    // Start is called before the first frame update
    void Start()
    {
        transform.position = head.position + new Vector3(head.forward.x, 0, head.forward.z).normalized * spawnDistance;
    }

    // Update is called once per frame
    void Update()
    {
        transform.LookAt(new Vector3(head.position.x, transform.position.y, head.position.z));
        transform.forward *= -1;
    }
}
