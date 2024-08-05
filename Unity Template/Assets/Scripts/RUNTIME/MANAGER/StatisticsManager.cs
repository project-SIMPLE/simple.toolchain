using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class StatisticsManager : MonoBehaviour
{
    public static StatisticsManager Instance = null;

    private int currentLakeCount = 0;
    private int currentWaterPumpCount = 0;
    private int currentSluiceGateCount = 0;
    private int currentEnemyCount = 0;

    private void Awake()
    {
        Instance = this;
    }

    public void IncreateLakeCount() 
    {
        currentLakeCount += 1;
    }

    public void IncreateWaterPumpCount()
    {
        currentWaterPumpCount += 1;
    }
    public void IncreateSluiceGateCount()
    {
        currentSluiceGateCount += 1;
    }

    public void IncreaseEnemyKillCount()
    {
        currentEnemyCount += 1;
    }


    //Getter
    public int LakeCount
    {
        get { return currentLakeCount; }
    }

    public int WaterPumpCount
    {
        get { return currentLakeCount; }
    }

    public int SluiceGateCount
    {
        get { return currentLakeCount; }
    }
    public int EnemyKillCount
    { 
        get { return currentEnemyCount; } 
    }
}
