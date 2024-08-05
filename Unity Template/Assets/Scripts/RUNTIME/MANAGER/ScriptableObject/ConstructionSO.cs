using System.Collections;
using System.Collections.Generic;
using UnityEngine;

[CreateAssetMenu(fileName = "Construction1", menuName = "ScriptableObjects/ConstructionSO")]
public class ConstructionSO : ScriptableObject
{
    public GameObject modelBuildPrefab;
    public GameObject finalPrefab;
    public int cost;
    [TextAreaAttribute]
    public string description;
    public int maxQuantity;
    public float cooldownTime;
    private int currentQuantity;
    private float currentTime;

    public int CurrentQuantity
    {
        get { return currentQuantity; }
    }

    public float CurrentTime
    {
        get { return currentTime; }
    }


    public void Init()
    {
        currentQuantity = maxQuantity;
        currentTime = 0;
    }

    public void ResetCooldown()
    {
        currentTime = cooldownTime;
    }

    public void DecreaseQuantity()
    {
        if(currentQuantity > 0)
        {
            currentQuantity--;
        }
    }

    public void IncreaseQuantity()
    {
        if(currentQuantity < maxQuantity)
        {
            currentQuantity++;
        }
    }

    public void DecreaseCooldown(float deltaTime)
    {
        if(currentTime > 0)
        {
            currentTime -= deltaTime;
        }
    }
}
