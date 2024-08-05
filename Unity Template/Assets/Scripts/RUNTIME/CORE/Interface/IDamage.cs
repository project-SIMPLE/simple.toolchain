using System.Collections;
using System.Collections.Generic;
using UnityEngine;


public interface IDamage
{
    float Range { get; }
    int Damage { get; }

    public bool HasValidTarget(GameObject target);
    public void DealDamage(IDamageable target);
}