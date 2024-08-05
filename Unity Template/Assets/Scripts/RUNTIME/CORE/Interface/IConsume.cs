using System.Collections;
using System.Collections.Generic;
using UnityEngine;


public interface IConsume
{
    float energy { get; }
    float costPerUse { get; }
    float currentEnergy { get; }

    public bool IsOutOfEnergy();
    public bool ConsumeEnergy(float amount);
}
