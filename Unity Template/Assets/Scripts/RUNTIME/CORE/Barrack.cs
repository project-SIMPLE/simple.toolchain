using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Barrack : MonoBehaviour, ISpawner, IDamageable
{
    [Header("Basic Info")]
    [SerializeField] private string uniqueName;
    [SerializeField] private int lvl;

    [Header("Stats")]
    [SerializeField] private bool freeSpawn;
    [SerializeField] private int health;

    [SerializeField] private float spawnRate;
    [SerializeField] private GameObject spawnPrefab;
    [SerializeField] private Transform spawnPoint;

    [Header("Miscellaneous")]
    [SerializeField] private float workRadius;
    [SerializeField] private LayerMask targetLayerMask;
    [SerializeField] private LayerMask spawnTriggerLayerMask;
    [SerializeField] private Animator animator;

    [Header("Subsidence Settings")]
    [SerializeField] private GameObject subsidencePrefab;
    [SerializeField] private Transform subsidenceSpawnPoint;

    // runtime privates
    private int currentHealh;
    private float currentRate;

    // Getters
    public int Health
    {
        get { return currentHealh; }
    }
    public string SpawnName
    {
        get { return spawnPrefab.name; }
    }
    public float SpawnRate
    {
        get { return spawnRate; }
    }

    void Start()
    {
        currentHealh = health;

        Collider[] nearbyTargets = Physics.OverlapSphere(transform.position, 5f, targetLayerMask);
        foreach (var target in nearbyTargets)
        {
            var health = target.GetComponent<ISpawner>();

            if (health != null)
            {
                // Debug.Log("" + target);
                if (currentHealh > 20) currentHealh -= 20;
            }
        }
        currentRate = spawnRate;
    }

    void Update()
    {
        if (!IsDead())
        {
            if (GameUI.Instance != null && GameUI.Instance.GetSocket() != null && gameObject != null)
            {
                GameUI.Instance.UpdatePlayerPosition(gameObject);
            }

            currentRate -= Time.deltaTime;
            if (currentRate <= 0)
            {
                if (CanSpawn())
                {
                    Spawn();
                    currentRate = spawnRate;
                }
            }
        }
    }


    public void TakeDamage(int damage)
    {
        currentHealh -= damage;
        if (currentHealh <= 0)
        {
            Die();
        }
    }

    public void Die()
    {
        if (subsidencePrefab)
        {
            var subsidence = Instantiate(subsidencePrefab, subsidenceSpawnPoint.position, subsidenceSpawnPoint.rotation);
        }
        RaycastHit[] hits = Physics.SphereCastAll(transform.position, workRadius, transform.forward, Mathf.Infinity, targetLayerMask);
        foreach (RaycastHit hit in hits)
        {
            Destroy(hit.transform.gameObject);
        }
        Destroy(gameObject);
    }

    public bool IsDead()
    {
        return currentHealh <= 0;
    }

    public void Spawn()
    {
        Instantiate(spawnPrefab, spawnPoint.position, spawnPoint.rotation);
        if (animator) animator.Play("Spawn");
        if (!freeSpawn) TakeDamage(1);
    }

    public bool CanSpawn()
    {
        Collider[] nearbySpawnTriggers = Physics.OverlapSphere(transform.position, workRadius, spawnTriggerLayerMask);
        return nearbySpawnTriggers.Length > 0;
    }

    void OnDrawGizmosSelected()
    {
        Gizmos.color = Color.red;
        //draw disable area
        Gizmos.DrawWireSphere(transform.position, workRadius);
    }

}
