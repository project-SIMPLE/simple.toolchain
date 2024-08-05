using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class PlayerResourcesManager : MonoBehaviour, ISupply
{
    [Header("Stats")]
    [SerializeField] private int initialAmount = 0;
    [SerializeField] private float workRadius = 10f;
    [SerializeField] private float refillInterval = 2f;
    [SerializeField] private int refillAmount = 1;

    [Header("Miscellaneous")]
    [SerializeField] private LayerMask targetLayerMask;


    // runtime privates
    private int currentAmount;
    private float currentInterval;
    private int currentRefillSources;
    private int totalTree;
    // Getters
    public int CurrentAmount { 
        get { return currentAmount; }
    }
    public float WorkRadius { 
        get { return workRadius; }
    }
    public float RefillInterval { 
        get { return refillInterval; }
    }
    public int RefillAmount { 
        get { return refillAmount; }
    }
    public int CurrentRefillSources {
        get { return currentRefillSources; }
    }
    public int TotalTree { 
        get { return totalTree; } 
    }

    void Awake()
    {
        currentAmount = initialAmount;
        currentInterval = refillInterval;
        currentRefillSources = 1;
        InvokeRepeating("CheckRefillSources", 0, .5f);
        GetTotalTree();
    }
    void GetTotalTree()
    {
        CheckRefillSources();
        totalTree = currentRefillSources;
    }
    void Update()
    {
        currentInterval -= Time.deltaTime;
        if (currentInterval <= 0)
        {
            Refill();
            currentInterval = refillInterval;
        }
        
    }
    
    void CheckRefillSources()
    {
        Collider[] nearbyTargets = Physics.OverlapSphere(transform.position, workRadius, targetLayerMask);
        currentRefillSources = nearbyTargets.Length;
    }


    // for subtract resoucres when build a construction
    public bool Supply(int amount)
    {
        if (currentAmount >= amount)
        {
            currentAmount -= amount;
            return true;
        }
        return false;
    }

    public void Refill()
    {
        currentAmount += refillAmount * currentRefillSources;
    }

    public bool IsEmpty()
    {
        return currentAmount <= 0;
    }

    void OnDrawGizmosSelected()
    {
        Gizmos.color = Color.blue;
        //draw disable area
        Gizmos.DrawWireSphere(transform.position, workRadius);
    }

}
