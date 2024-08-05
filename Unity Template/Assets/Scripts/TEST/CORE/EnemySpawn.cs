using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class EnemySpawn : MonoBehaviour
{
    public GameObject enemyPrefab;
    public float sapwnRate = 1.0f;

    public List<Transform> wayPoints;

    public int maxCount = 10;
    private int count = 0;
    // void Start()
    // {
    //     InvokeRepeating("Spawn", .5f, sapwnRate);
    // }

    void Spawn()
    {
        GameObject enemy = Instantiate(enemyPrefab, transform.position, enemyPrefab.transform.rotation);
        enemy.GetComponent<EnemyController>().SetDestination(wayPoints);

        count++;
        if(count >= maxCount)
        {
            CancelInvoke();
        }
    }

    public void SpawnTest()
    {
        count = 0;
        InvokeRepeating("Spawn", .5f, sapwnRate);
    }
}
