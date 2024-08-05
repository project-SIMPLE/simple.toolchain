using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using TMPro;
using UnityEngine.UI;

public class HUD : MonoBehaviour
{
    [SerializeField] private PlayerResourcesManager playerResourcesManager;
    [SerializeField] private LevelManager levelManager;

    [SerializeField] private Image timeBar;
    [SerializeField] private TextMeshProUGUI time;
    [SerializeField] private TextMeshProUGUI wave;
    [SerializeField] private TextMeshProUGUI step;
    [SerializeField] private TextMeshProUGUI resoucre;

    [SerializeField] private string waveText;
    [SerializeField] private string[] waveStepTexts;

    
    // Update is called once per frame
    void Update()
    {
        UpdateLevelUI();
        UpdateResourcesUI();
    }

    void UpdateLevelUI()
    {
        if (!levelManager) return;

        if (levelManager.Finished)
        {
            time.text = "FINISHED";
        }
        else
        {
            timeBar.fillAmount = (levelManager.CurrentWaveStepTime - levelManager.CurrentTime) / levelManager.CurrentWaveStepTime;

            int minutes = Mathf.FloorToInt(levelManager.CurrentTime / 60F);
            int seconds = Mathf.FloorToInt(levelManager.CurrentTime - minutes * 60);
            string niceTime = string.Format("{0:0}:{1:00}", minutes, seconds);
            time.text = niceTime;
        }
        wave.text = waveText + levelManager.CurrentWave + "/" + levelManager.MaxWave;
        switch (levelManager.CurrentWaveStep)
        {
            case WaveStep.Preparation:
                step.text = waveStepTexts[0];
                break;
            case WaveStep.Defense:
                step.text = waveStepTexts[1];
                break;
        }
    }

    void UpdateResourcesUI()
    {
        if (playerResourcesManager)
            resoucre.text = playerResourcesManager.CurrentAmount.ToString();
    }
}
