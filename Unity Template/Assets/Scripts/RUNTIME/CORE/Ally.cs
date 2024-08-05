using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Ally : MonoBehaviour, IDamageable, IDamage
{
    [Header("Basic Info")]
    [SerializeField] private string uniqueName;
    [SerializeField] private int lvl;
    
    [Header("Stats")]
    [SerializeField] private int health = 2;
    [SerializeField] private float moveSpeed = 2f;
    [SerializeField] private float detectRange = 30f;
    [SerializeField] private int attackDamage = 1;

    [Header("Miscellaneous")]
    [SerializeField] private LayerMask targetLayerMask;
    [SerializeField] private Animator animator;

    // runtime privates
    private int currentHealh;
    private Transform target;
    private UnityEngine.AI.NavMeshAgent navAgent;

    // Getters 
    public int Health { 
        get { return currentHealh; } 
    }
    public float Range { 
        get { return detectRange; }
    }
    public int Damage { 
        get { return attackDamage; } 
    }


    void Start()
    {
        currentHealh = health;
        navAgent = GetComponent<UnityEngine.AI.NavMeshAgent>();
        if (navAgent) navAgent.speed = moveSpeed;
        InvokeRepeating("FindTarget", 0f, .5f);
    }

    void Update()
    {
        if(target)
        {
            navAgent.enabled = true;
            navAgent.SetDestination(target.position);
        }
        else
        {
            navAgent.enabled = false;
        }  
    }
    

    void OnTriggerEnter(Collider other)
    {
        if (HasValidTarget(other.gameObject) && !IsDead())
        {
            Enemy enemy = other.GetComponent<Enemy>();
            enemy.TakeDamage(attackDamage);
            TakeDamage(1);
        }
    }

    void FindTarget()
    {
        Collider[] nearbyTargets = Physics.OverlapSphere(transform.position, detectRange, targetLayerMask);

        float closestDistance = Mathf.Infinity;
        GameObject closestTarget = null;

        if (nearbyTargets.Length <= 0)
        {
            target = null;
            return;
        } 

        foreach (Collider target in nearbyTargets)
        {
            float targetDistance = Vector3.Distance(transform.position, target.transform.position);
            if (targetDistance < closestDistance)
            {
                closestDistance = targetDistance;
                closestTarget = target.gameObject;
            }
        }

        if (closestTarget)
            target = closestTarget.transform;
    }

    public void TakeDamage(int damage)
    {
        currentHealh -= damage;
        if(currentHealh <= 0)
        {
            Die();
        }
    }

    public void Die()
    {
        //navAgent.enabled = false;
        if (animator) animator.Play("Disappear");
        Destroy(gameObject,2f);
    }

    public bool IsDead()
    {
        return currentHealh <= 0;
    }

    public bool HasValidTarget(GameObject target)
    {
        return (targetLayerMask == (targetLayerMask | (1 << target.layer)));
    }

    public void DealDamage(IDamageable target)
    {
        target.TakeDamage(attackDamage);
    }
}
