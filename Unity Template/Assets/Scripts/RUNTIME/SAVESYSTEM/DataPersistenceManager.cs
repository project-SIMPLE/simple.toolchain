using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System.Linq;


public class DataPersistenceManager : MonoBehaviour
{
    public string fileName;
    public GameData gameData;
    private FileDataHandler fileDataHandler;
    public static DataPersistenceManager instance { get; private set; }

    private void Start()
    {
        this.fileDataHandler = new FileDataHandler(Application.persistentDataPath, fileName);
        Debug.Log(Application.persistentDataPath);
        LoadGame();
    }

    private void OnApplicationQuit()
    {
        SaveGame();
    }

    public void NewGame()
    {
        this.gameData = new GameData();
    }

    private void LoadGame()
    {
        gameData = fileDataHandler.Load();
        if (gameData == null)
        {
            Debug.Log("no data was found. Initializing data to defaults.");
            NewGame();
        }
        Debug.Log("load highscores");
    }

    private void SaveGame()
    {
        fileDataHandler.Save(gameData);
    }


    public int GetHighscore()
    {
        return gameData.highscore;
    }

    public bool CheckHighscore(int score)
    {
        if (score > gameData.highscore)
            return true;
        return false;
    }

    public void SaveHighscore(int score)
    {
        if (score > gameData.highscore)
        {
            gameData.highscore = score;
            SaveGame();
        }
    }
}
