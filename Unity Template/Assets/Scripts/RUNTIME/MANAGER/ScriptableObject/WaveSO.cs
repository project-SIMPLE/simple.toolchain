using System.Collections;
using System.Collections.Generic;
using UnityEngine;


[System.Serializable]
public class WaveEnemySpawn
{
    public float timeFrame;
    public Enemy spawnEnemy;
    public int spawnAmount;
    public int spawnPoint;
}


[CreateAssetMenu(fileName = "Wave1", menuName = "ScriptableObjects/WaveSO")]
public class WaveSO : ScriptableObject
{
    public List<WaveEnemySpawn> waveSpawns;
    public float waveTime;
    public float preparationTime;

}
