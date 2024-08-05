
using UnityEngine;
using UnityEngine.SceneManagement;
using TMPro;
using UnityEngine.UI;

public class EndofGameController : MonoBehaviour
{
    TextMeshProUGUI textMP;

    void Start()
    {
        TextMeshProUGUI textPN = GameObject.FindGameObjectWithTag("textPN").GetComponent<TextMeshProUGUI>();
        textPN.text = "Player id: " + StaticInformation.getId();

        textMP = GameObject.FindGameObjectWithTag("textIP").GetComponent<TextMeshProUGUI>();
        textMP.text = StaticInformation.endOfGame;
       
    }

    public void ResetBtn()
    {
        SceneManager.LoadScene("Startup Menu");
    }


}
