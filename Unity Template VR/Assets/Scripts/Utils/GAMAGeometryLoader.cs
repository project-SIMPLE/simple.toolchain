
using UnityEngine;
using WebSocketSharp;
using System;
using Newtonsoft.Json.Linq;
using System.Collections.Generic;


using Newtonsoft.Json;
using System.Linq;

public class GAMAGeometryLoader: ConnectionWithGama
{
   
    // optional: define a scale between GAMA and Unity for the location given
    public float offsetYBackgroundGeom = 0.0f;
    protected WorldJSONInfo infoWorld;
    protected Dictionary<string, List<object>> geometryMap;

    private PolygonGenerator polyGen;

//    private GAMAGeometry geoms;

    private bool continueProcess = true;
    public float GamaCRSCoefX = 1.0f;
    public float GamaCRSCoefY = 1.0f;
    public float GamaCRSOffsetX = 0.0f;
    public float GamaCRSOffsetY = 0.0f;

    protected Dictionary<string, PropertiesGAMA> propertyMap = null;


    protected AllProperties propertiesGAMA;
    protected ConnectionParameter parameters = null;
    protected CoordinateConverter converter;


    public void GenerateGeometries(string ip_, string port_, float x, float y, float ox, float oy, float YOffset)
    {
       ip = ip_;
        port = port_;
        GamaCRSCoefX = x;
        GamaCRSCoefY = y;
        GamaCRSOffsetX = ox;
        GamaCRSOffsetY = oy;
        offsetYBackgroundGeom = YOffset;
        socket = new WebSocket("ws://" + ip + ":" + port + "/");
        Debug.Log("ws://" + ip + ":" + port + "/");

        continueProcess = true;

        socket.OnMessage += HandleReceivedMessage;
        socket.OnOpen += HandleConnectionOpen;
        socket.OnClose += HandleConnectionClosed;
        
        // Enable the Per-message Compression extension.
        // Saved some bandwidth
        socket.Compression = CompressionMethod.None;//Deflate;
        
        socket.Connect();

        DateTime dt = DateTime.Now;
        dt = dt.AddSeconds(60);
        while (continueProcess) {
            if (infoWorld != null)
            {
                generateGeom();
                continueProcess = false;
            }
            if (DateTime.Now.CompareTo(dt) >= 0)
            {
                Debug.Log("end");
                break;
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
                { "id", "geomloader" },
                { "heartbeat", "5000" }
            };
            string jsonStringId = JsonConvert.SerializeObject(jsonId);
            SendMessageToServer(jsonStringId, new Action<bool>((success) => {
                if (success) { }
            }));
            Debug.Log("ConnectionManager: Connection opened");
        

    }


    private GameObject instantiatePrefab(String name, PropertiesGAMA prop)
    {
        if (prop.prefabObj == null)
        {
            prop.loadPrefab(parameters.precision);
        }
        GameObject obj = Instantiate(prop.prefabObj);
        float scale = ((float)prop.size) / parameters.precision;
        obj.transform.localScale = new Vector3(scale, scale, scale);
        obj.SetActive(true);

        if (prop.hasCollider)
        {
            if (obj.TryGetComponent<LODGroup>(out var lod))
            {
                foreach (LOD l in lod.GetLODs())
                {
                    GameObject b = l.renderers[0].gameObject;
                    BoxCollider bc = b.AddComponent<BoxCollider>();
                    // b.tag = obj.tag;
                    // b.name = obj.name;
                    //bc.isTrigger = prop.isTrigger;
                }

            }
            else
            {
                BoxCollider bc = obj.AddComponent<BoxCollider>();
                // bc.isTrigger = prop.isTrigger;
            }
        }
        List<object> pL = new List<object>();
        pL.Add(obj); pL.Add(prop);
        instantiateGO(obj, name, prop);
        return obj;
    }


    private void instantiateGO(GameObject obj, String name, PropertiesGAMA prop)
    {
        obj.name = name;
       
        if (prop.tag != null && !string.IsNullOrEmpty(prop.tag))
            obj.tag = prop.tag;

        if (prop.isInteractable)
        {
            UnityEngine.XR.Interaction.Toolkit.Interactables.XRBaseInteractable interaction = null;
            if (prop.isGrabable)
            {
                interaction = obj.AddComponent<UnityEngine.XR.Interaction.Toolkit.Interactables.XRGrabInteractable>();
                Rigidbody rb = obj.GetComponent<Rigidbody>();
                if (prop.constraints != null && prop.constraints.Count == 6)
                {
                    if (prop.constraints[0])
                        rb.constraints = rb.constraints | RigidbodyConstraints.FreezePositionX;
                    if (prop.constraints[1])
                        rb.constraints = rb.constraints | RigidbodyConstraints.FreezePositionY;
                    if (prop.constraints[2])
                        rb.constraints = rb.constraints | RigidbodyConstraints.FreezePositionZ;
                    if (prop.constraints[3])
                        rb.constraints = rb.constraints | RigidbodyConstraints.FreezeRotationX;
                    if (prop.constraints[4])
                        rb.constraints = rb.constraints | RigidbodyConstraints.FreezeRotationY;
                    if (prop.constraints[5])
                        rb.constraints = rb.constraints | RigidbodyConstraints.FreezeRotationZ;
                }


            }
            else 
            {

                interaction = obj.AddComponent<UnityEngine.XR.Interaction.Toolkit.Interactables.XRSimpleInteractable>();


            } 
            if (interaction.colliders.Count == 0)
            {
                Collider[] cs = obj.GetComponentsInChildren<Collider>();
                if (cs != null)
                {
                    foreach (Collider c in cs)
                    {
                        interaction.colliders.Add(c);
                    }
                }
            }
           

        }
    }






    void GenerateGeometries()
    {
        Debug.Log("GenerateGeometries");
        Dictionary<PropertiesGAMA, List<GameObject>> mapObjects = new Dictionary<PropertiesGAMA, List<GameObject>>();
       int cptPrefab = 0;
        int cptGeom = 0;
        for (int i = 0; i < infoWorld.names.Count; i++)
        {
            string name = infoWorld.names[i];
            string propId = infoWorld.propertyID[i];

            PropertiesGAMA prop = propertyMap[propId];
            GameObject obj = null;

            if (prop.hasPrefab)
            {
                obj = instantiatePrefab(name, prop); 
                
                
                List<int> pt = infoWorld.pointsLoc[cptPrefab].c;
                Vector3 pos = converter.fromGAMACRS(pt[0], pt[1], pt[2]);
                pos.y += pos.y + prop.yOffsetF;
                float rot = prop.rotationCoeffF * ((0.0f + pt[3]) / parameters.precision) + prop.rotationOffsetF;
                obj.transform.SetPositionAndRotation(pos, Quaternion.AngleAxis(rot, Vector3.up));
                //obj.SetActive(true);
                cptPrefab++;

            }
            else
            {
                if (polyGen == null)
                {
                    polyGen = PolygonGenerator.GetInstance();
                    polyGen.Init(converter); 
                }
                List<int> pt = infoWorld.pointsGeom[cptGeom].c;
                float YoffSet = (0.0f + infoWorld.offsetYGeom[cptGeom]) / (0.0f + parameters.precision);
                
                obj = polyGen.GeneratePolygons(true, name, pt.ToArray(), prop, parameters.precision);
                obj.transform.position = new Vector3(obj.transform.position.x, obj.transform.position.y + YoffSet, obj.transform.position.z);
                if (prop.hasCollider)
                {

                    MeshCollider mc = obj.AddComponent<MeshCollider>();
                    if (prop.isGrabable)
                    {
                        mc.convex = true;
                    }
                    mc.sharedMesh = polyGen.surroundMesh;
                    // mc.isTrigger = prop.isTrigger;
                }

                instantiateGO(obj, name, prop);
                // polyGen.surroundMesh = null;

               
                List<object> pL = new List<object>();
                pL.Add(obj); pL.Add(prop);
                //obj.SetActive(true);
                cptGeom++;

            }
            if (obj != null)
            {
                if (!mapObjects.ContainsKey(prop))
                    mapObjects[prop] = new List<GameObject>();
                mapObjects[prop].Add(obj);
            }


        }
        GameObject n = new GameObject("GENERATED");
        foreach (PropertiesGAMA p in mapObjects.Keys)
        {
            GameObject g = new GameObject(p.id);
            g.transform.parent = n.transform;
            foreach (GameObject o in mapObjects[p])
            {
                o.transform.parent = g.transform;

            }
        }
        infoWorld = null;
    }



    private void generateGeom()
    {


       if (parameters != null && converter != null )
        {

            GenerateGeometries();
            continueProcess = false;

        }
    }

    void HandleServerMessageReceived(string firstKey, String content)
    {

        if (content == null || content.Equals("{}")) return;
     
        switch (firstKey)
        {
            // handle general informations about the simulation
            case "precision":

                parameters = ConnectionParameter.CreateFromJSON(content);
                converter = new CoordinateConverter(parameters.precision, GamaCRSCoefX, GamaCRSCoefY, GamaCRSCoefY, GamaCRSOffsetX, GamaCRSOffsetY, 1.0f);
    
                break;

            case "properties":
                propertiesGAMA = AllProperties.CreateFromJSON(content);
                propertyMap = new Dictionary<string, PropertiesGAMA>();
                foreach (PropertiesGAMA p in propertiesGAMA.properties)
                {
                    propertyMap.Add(p.id, p);
                }
                break;

            // handle agents while simulation is running
            case "pointsLoc":
                if (infoWorld == null)
                {
                    infoWorld = WorldJSONInfo.CreateFromJSON(content);
                   
                }
                break;
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
            else if(type.Equals("json_state")) {

                Boolean inGame = (Boolean)jsonObj["in_game"];
                if (inGame)
                {
                    Dictionary<string, string> args = new Dictionary<string, string> {
                         {"id", "geomloader" }
                    };
                   
                    SendExecutableAsk("send_init_data", args);

                }
            }
        } 
    }

}