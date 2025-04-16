
using UnityEngine;
using UnityEngine.SceneManagement;
using TMPro;
using UnityEngine.UI;

public class MenuController : MonoBehaviour
{
    [SerializeField] private string host = "127.0.0.1";
    [SerializeField] private string portWithMiddleware = "8080";
    [SerializeField] private string portWithoutMiddleware = "1000";
    private bool useMiddleWare;
    TextMeshProUGUI textMP;
    Toggle m_Toggle;

    public void Start()
    {

        textMP = GameObject.FindGameObjectWithTag("textIP").GetComponent<TextMeshProUGUI>();
        m_Toggle = GameObject.FindGameObjectWithTag("useMiddleWare").GetComponent<Toggle>();
        GameObject ob = GameObject.FindGameObjectWithTag("textPN");
        TextMeshProUGUI textPN = ob.GetComponent<TextMeshProUGUI>();
        textPN.text = "Player id: " + StaticInformation.getId();
       
        if (!PlayerPrefs.HasKey("MIDDLEWARE") || PlayerPrefs.GetString("MIDDLEWARE").Length == 0)
            PlayerPrefs.SetString("MIDDLEWARE", "N");
        useMiddleWare = PlayerPrefs.GetString("MIDDLEWARE").Equals("Y");
        m_Toggle.SetIsOnWithoutNotify(useMiddleWare);

        string port = useMiddleWare ? portWithMiddleware : portWithoutMiddleware;
       
        string ip = PlayerPrefs.GetString("IP");
        if (ip.Length == 0)
        {
            ip = host;
            PlayerPrefs.SetString("IP", ip);
        }
        textMP.text = "Current IP: " + ip + "/" + port;

        

    }

    public void OnValueMiddleWare()
    {
        useMiddleWare = m_Toggle.isOn;
        string port = useMiddleWare ? portWithMiddleware : portWithoutMiddleware;
        textMP.text = "Current IP: " + PlayerPrefs.GetString("IP") + ":" + port;

    }
    public void StartBtn()
    {
        PlayerPrefs.SetString("MIDDLEWARE", useMiddleWare ? "Y" : "N") ;
        string port = useMiddleWare ? portWithMiddleware : portWithoutMiddleware;
        PlayerPrefs.SetString("PORT", port);
        SceneManager.LoadScene("Main Scene");
    }

    public void IPBtn()
    {
        SceneManager.LoadScene("IP Menu");
    }
}
