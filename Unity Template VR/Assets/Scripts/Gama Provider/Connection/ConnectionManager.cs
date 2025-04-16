using System;
using System.Collections.Generic;
using UnityEngine;
using WebSocketSharp;
using Newtonsoft.Json.Linq;
using Newtonsoft.Json;
using System.Linq;

public class ConnectionManager : WebSocketConnector
{
     
    private ConnectionState currentState;
    private bool connectionRequested; 

    // called when the connection state is manually changed
    public event Action<ConnectionState> OnConnectionStateChanged;

    // called when a "json_simulation" message is received
    public event Action<String, String> OnServerMessageReceived;

    // called when a "json_state" message is received 
    public event Action<JObject> OnConnectionStateReceived;

    // called when a connection request fails
    public event Action<bool> OnConnectionAttempted;

    public static ConnectionManager Instance = null;

    //use to seperate messages in the case where the middleware is not used
    protected String MessageSeparator  = "|||";

private String AgentToSendInfo = "simulation[0].unity_linker[0]";
     
    
    // ############################################# UNITY FUNCTIONS #############################################
    void Awake() {
        UseMiddleware = DesktopMode ? UseMiddlewareDM : PlayerPrefs.GetString("MIDDLEWARE").Equals("Y");
        Debug.Log("ConnectionManager: Awake : " + PlayerPrefs.GetString("MIDDLEWARE"));
        Debug.Log("ConnectionManager Awake host: " + PlayerPrefs.GetString("IP") + " PORT: " + PlayerPrefs.GetString("PORT") + " UseMiddleware: "+ UseMiddleware);

        Instance = this;

        Debug.Log("START");
        UpdateConnectionState(ConnectionState.DISCONNECTED);
    }

   

    public string GetMessageSeparator()
    {
        return MessageSeparator; 
    }

    // ############################################# CONNECTION HANDLER #############################################
    public void UpdateConnectionState(ConnectionState newState) {
        
        switch (newState) {
            case ConnectionState.PENDING:
                Debug.Log("ConnectionManager: UpdateConnectionState -> PENDING");
                break;
            case ConnectionState.CONNECTED:
                Debug.Log("ConnectionManager: UpdateConnectionState -> CONNECTED");
                break;
            case ConnectionState.AUTHENTICATED:
                Debug.Log("ConnectionManager: UpdateConnectionState -> AUTHENTICATED");
                break;
            case ConnectionState.DISCONNECTED:
                Debug.Log("ConnectionManager: UpdateConnectionState -> DISCONNECTED");
              //  TryConnectionToServer();
                break;
            default:
                break;
        }

        currentState = newState;
        OnConnectionStateChanged?.Invoke(newState);        
    }

    // ############################################# HANDLERS #############################################

    protected override void HandleConnectionOpen()
    {
        if (UseMiddleware)
        {
            var jsonId = new Dictionary<string, string> {
                {"type", "connection"},
                { "id", StaticInformation.getId() },
                { "heartbeat", ""+ HeartbeatInMs}
            }; 
            string jsonStringId = JsonConvert.SerializeObject(jsonId);
            SendMessageToServer(jsonStringId);
            Debug.Log("ConnectionManager: Connection opened");
        }
       
    }

    protected override void ManageMessage(string message)
    {

            
            JObject jsonObj = JObject.Parse(message);
            string type = (string)jsonObj["type"];
           
        
            if (UseMiddleware)
            {
                switch (type)
                {
                    case "ping":
                        var jsonId = new Dictionary<string, string> {{"type", "pong"}};
                        string jsonStringId = JsonConvert.SerializeObject(jsonId);
                        SendMessageToServer(jsonStringId);
                        break;
                    case "json_state":
                        OnConnectionStateReceived?.Invoke(jsonObj);
                        bool authenticated = (bool)jsonObj["in_game"];
                        bool connected = (bool)jsonObj["connected"];

                        if (authenticated && connected)
                        {
                            if (!IsConnectionState(ConnectionState.AUTHENTICATED))
                            {
                                Debug.Log("ConnectionManager: Player successfully authenticated");
                                UpdateConnectionState(ConnectionState.AUTHENTICATED);
                            }

                        }
                        else if (connected && !authenticated)
                        {
                            if (!IsConnectionState(ConnectionState.CONNECTED))
                            {
                                connectionRequested = false;
                                Debug.Log("ConnectionManager: Successfully connected, waiting for authentication...");
                                UpdateConnectionState(ConnectionState.CONNECTED);
                                OnConnectionAttempted?.Invoke(true);
                            }
                            else
                            {
                                Debug.LogWarning("ConnectionManager: Already connected, waiting for authentication...");
                            }

                        }  
                        break;  

                    case "json_output":
                        JObject content = (JObject)jsonObj["contents"];
                        String firstKey = content.Properties().Select(pp => pp.Name).FirstOrDefault();
                        OnServerMessageReceived?.Invoke(firstKey, content.ToString());
                        break;

                    default:
                        break;
                }
            } 
            else if (type.Equals("SimulationOutput"))
            {
                JValue content = (JValue)jsonObj["content"];
               // Debug.Log("MessageSeparator: " + MessageSeparator);
                foreach (String mes in content.ToString().Split(MessageSeparator))
                {
                    if (!mes.IsNullOrEmpty())
                        OnServerMessageReceived?.Invoke(null, mes);
                }
            }
        
    }

