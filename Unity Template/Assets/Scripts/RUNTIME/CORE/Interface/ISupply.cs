using System.Collections;
using System.Collections.Generic;
using UnityEngine;


public interface ISupply
{
    int CurrentAmount { get; }
    float WorkRadius { get; }

    float RefillInterval { get; }
    int RefillAmount { get; }

    
    public bool Supply(int amount);
    public void Refill();
    public bool IsEmpty();
}
