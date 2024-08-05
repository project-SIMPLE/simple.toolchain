using System;
using System.Collections;
using System.Collections.Generic;
using System.Timers;
using TMPro;
using UnityEngine;
using UnityEngine.SceneManagement;
using UnityEngine.UI;

public class IpManager : MonoBehaviour
{
    [SerializeField] private double delay = 2000;

    private static System.Timers.Timer aTimer;
    private static bool ready = false;
    private TextMeshProUGUI playerTextOutput;


    private static bool NotValid(string ip)
    {
        if (ip == null || ip.Length == 0) return false;
        string[] ipb = ip.Split(".");
        return (ipb.Length != 4);
    }

    private static void OnTimedEvent(object source, ElapsedEventArgs e)
    {
        ready = true;
        aTimer.Stop();
        aTimer = null;

    }

    // Start is called before the first frame update
    void Start() {
        string ip = PlayerPrefs.GetString("IP");
        if (NotValid(ip))
            ip = "127.0.0.1";

        playerTextOutput = GameObject.FindGameObjectWithTag("textIP").GetComponentInChildren<TextMeshProUGUI>();

        playerTextOutput.text = ip;
        ready = false;
        aTimer = new System.Timers.Timer(delay);
        aTimer.Elapsed += new ElapsedEventHandler(OnTimedEvent);

        aTimer.AutoReset = false;
        aTimer.Enabled = true;
       PlayerPrefs.SetString("IP", ip);
       PlayerPrefs.Save();
    }


    public void OnTriggerEnterBtn(Text text) {
        string t = text.text;

        if (ready)
        {
            playerTextOutput.text += t;
                
        }
    }

    public void OnTriggerEnterValidate() {

        if (ready) {
            PlayerPrefs.SetString("IP", playerTextOutput.text);
            PlayerPrefs.Save();
            SceneManager.LoadScene("Startup Menu");
        }
    }

    public void OnTriggerEnterCancel() {

        if (ready)
        {
            SceneManager.LoadScene("Startup Menu");
        }
    }
    

    public void OnTriggerEnterDelete() {

        if (ready && playerTextOutput.text.Length > 0)
        {
            playerTextOutput.text = playerTextOutput.text.Substring(0, playerTextOutput.text.Length - 1);
            
        }
    }

}
