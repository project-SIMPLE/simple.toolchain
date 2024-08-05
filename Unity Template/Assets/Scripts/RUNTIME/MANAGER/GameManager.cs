using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;

public enum GameStatus 
{
    Wait,
    InProgress,
    Lose,
    Win,
    Tutorial
}

public class GameManager : MonoBehaviour
{
    [SerializeField] private BuildSystemManager buildSystemManager;
    [SerializeField] private PlayerResourcesManager playerResourcesManager;
    [SerializeField] private LevelManager levelManager;
    [SerializeField] private TutorialManager tutorialManager;
    

    [SerializeField] private BuildUI buildUI;
    [SerializeField] private HUD hud;

    // runtime privates
    private GameStatus gameStatus;
    private int score;

    // Getters
    public int Score {
        get { return score; }
    }

    void Start()
    {
        gameStatus = GameStatus.Wait;
    }

    void Update()
    {
        if (gameStatus == GameStatus.InProgress)
        {
            // Win Condition
            if (levelManager.Finished)
            {
                gameStatus = GameStatus.Win;
                Debug.Log("WINNNNNNNNN");
                EndLevel();
            }
            // Losse Condition
            if (playerResourcesManager.CurrentRefillSources <= 0)
            {
                gameStatus = GameStatus.Lose;
                Debug.Log("LOSEEEE");
                EndLevel();
            }
        }
    }

    void UpdateScore()
    {
        score = 100 * playerResourcesManager.CurrentRefillSources;
    }

    public GameStatus CurrentGameStatus()
    {
        return gameStatus;
    }

    public void StartLevel()
    {
        buildSystemManager.gameObject.SetActive(true);
        playerResourcesManager.gameObject.SetActive(true);
        levelManager.gameObject.SetActive(true);
        buildUI.gameObject.SetActive(true);
        hud.gameObject.SetActive(true);
        gameStatus = GameStatus.InProgress;
    }

    public void StartTutorial()
    {
        buildSystemManager.gameObject.SetActive(true);
        levelManager.gameObject.SetActive(true);
        levelManager.ToggleLoop();
        buildUI.gameObject.SetActive(true);
        tutorialManager.gameObject.SetActive(true);
        gameStatus = GameStatus.Tutorial;
    }

    // Update is called once per frame
    public void RestartLevel()
    {
        SceneManager.LoadScene( SceneManager.GetActiveScene().name);
    }

    void EndLevel()
    {
        UpdateScore();
        Destroy(buildSystemManager.gameObject);
        Destroy(playerResourcesManager.gameObject);
        Destroy(levelManager.gameObject);
        Destroy(buildUI.gameObject);
    }

}

