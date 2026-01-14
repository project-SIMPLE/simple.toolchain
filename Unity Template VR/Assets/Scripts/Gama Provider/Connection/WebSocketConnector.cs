using System;
using UnityEngine;

using System.Text;

using NativeWebSocket;
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

    async void Start()
    {
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
            if ((host == null || host == "") && host.Length == 0)
            {
                host = DefaultIP;
            }
        }
        Debug.Log("WebSocketConnector host: " + host + " PORT: " + port + " MIDDLEWARE:" + UseMiddleware);

        socket = new WebSocket("ws://" + host + ":" + port + "/");

        // Enable the Per-message Compression extension.
        // Saved some bandwidth
        // Doesn't work on our specific installation : https://github.com/sta/websocket-sharp/issues/580
        /*  socket.Compression = CompressionMethod.None;//Deflate;

          socket.OnOpen += HandleConnectionOpen;
          socket.OnMessage += HandleReceivedMessage;
          socket.OnClose += HandleConnectionClosed;*/
        socket.OnOpen += () =>
        {
            Debug.Log("WS connected!");
            HandleConnectionOpen();
        };

        // Add OnMessage event listener
        socket.OnMessage += (byte[] msg) =>
        {
            string mes = Encoding.UTF8.GetString(msg);
            // Debug.Log("WS received message: " + mes);
            ManageMessage(mes);
        };

        // Add OnError event listener
        socket.OnError += (string errMsg) =>
        {
            Debug.Log("WS error: " + errMsg);
        };

        // Add OnClose event listener
        socket.OnClose += (WebSocketCloseCode code) =>
        {
            HandleConnectionClosed();
            Debug.Log("WS closed with code: " + code.ToString());
        };

        // Connect to the server 
        await socket.Connect();

    }

    protected virtual void HandleConnectionClosed()
    {

    }
    protected virtual void ManageMessage(string message)
    {

    }

    protected virtual void HandleConnectionOpen()
    {

    }

    private async void OnApplicationQuit()
    {
        await socket.Close();
    }

    async void OnDestroy() {
        await socket.Close();
    }

    // ############################## HANDLERS ##############################

    // #######################################################################

    void Update()
    {
    #if !UNITY_WEBGL || UNITY_EDITOR
            socket.DispatchMessageQueue();
    #endif
    }


    async protected void SendMessageToServer(string message)
    {
        //        Debug.Log("SEND MESSAGE: " + message);
        await socket.SendText(message);
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