    protected override void HandleConnectionClosed() {
        // checks if the connection was closed just after a connection request
        Debug.Log("ConnectionManager: HandleConnectionClosed");
        if (connectionRequested) {
            connectionRequested = false;
            OnConnectionAttempted?.Invoke(false);
            Debug.Log("ConnectionManager: Failed to connect to server");
        } 
        UpdateConnectionState(ConnectionState.DISCONNECTED);
    }

    // ############################################# UTILITY FUNCTIONS #############################################
    public void TryConnectionToServer() {
        if(IsConnectionState(ConnectionState.DISCONNECTED)) {
            Debug.Log("ConnectionManager: Attempting to connect to " + (UseMiddleware?"middleware":"GAMA")+ ": ws://" + host + ":" + port + "/");
            connectionRequested = true;
            UpdateConnectionState(ConnectionState.PENDING);

            GetSocket().Connect();
             
            if (! UseMiddleware)  
            {
                Debug.Log("Create player direct :" + ConnectionManager.Instance.GetConnectionId());

                  Dictionary<string, string> args = new Dictionary<string, string> {
                    {"id", "\""+ConnectionManager.Instance.GetConnectionId()+"\""}
                  };
                  SendExecutableAsk("create_init_player", args);

                 
                UpdateConnectionState(ConnectionState.AUTHENTICATED); 

            }
        } else {
            Debug.LogWarning("ConnectionManager: Already connected to middleware: " + this.currentState);
        }
        
    }
     
    public void DisconnectFromServer() {
        if(!IsConnectionState(ConnectionState.DISCONNECTED)) {
            Debug.Log("ConnectionManager: Disconnecting from middleware...");
            GetSocket().Close();
            UpdateConnectionState(ConnectionState.DISCONNECTED);
        } else {
            Debug.LogWarning("ConnectionManager: Already disconnected from middleware");
        }
    }

    public bool IsConnectionState(ConnectionState currentState) {
        return this.currentState == currentState;
    }

    public void SendExecutableExpression(string expression) {
        Dictionary<string, string> jsonExpression = null;
        jsonExpression = new Dictionary<string, string> {
            {"type", "expression"},
            {"expr", expression}
        };

        string jsonStringExpression = JsonConvert.SerializeObject(jsonExpression);
        SendMessageToServer(jsonStringExpression);

        /*, new Action<bool>((success) => {
            if (!success) {
                numErrors++;
                Debug.LogError("ConnectionManager: Failed to send executable expression");
                if (numErrors > numErrorsBeforeDeconnection)
                {
                    GetSocket().Close();
                   currentState = (ConnectionState.DISCONNECTED);
                    numErrors = 0;
                }
            } else
            {
                numErrors = 0;
            }
        }));*/
    }

    public void SendExecutableAsk(string action, Dictionary<string,string> arguments)
    {
        string argsJSON = JsonConvert.SerializeObject(arguments);
        Dictionary<string, string> jsonExpression = null;
        jsonExpression = new Dictionary<string, string> {
            {"type", "ask"},
            {"action", action},
            {"args", argsJSON},
            {"agent", AgentToSendInfo }
        };

        string jsonStringExpression = JsonConvert.SerializeObject(jsonExpression);

        SendMessageToServer(jsonStringExpression);

        /*, new Action<bool>((success) => {
            if (!success)
            {
                numErrors++;
                Debug.LogError("ConnectionManager: Failed to send executable ask");
                if (numErrors > numErrorsBeforeDeconnection)
                {
                    GetSocket().Close();
                    currentState = (ConnectionState.DISCONNECTED);
                    numErrors = 0;
                }
            } else
            {
                numErrors = 0;
            }
    }));*/
    }

    public void DisconnectProperly() {
        Dictionary<string,string> jsonExpression = new Dictionary<string,string> {
            {"type", "disconnect_properly"}
        };
        string jsonStringExpression = JsonConvert.SerializeObject(jsonExpression);
        SendMessageToServer(jsonStringExpression);
        DisconnectFromServer();
    }

    public string GetConnectionId() {
        return StaticInformation.getId();
    }


    public bool getUseMiddleware()
    {
        return UseMiddleware;
    }

    public void Reconnect()
    {
        Debug.Log("Reconnect");
        currentState = ConnectionState.DISCONNECTED;
        TryConnectionToServer();
    }


}


public enum ConnectionState {
    DISCONNECTED,
    // waiting for connection to be established
    PENDING, 
    // connection established, waiting for authentication
    CONNECTED,
    // connection established and authenticated
    AUTHENTICATED
}
