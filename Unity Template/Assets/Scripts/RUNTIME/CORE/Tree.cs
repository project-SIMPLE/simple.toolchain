using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Tree : MonoBehaviour, IDamageable
{
    [Header("Stats")]
    [SerializeField] private int health = 2;

    // runtime privates
    private int currentHealh;

    // Getters
    public int Health
    {
        get { return currentHealh; }
    }

    void Start()
    {
        currentHealh = health;
    }
    private bool created = false;
    void Update()
    {
        if (!created && GameUI.Instance != null && GameUI.Instance.GetSocket() != null && gameObject != null)
        {
            GameUI.Instance.UpdatePlayerPosition(gameObject);
            created = true;
        }
    }

    public void TakeDamage(int damage)
    {
        currentHealh -= damage;
        if (currentHealh <= 0)
        {
            if (GameUI.Instance != null && GameUI.Instance.GetSocket() != null && gameObject != null)
            {
                GameUI.Instance.DeletePlayer(gameObject);
            }
            Die();
        }
    }

    public void Die()
    {
        Destroy(gameObject);
    }

    public bool IsDead()
    {
        return currentHealh <= 0;
    }
}
