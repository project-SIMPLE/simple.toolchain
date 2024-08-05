using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Turret : MonoBehaviour
{
    public Transform target;

    public float range = 15f;
    public float energyCostPerAtk = 1f;
    private float currentEnergy = 0f;

    public string enemyTag = "Enemy";

    public Transform partToRotate;

    public GameObject bulletPrefab;
    public Transform firePoint;
    void Start()
    {
        InvokeRepeating("UpdateTarget", 0f, .5f);
    }

    void UpdateTarget()
    {
        GameObject[] enemies = GameObject.FindGameObjectsWithTag(enemyTag);
        float shortesDistance = Mathf.Infinity;
        GameObject nearesEnemy = null;
        foreach(GameObject enemy in enemies)
        {
            float distanceToEnemy = Vector3.Distance(transform.position, enemy.transform.position);
            if(distanceToEnemy < shortesDistance)
            {
                shortesDistance = distanceToEnemy;
                nearesEnemy = enemy;
            }
        }

        if(nearesEnemy != null && shortesDistance <= range)
        {
            target = nearesEnemy.transform;
        }
    }
    // Update is called once per frame
    void Update()
    {
        if(target == null)
        {
            return;
        }
        Vector3 dir = target.position - transform.position;
        Quaternion lookRotation = Quaternion.LookRotation(dir);
        Vector3 rotation = lookRotation.eulerAngles;
        partToRotate.rotation = Quaternion.Euler(0f, rotation.y, 0f);

        if (currentEnergy <= 0)
        {
            Shoot();
            currentEnergy = energyCostPerAtk;
        }
        currentEnergy -= Time.deltaTime;

    }
    void Shoot()
    {
        GameObject bulletGO = (GameObject)Instantiate(bulletPrefab, firePoint.position, firePoint.rotation);
        Bullet bullet= bulletGO.GetComponent<Bullet>();

        if(bullet != null)
        {
            bullet.Seek(target);
        }
    }
    void OnDrawGizmosSelected()
    {
        Gizmos.color = Color.red;
        Gizmos.DrawWireSphere(transform.position, range);
    }
}
