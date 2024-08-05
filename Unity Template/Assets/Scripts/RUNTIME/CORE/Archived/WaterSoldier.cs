using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.AI;

public class WaterSoldier : MonoBehaviour
{
    public string enemyTag = "Enemy";

    public Transform target;

    public float range = 30f;
    public int damage = 1;
    NavMeshAgent agent;


    private void OnTriggerEnter(Collider other)
    {

        if (other.CompareTag(enemyTag))
        {
            Enemy enemy = other.GetComponent<Enemy>();
            enemy.TakeDamage(damage);
            Die();
        }
    }
    void UpdateTarget()
    {
        GameObject[] enemies = GameObject.FindGameObjectsWithTag(enemyTag);
        float shortesDistance = Mathf.Infinity;
        GameObject nearesEnemy = null;
        foreach (GameObject enemy in enemies)
        {
            float distanceToEnemy = Vector3.Distance(transform.position, enemy.transform.position);
            if (distanceToEnemy < shortesDistance)
            {
                shortesDistance = distanceToEnemy;
                nearesEnemy = enemy;
            }
        }

        if (nearesEnemy != null && shortesDistance <= range)
        {
            target = nearesEnemy.transform;
        }
    }
    void Start()
    {
        agent = GetComponent<NavMeshAgent>();
        InvokeRepeating("UpdateTarget", 0f, .5f);
    }

    void Update()
    {
        if(target!= null)
        {
            agent.SetDestination(target.position);
        }
    }

    
    void Die()
    {
        Destroy(gameObject);
    }
}
