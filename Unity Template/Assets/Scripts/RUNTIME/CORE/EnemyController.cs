using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.AI;

public class EnemyController : MonoBehaviour
{
    private List<Transform> wayPoints;

    private int currentWayPointIndex = 0;
    private float agentStoppingDistance = .3f;

    private bool wayPointsSet = false;

    NavMeshAgent agent;

    private Enemy enemy;
    void Start()
    {
        enemy = GetComponent<Enemy>();
        agent = GetComponent<NavMeshAgent>();
    }

    void Update()
    {
        if (!wayPointsSet)
        {
            return;
        }
        if (!agent.pathPending && agent.remainingDistance <= agentStoppingDistance)
        {
            if (currentWayPointIndex == wayPoints.Count)
            {
                Destroy(this.gameObject, .1f);
            }
            else
            {
                agent.SetDestination(wayPoints[currentWayPointIndex].position);
                currentWayPointIndex++;
            }
        }
    }

    public void SetDestination(List<Transform> wayPoints)
    {
        this.wayPoints = wayPoints;
        wayPointsSet = true;
    }
    
 
}
