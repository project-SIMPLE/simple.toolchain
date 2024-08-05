using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
using TMPro;
using WebSocketSharp;
using Newtonsoft.Json.Linq;
using Newtonsoft.Json;

public class GameUI : MonoBehaviour
{
    [SerializeField] private GameManager gameManager;
    [SerializeField] private TutorialManager tutorialManager;
    [SerializeField] private Transform head;
    [SerializeField] private float spawnDistance;

    [SerializeField] private GameObject startContent;
    [SerializeField] private GameObject finalContent;

    [Header("Text")]
    [SerializeField] private TextMeshProUGUI finalText;
    [SerializeField] private string winText = "YOU WON!!!";
    [SerializeField] private string loseText = "YOU LOST!!!";

    [SerializeField] private PlayerResourcesManager playerResourcesManager;
    private SubsidenceManager subsidenceManager;


    private WebSocket socket;
    private bool connected = false;
    public static GameUI Instance = null;

    private string reportText = "";
    [SerializeField] private TextMeshProUGUI reportTextMeshPro;

    void Start()
    {
        string ip = PlayerPrefs.GetString("IP");
        if (NotValid(ip))
            ip = "127.0.0.1";

        playerTextOutput = GameObject.FindGameObjectWithTag("textIP").GetComponentInChildren<TextMeshProUGUI>();

        playerTextOutput.text = ip;
        ready = false;
        transform.position = head.position + new Vector3(head.forward.x, 0, head.forward.z).normalized * spawnDistance;
        startContent.SetActive(true);
        finalContent.SetActive(false);
        Instance = this;
    }
    void Awake()
    {
        Instance = this;
        subsidenceManager = FindObjectOfType<SubsidenceManager>();
    }
    void Update()
    {
        ready = true;
        if (gameManager.CurrentGameStatus() == GameStatus.Win || gameManager.CurrentGameStatus() == GameStatus.Lose)
        {
            transform.position = head.position + new Vector3(head.forward.x, 0, head.forward.z).normalized * spawnDistance;
            startContent.SetActive(false);
            finalContent.SetActive(true);

            if (gameManager.CurrentGameStatus() == GameStatus.Win)
            {
                finalText.text = winText;
            }
            if (gameManager.CurrentGameStatus() == GameStatus.Lose)
            {
                finalText.text = loseText;
            }
            // Add Report Text Here
            reportText = "Living Trees: " + playerResourcesManager.CurrentRefillSources + "\n" +
                         "Dead Trees: " + (playerResourcesManager.TotalTree - playerResourcesManager.CurrentRefillSources) + "\n" +
                         "Lake Structures Built: " + StatisticsManager.Instance.LakeCount + "\n" +
                         "WaterPump Structures Built: " + StatisticsManager.Instance.WaterPumpCount + "\n" +
                         "SluiceGate Structures Built: " + StatisticsManager.Instance.SluiceGateCount + "\n" +
                         "Enemies Neutralized: " + StatisticsManager.Instance.EnemyKillCount + "\n" +
                         "Remaining Groundwater Level (Local): " + subsidenceManager.RemainingWaterLevelLocal + "\n" +
                         "Remaining Groundwater Level (Global): " + subsidenceManager.RemainingWaterLevelGlobal + "\n" +
                         "Subsidence Score: " + subsidenceManager.SubsidenceScore;

            reportTextMeshPro.text = reportText;
        }

        transform.LookAt(new Vector3(head.position.x, transform.position.y, head.position.z));
        transform.forward *= -1;
    }

    public void StartTutorialUI()
    {
        gameManager.StartTutorial();
        startContent.gameObject.SetActive(false);
    }

    public void StartUI()
    {
        // PlayerPrefs.SetString("IP", "localhost");
        PlayerPrefs.SetString("PORT", "1000");
        PlayerPrefs.SetString("IP", playerTextOutput.text);
        PlayerPrefs.Save();

        port = PlayerPrefs.GetString("PORT");
        host = PlayerPrefs.GetString("IP");
        socket = new WebSocket("ws://" + host + ":" + port + "/");
        socket.OnOpen += HandleConnectionOpen;
        socket.Connect();

        gameManager.StartLevel();
        startContent.SetActive(false);
    }

    public void RetryUI()
    {
        Restart();
        gameManager.RestartLevel();
    }

    public List<float> toGAMACRS3D(Vector3 pos)
    {
        List<float> position = new List<float>();
        // position.Add((int)((pos.x - GamaCRSOffsetX) / GamaCRSCoefX * precision));
        // position.Add((int)((pos.z - GamaCRSOffsetY) / GamaCRSCoefY * precision));
        // position.Add((int)((pos.y - GamaCRSOffsetZ) / GamaCRSCoefZ * precision));
        position.Add((float)(pos.x * precision));
        position.Add((float)(pos.z * precision));
        position.Add((float)(pos.y * precision));

        return position;
    }

    // optional: define a scale between GAMA and Unity for the location given
    public float GamaCRSCoefX = 1.0f;
    public float GamaCRSCoefY = 1.0f;
    public float GamaCRSCoefZ = 1.0f;
    public float GamaCRSOffsetX = 0.001f;
    public float GamaCRSOffsetY = 0.001f;
    public float GamaCRSOffsetZ = 0.001f;

