using System;
using UnityEngine;
using WebSocketSharp;


public abstract class WebSocketConnector : MonoBehaviour
{

    protected string DefaultIP = "localhost";
    protected string DefaultPort = "8080";


    protected string host ;
     protected string port;

    protected bool UseMiddleware; 

    private WebSocket socket; 


    protected int HeartbeatInMs = 5000; //only for middleware mode
    protected bool DesktopMode = false;
    public bool fixedProperties = true;
   protected bool UseMiddlewareDM = true;

    protected int numErrorsBeforeDeconnection = 10;
    protected int numErrors = 0;

    void OnEnable() {
       
       // port = PlayerPrefs.GetString("PORT"); 
        host = PlayerPrefs.GetString("IP");
        port = DefaultPort;

        if (DesktopMode)
        {
            UseMiddleware = UseMiddlewareDM;
            host = "localhost";

            if (UseMiddleware)
            {
                port = "8080";
            }
            else 
            {
                port = "1000";
            }
            
        } else if (fixedProperties)
        {
            UseMiddleware = UseMiddlewareDM;
            host = DefaultIP;
            port = DefaultPort;
            
        } else
        {
            if (host == null && host.Length == 0)
            {
                host = DefaultIP;
                
            }
        }
        Debug.Log("WebSocketConnector host: " + host + " PORT: " + port + " MIDDLEWARE:" + UseMiddleware);

        socket = new WebSocket("ws://" + host + ":" + port + "/");
        
        // Enable the Per-message Compression extension.
        // Saved some bandwidth
        // Doesn't work on our specific installation : https://github.com/sta/websocket-sharp/issues/580
        socket.Compression = CompressionMethod.None;//Deflate;
        
        socket.OnOpen += HandleConnectionOpen;
        socket.OnMessage += HandleReceivedMessage;
        socket.OnClose += HandleConnectionClosed;
        
    }

   void OnDestroy() {
        socket.Close();
    }

    // ############################## HANDLERS ##############################

    protected abstract void HandleConnectionOpen(object sender, System.EventArgs e);

    protected abstract void HandleReceivedMessage(object sender, MessageEventArgs e);

    protected abstract void HandleConnectionClosed(object sender, CloseEventArgs e);

    // #######################################################################

    protected void SendMessageToServer(string message, Action<bool> successCallback) {
       socket.SendAsync(message, successCallback);
    }

    protected WebSocket GetSocket() {
        return socket;
    }

    private bool ValidIp(string ip) {
        if (ip == null || ip.Length == 0) return false;
        string[] ipb = ip.Split(".");
        return (ipb.Length != 4);
    }
}
