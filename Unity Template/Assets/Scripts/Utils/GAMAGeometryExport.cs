using System;
using System.Collections.Generic;
using UnityEngine;
using WebSocketSharp;
using Newtonsoft.Json.Linq;
using Newtonsoft.Json;
using System.Linq;

public class GAMAGeometryExport : ConnectionWithGama
{

    protected ConnectionParameter parameters = null;
    
    // optional: define a scale between GAMA and Unity for the location given
    public float GamaCRSCoefX = 1.0f;
    public float GamaCRSCoefY = 1.0f;
    public float GamaCRSOffsetX = 0.0f;
    public float GamaCRSOffsetY = 0.0f;

    private bool continueProcess = true;
    GameObject objectToSend;

    Dictionary<string, string> argsToSend = null;

    public void ManageGeometries(GameObject objectToSend_, string ip_, string port_, float x, float y, float ox, float oy)
    {
        objectToSend = objectToSend_;
        if (objectToSend == null) return;
        parameters = null;

        ip = ip_;
        port = port_;
        GamaCRSCoefX = x;
        GamaCRSCoefY = y;
        GamaCRSOffsetX = ox;
        GamaCRSOffsetY = oy;

        UnityGeometry ug = new UnityGeometry(objectToSend, new CoordinateConverter(10000, x, y, ox, oy));

        string message = ug.ToJSON();
      
        argsToSend = new Dictionary<string, string> {
                    {"geoms", message}
                  };

        socket = new WebSocket("ws://" + ip + ":" + port + "/");

         continueProcess = true; 

        socket.OnOpen += HandleConnectionOpen;
        socket.OnMessage += HandleReceivedMessage;
        socket.OnClose += HandleConnectionClosed;
        
        // Enable the Per-message Compression extension.
        // Saved some bandwidth
        socket.Compression = CompressionMethod.None;//Deflate;

        socket.Connect();
        
        while (continueProcess)
        {
            if (parameters != null)
            {
                ExportGeoms();
                continueProcess = false;
            }
           
        }
       
    }

    void HandleConnectionClosed(object sender, CloseEventArgs e)
    {
        continueProcess = false;
    }

    void HandleConnectionOpen(object sender, System.EventArgs e)
    {
        var jsonId = new Dictionary<string, string> {
                {"type", "connection"},
                { "id", "geomexporter" },
                { "heartbeat", "5000"}
            };
        string jsonStringId = JsonConvert.SerializeObject(jsonId);
        SendMessageToServer(jsonStringId, new Action<bool>((success) => {
            if (success) { }
        }));
        Debug.Log("ConnectionManager: Connection opened");


    }

    private void ExportGeoms()
    {
        Debug.Log("export Geom");
        if (parameters != null )
        {
           
          
            SendExecutableAsk("receive_geometries", argsToSend);

           
            continueProcess = false;

        }
    }

    void HandleServerMessageReceived(string firstKey, String content)
    {

        if (content == null || content.Equals("{}")) return;
        else if (content.Contains("precision"))
            firstKey = "precision";

        switch (firstKey)
        {
            // handle general informations about the simulation
            case "precision":
                parameters = ConnectionParameter.CreateFromJSON(content);
                Debug.Log("Received parameter data");
                break;

            // handle geometries sent by GAMA at the beginning of the simulation
           
        }

    }
    void HandleReceivedMessage(object sender, MessageEventArgs e)
    {

        if (e.IsText)
        {
            JObject jsonObj = JObject.Parse(e.Data);
            string type = (string)jsonObj["type"];


            if (type.Equals("json_output"))
            {
                JObject content = (JObject)jsonObj["contents"];
                String firstKey = content.Properties().Select(pp => pp.Name).FirstOrDefault();
                HandleServerMessageReceived(firstKey, content.ToString());
                 
            } 
            else if (type.Equals("json_state"))
            {

                Boolean inGame = (Boolean)jsonObj["in_game"];
                if ( inGame)
                {
                    Dictionary<string, string> args = new Dictionary<string, string> {
                         {"id", "geomexporter" }
                    };
                   
                    SendExecutableAsk("send_init_data", args);

                }
            } 
        }
    }


}