    public int precision = 1;


    public void UpdatePlayer()
    {
        Vector2 vF = new Vector2(Camera.main.transform.forward.x, Camera.main.transform.forward.z);
        Vector2 vR = new Vector2(transform.forward.x, transform.forward.z);
        vF.Normalize();
        vR.Normalize();
        float c = vF.x * vR.x + vF.y * vR.y;
        float s = vF.x * vR.y - vF.y * vR.x;

        int angle = (int)(((s > 0) ? -1.0 : 1.0) * (180 / Math.PI) * Math.Acos(c) * 1);
        List<float> p = toGAMACRS3D(Camera.main.transform.position);

        Dictionary<string, string> args = new Dictionary<string, string> {
            {"id",  ("\"mainPlayer\"") },
            {"x", "" +p[0]},
            {"y", "" +p[1]},
            {"z", "" +p[2]},
            {"angle", "" +angle}
        };
        SendExecutableAsk("simulation[0]", "move_player_main", args);
        // ConnectionManager.Instance.SendExecutableExpression("do move_player_external($id," + p[0] + "," + p[1] + "," + angle + ");");

    }
    void FixedUpdate()
    {
        UpdatePlayer();
    }
    public void Restart()
    {
        Dictionary<string, string> args = new Dictionary<string, string> {
            {"id",  ("\"1\"") }
        };

        Debug.Log("Restart: ");

        SendExecutableAsk("simulation[0]", "Restart", args);
    }

    public void DeletePlayer(GameObject player)
    {
        Dictionary<string, string> args = new Dictionary<string, string> {
            {"id",  ("\""+player+" "+player.GetInstanceID()+"\"") }
        };

        // Debug.Log("DeletePlayer: " + player);

        SendExecutableAsk("simulation[0]", "DeletePlayer", args);
    }
    public void UpdatePlayerPosition(GameObject player)
    {
        if (!connected || finalContent.activeSelf) return;
        Vector2 vF = new Vector2(Camera.main.transform.forward.x, Camera.main.transform.forward.z);
        Vector2 vR = new Vector2(transform.forward.x, transform.forward.z);
        vF.Normalize();
        vR.Normalize();
        float c = vF.x * vR.x + vF.y * vR.y;
        float s = vF.x * vR.y - vF.y * vR.x;
        int angle = (int)(((s > 0) ? -1.0 : 1.0) * (180 / Math.PI) * Math.Acos(c) * precision);

        List<float> p = toGAMACRS3D(player.transform.position);

        // Vector3 v = new Vector3(Camera.main.transform.position.x, player.transform.position.y, Camera.main.transform.position.z);
        // List<float> p = toGAMACRS3D(v);
        Dictionary<string, string> args = new Dictionary<string, string> {
            {"id",  ("\""+player+" "+player.GetInstanceID()+"\"") },
            {"x", "" +p[0]},
            {"y", "" +p[1]},
            {"z", "" +p[2]},
            {"angle", "" +angle}
        };

        // Debug.Log("move_player_external: " + player + " " + p[0] + "," + p[1] + "," + p[2]);

        SendExecutableAsk("simulation[0]", "move_player_external", args);
    }
    protected string host;
    protected string port;
    private static bool ready = false;
    private TextMeshProUGUI playerTextOutput;

    private static bool NotValid(string ip)
    {
        if (ip == null || ip.Length == 0) return false;
        string[] ipb = ip.Split(".");
        return (ipb.Length != 4);
    }


    public void OnTriggerEnterBtn(Text text)
    {
        string t = text.text;

        if (ready)
        {
            playerTextOutput.text += t;

        }
    }

    public void OnTriggerEnterDelete()
    {

        if (ready && playerTextOutput.text.Length > 0)
        {
            playerTextOutput.text = playerTextOutput.text.Substring(0, playerTextOutput.text.Length - 1);

        }
    }

    public void OnTriggerEnterCancel()
    {

        if (ready && playerTextOutput.text.Length > 0)
        {
            playerTextOutput.text = "";

        }
    }
    protected void HandleConnectionOpen(object sender, System.EventArgs e)
    {
        connected = true;
        Debug.Log("ConnectionManager: Connection opened");

    }

    public void SendExecutableAsk(string AgentToSendInfo, string action, Dictionary<string, string> arguments)
    {
        if (!connected) return;
        string argsJSON = JsonConvert.SerializeObject(arguments);
        Dictionary<string, string> jsonExpression = null;
        jsonExpression = new Dictionary<string, string> {
            {"type", "ask"},
            {"action", action},
            {"args", argsJSON},
            {"agent", AgentToSendInfo }
        };

        string jsonStringExpression = JsonConvert.SerializeObject(jsonExpression);

        SendMessageToServer(jsonStringExpression, new Action<bool>((success) =>
        {

        }));
    }
    public WebSocket GetSocket()
    {
        return socket;
    }

    protected void SendMessageToServer(string message, Action<bool> successCallback)
    {
        socket.SendAsync(message, successCallback);
    }

    void OnDestroy()
    {
        if (socket != null)
        {
            socket.Close();
        }
    }
}
