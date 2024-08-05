using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class SluiceGateBehavior : MonoBehaviour
{
    [SerializeField] private int damage;
    [SerializeField] private float damageInterval = 1.0f;
    [SerializeField] private LayerMask enemyLayer;
    private float lastDamageTime = 0f;

    
    private void Damage(Collider target)
    {
        Enemy e = target.GetComponent<Enemy>();
        e.TakeDamage(damage);
    }

    void Start()
    {
        
    }

    // Update is called once per frame
    void Update()
    {
        if (Time.time - lastDamageTime >= damageInterval)
        {
            lastDamageTime = Time.time;

            Collider[] colliders = Physics.OverlapBox(transform.position, transform.localScale / 2, transform.rotation);

            foreach (Collider collider in colliders)
            {
                if (enemyLayer == (enemyLayer | (1 << collider.gameObject.layer)))
                {
                    Damage(collider);
                }
            }
        }
    }
}
