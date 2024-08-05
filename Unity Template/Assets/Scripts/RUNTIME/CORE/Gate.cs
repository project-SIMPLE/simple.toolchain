using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Gate : MonoBehaviour, IDamage
{
    [Header("Basic Info")]
    [SerializeField] private string uniqueName;
    [SerializeField] private int lvl;

    [Header("Stats")]
    [SerializeField] private int attackDamage = 1;
    [SerializeField] private Vector3 attackBox;
    [SerializeField] private int attackInterval = 1;
    [SerializeField] private float attackTime = 1.0f;

    [Header("Miscellaneous")]
    [SerializeField] private LayerMask targetLayerMask;


    // runtime privates
    private float currentInterval = 0f;
    private float currentATime = 0f;
    private bool isActive = false;


    // Getters
    public float Range
    {
        get { return 0; }
    }
    public int Damage
    {
        get { return attackDamage; }
    }
    public bool Active
    {
        get { return isActive; }
    }

    void Start()
    {

        currentInterval = attackInterval;
    }

    void Update()
    {
        if (GameUI.Instance != null && GameUI.Instance.GetSocket() != null && gameObject != null)
        {
            GameUI.Instance.UpdatePlayerPosition(gameObject);
        }
        if (isActive)
        {
            Block();
        }
        else
        {
            currentInterval -= Time.deltaTime;
            if (currentInterval <= 0)
            {

                isActive = true;
                currentInterval = attackInterval;
                currentATime = attackTime;
            }
        }
    }

    public void Block()
    {
        currentATime -= Time.deltaTime;
        if (currentATime <= 0)
        {
            isActive = false;
            return;
        }

        Collider[] colliders = Physics.OverlapBox(transform.position, attackBox, transform.rotation);
        foreach (Collider collider in colliders)
        {
            if (HasValidTarget(collider.gameObject))
            {
                var target = collider.gameObject.GetComponent<IDamageable>();
                if (target != null)
                {
                    DealDamage(target);
                    if (target.IsDead()) Destroy(collider.gameObject);
                }

            }
        }
    }

    public bool HasValidTarget(GameObject target)
    {
        return (targetLayerMask == (targetLayerMask | (1 << target.layer)));
    }

    public void DealDamage(IDamageable target)
    {
        target.TakeDamage(attackDamage);
    }

    void OnDrawGizmos()
    {
        Gizmos.color = Color.red;
        //Check that it is being run in Play Mode, so it doesn't try to draw this in Editor mode
            //Draw a cube where the OverlapBox is (positioned where your GameObject is as well as a size)
            Gizmos.DrawWireCube(transform.position,attackBox);
    }

}
