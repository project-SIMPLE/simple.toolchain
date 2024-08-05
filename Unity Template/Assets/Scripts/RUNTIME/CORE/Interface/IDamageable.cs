using System.Collections;
using System.Collections.Generic;
using UnityEngine;


public interface IDamageable
{
    int Health { get; }

    public void TakeDamage(int damage);
    public void Die();
    public bool IsDead();
}