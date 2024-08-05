using System.Collections;
using System.Collections.Generic;
using TMPro;
using UnityEngine;

public class Tower : MonoBehaviour
{
    //tag name
    public string enemyTag = "Enemy";
    public string waterPumpTag = "WaterPump";

    //water pump and lake
    public Transform target;
    public float range = 15f;

    public GameObject waterSoldierPrefab;
    public Transform spawnPoint;
    private float spawnRate = 5.0f;
    private float count = 0f;

    //water pump
    public TextMeshProUGUI textHP;
    public int Health = 1;
    public float sphereRadius;
    public float maxDistance;
    public LayerMask layerMask;
    private bool isvoid = false;

    // Start is called before the first frame update
    void Start()
    {
        InvokeRepeating("UpdateTarget", 0f, .5f);
    }

    // Update is called once per frame
    void Update()
    {
        if (isvoid)
        {
            return;
        }
        count += Time.deltaTime;
        if (this.CompareTag(waterPumpTag))
        {
            textHP.text = Health.ToString();
        }
        if (target == null)
        {
            return;
        }
        if (this.CompareTag(waterPumpTag) && Health == 0 && !isvoid)
        {
            isvoid = true;
            RaycastHit[] hits = Physics.SphereCastAll(transform.position, sphereRadius, transform.forward, maxDistance, layerMask, QueryTriggerInteraction.UseGlobal);
            foreach(RaycastHit hit in hits)
            {
                Debug.Log(hit.transform.name);

                Destroy(hit.transform.gameObject);
            }
            Debug.Log("Die");
            return;
        }
        if(count >= spawnRate)
        {
            Debug.Log("Spawn" + count);
            count = 0;
            SpawnSoldier();
        }
        
    }

    public void SpawnSoldier()
    {
        Instantiate(waterSoldierPrefab, spawnPoint.position, spawnPoint.rotation);
        if (this.CompareTag(waterPumpTag))
        {
            Health--;
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
    void OnDrawGizmosSelected()
    {
        Gizmos.color = Color.red;
        //draw target area
        Gizmos.DrawWireSphere(transform.position, range);

        //draw disable area
        Gizmos.DrawWireSphere(transform.position, sphereRadius);
    }
}
