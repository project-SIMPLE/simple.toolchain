using System.Collections;
using System.Collections.Generic;
using UnityEngine;


public enum WaveStep
{
    Preparation,
    Defense
}

public class LevelManager : MonoBehaviour
{
    [SerializeField] private bool loop = false;
    [SerializeField] private List<WaveSO> waves;
    [SerializeField] private List<EnemySpawner> spawns;

    // runtime privates
    private bool finished = false;
    private WaveStep currentWaveStep;
    private int currentWave;
    private float currentTime;
    private int currentWaveSpawnIndex;

    // Getters
    public bool Finished
    {
        get { return finished; }
    }
    public int CurrentWave
    {
        get { return currentWave + 1; }
    }
    public int MaxWave
    {
        get { return waves.Count; }
    }
    public float CurrentTime
    {
        get { return currentTime; }
    }
    public WaveStep CurrentWaveStep
    {
        get { return currentWaveStep; }
    }

    public float CurrentWaveStepTime
    {
        get
        {
            if (currentWaveStep == WaveStep.Preparation)
                return waves[currentWave].preparationTime;
            else
                return waves[currentWave].waveTime;
        }
    }

    public void ToggleLoop()
    {
        loop = !loop;
    }

    void Awake()
    {
        InitLevel();
    }

    void Update()
    {
        if (finished) return;


        if (currentTime == waves[currentWave].preparationTime && GameUI.Instance != null)
        {
            if (currentWave > 0)
            {
                GameUI.Instance.DeletePlayer(spawns[0].gameObject);
            }

            for (int i = currentWaveSpawnIndex; i < waves[currentWave].waveSpawns.Count; i++)
            {
                int spawnPointIndex = waves[currentWave].waveSpawns[i].spawnPoint - 1;

                GameUI.Instance.UpdatePlayerPosition(spawns[spawnPointIndex].gameObject);
            }
        }
        currentTime -= Time.deltaTime;
        if (currentTime <= 0)
        {
            if (!MoveToDefenseStep())
                MoveToNextWave();
        }
        else
        {
            ExecuteDefenseStep();
        }
    }

    void InitLevel()
    {
        currentWaveStep = WaveStep.Preparation;
        currentWave = 0;
        currentTime = waves[0].preparationTime;
        currentWaveSpawnIndex = 0;
    }

    bool MoveToDefenseStep()
    {
        if (currentWaveStep == WaveStep.Preparation)
        {
            currentTime = waves[currentWave].waveTime;
            currentWaveStep = WaveStep.Defense;
            return true;
        }
        return false;
    }

    bool MoveToNextWave()
    {
        if (currentWaveStep == WaveStep.Defense)
        {
            currentWave++;
            if (currentWave > waves.Count - 1)
            {
                if (loop) InitLevel();
                else finished = true;
            }
            else
            {
                currentWaveStep = WaveStep.Preparation;
                currentTime = waves[currentWave].preparationTime;
                currentWaveSpawnIndex = 0;
            }
            return true;
        }
        return false;
    }

    void ExecuteDefenseStep()
    {
        if (currentWaveStep != WaveStep.Defense) return;

        int start = currentWaveSpawnIndex;
        for (int i = start; i < waves[currentWave].waveSpawns.Count; i++)
        {
            float spawnTimeFrame = waves[currentWave].waveSpawns[i].timeFrame;
            if (waves[currentWave].waveTime - currentTime >= spawnTimeFrame)
            {
                int spawnPointIndex = waves[currentWave].waveSpawns[i].spawnPoint - 1;
                spawns[spawnPointIndex].StartAutoSpawn(
                    waves[currentWave].waveSpawns[i].spawnEnemy.gameObject,
                    waves[currentWave].waveSpawns[i].spawnAmount
                );
                currentWaveSpawnIndex = i + 1;
            }
            else
            {
                return;
            }
        }
    }
}
