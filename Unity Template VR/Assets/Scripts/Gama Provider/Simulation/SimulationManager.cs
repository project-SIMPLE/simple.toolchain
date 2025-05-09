using System;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;
using UnityEngine.XR.Interaction.Toolkit;
using UnityEngine.InputSystem;


public class SimulationManager : MonoBehaviour
{
    [SerializeField] protected InputActionReference primaryRightHandButton = null;
  
    [Header("Base GameObjects")]
    [SerializeField] protected GameObject player;
    [SerializeField] protected GameObject Ground;


    // optional: define a scale between GAMA and Unity for the location given
    [Header("Coordinate conversion parameters")]
    protected float GamaCRSCoefX = 1.0f;
    protected float GamaCRSCoefY = 1.0f;
     protected float GamaCRSOffsetX = 0.0f;
    protected float GamaCRSOffsetY = 0.0f;


    protected Transform XROrigin;
   
    // Z offset and scale
     protected float GamaCRSOffsetZ = 0.0f;

    protected List<GameObject> toFollow;

    XRInteractionManager interactionManager;

    // ################################ EVENTS ################################
    // called when the current game state changes
    public static event Action<GameState> OnGameStateChanged;
    // called when the game is restarted
    public static event Action OnGameRestarted;

    // called when the world data is received
    //    public static event Action<WorldJSONInfo> OnWorldDataReceived;
    // ########################################################################

    protected Dictionary<string, List<object>> geometryMap;
    protected Dictionary<string, PropertiesGAMA> propertyMap = null;

    protected List<GameObject> SelectedObjects;


    protected bool handleGeometriesRequested;
    protected bool handleGroundParametersRequested;

    protected CoordinateConverter converter;
    protected PolygonGenerator polyGen;
    protected ConnectionParameter parameters;
    protected AllProperties propertiesGAMA;
    protected WorldJSONInfo infoWorld;
    protected AnimationInfo infoAnimation = null;
    protected GameState currentState;

    public static SimulationManager Instance = null;


    //allows to define the minimal time between two interactions
    protected float timeWithoutInteraction = 1.0f; //in second

    protected float remainingTime = 0.0f;


    protected bool sendMessageToReactivatePositionSent = false;

    protected float maxTimePing = 1.0f;
    protected float currentTimePing = 0.0f;

    protected List<GameObject> toDelete;

    protected bool readyToSendPosition = false;

    protected bool readyToSendPositionInit = true;

    protected float TimeSendPosition = 0.05f;
    protected float TimeSendPositionAfterMoving = 1.0f;
    protected float TimerSendPosition = 0.0f;

    protected List<GameObject> locomotion;
    protected MoveHorizontal mh = null;
    protected MoveVertical mv = null;

    protected DEMData data;
    protected DEMDataLoc dataLoc;
    protected TeleoportAreaInfo dataTeleport;
    protected WallInfo dataWall;
    protected EnableMoveInfo enableMove;


    protected float TimeSendInit = 0.5f;
    protected float TimerSendInit ;

    //Cache
    Dictionary<string, string> connectionID;
    HashSet<string> toRemove = new HashSet<string>();

    // ############################################ UNITY FUNCTIONS ############################################
    void Awake()
    {
        Debug.Log("Simulation Manager");
        Instance = this;
        SelectedObjects = new List<GameObject>();
        // toDelete = new List<GameObject>();

       locomotion = new List<GameObject>(GameObject.FindGameObjectsWithTag("locomotion"));
        mh = player.GetComponentInChildren<MoveHorizontal>();
        mv = player.GetComponentInChildren<MoveVertical>();
         
        XROrigin = player.transform;
        playerMovement(false);
        toFollow = new List<GameObject>();

        connectionID = new Dictionary<string, string>
        {
            {"id", ConnectionManager.Instance.getUseMiddleware()
                    ? ConnectionManager.Instance.GetConnectionId()
                    : ("\"" + ConnectionManager.Instance.GetConnectionId() + "\"")
            }
        };
    }


    void OnEnable()
    {
        if (ConnectionManager.Instance != null)
        {
            ConnectionManager.Instance.OnServerMessageReceived += HandleServerMessageReceived;
            ConnectionManager.Instance.OnConnectionAttempted += HandleConnectionAttempted;
            ConnectionManager.Instance.OnConnectionStateChanged += HandleConnectionStateChanged;
            Debug.Log("SimulationManager: OnEnable");
        }
        else
        {
            Debug.Log("No connection manager");
        }
    }

    void OnDisable()
    {
        Debug.Log("SimulationManager: OnDisable");
        ConnectionManager.Instance.OnServerMessageReceived -= HandleServerMessageReceived;
        ConnectionManager.Instance.OnConnectionAttempted -= HandleConnectionAttempted;
        ConnectionManager.Instance.OnConnectionStateChanged -= HandleConnectionStateChanged;
    }

    void OnDestroy()
    {
        Debug.Log("SimulationManager: OnDestroy");
    }

    void Start()
    {
        geometryMap = new Dictionary<string, List<object>>();
        handleGeometriesRequested = false;
        // handlePlayerParametersRequested = false;
        handleGroundParametersRequested = false;
        OnEnable();
    }


    void FixedUpdate()
    {

        if (sendMessageToReactivatePositionSent)
        {
            ConnectionManager.Instance.SendExecutableAsk("player_position_updated", connectionID);
            sendMessageToReactivatePositionSent = false;
        }

        if (handleGroundParametersRequested)
        {
            InitGroundParameters();
            handleGroundParametersRequested = false;

            Debug.Log("handleGroundParametersRequested: " + handleGroundParametersRequested);

        }

        if (handleGeometriesRequested && infoWorld != null && infoWorld.isInit)// && propertyMap != null)
        {

            sendMessageToReactivatePositionSent = true;
            GenerateGeometries(true, null);
            handleGeometriesRequested = false;
            UpdateGameState(GameState.GAME);
         

        }
        if (infoWorld != null && !infoWorld.isInit && IsGameState(GameState.LOADING_DATA))
        {
            infoWorld = null;
        }
        if (converter != null && data != null)
        {
            manageUpdateTerrain();
        }
        if (converter != null && dataLoc != null)
        {
            manageSetValueTerrain();
        }
        if (converter != null && dataTeleport != null)
        {
            manageTeleportationArea();
        }
        if (converter != null &&  dataWall != null)
        {
            manageWalls();
        }
        if (enableMove != null)
        {
            playerMovement(enableMove.enableMove);
            enableMove = null;
        }

        if (infoAnimation != null)
        {
            updateAnimation();
            infoAnimation = null;
        }

        if (IsGameState(GameState.LOADING_DATA) && ConnectionManager.Instance.getUseMiddleware())
        {
            if (TimerSendInit > 0)
                TimerSendInit -= Time.deltaTime;
            if (TimerSendInit <= 0)
            {
                TimerSendInit = TimeSendInit;
                ConnectionManager.Instance.SendExecutableAsk("send_init_data", connectionID);
            }
        }

        if (IsGameState(GameState.GAME))
        {
            if (readyToSendPosition && TimerSendPosition <= 0.0f && readyToSendPositionInit)
                UpdatePlayerPosition();
            UpdateGameToFollowPosition();
            if (infoWorld != null && !infoWorld.isInit)
                UpdateAgentsList();
        }

    }

    private void Update()
    {
        if (remainingTime > 0)
            remainingTime -= Time.deltaTime;
        if (TimerSendPosition > 0)
        {
            TimerSendPosition -= Time.deltaTime;
        }
        if (currentTimePing > 0)
        { 
            currentTimePing -= Time.deltaTime;
            if (currentTimePing <= 0)
            {
                Debug.Log("Try to reconnect to the server");
                ConnectionManager.Instance.Reconnect();
            }
        }


        if (primaryRightHandButton != null && primaryRightHandButton.action.triggered)
        {
            TriggerMainButton();
        }
      /*  if (TryReconnectButton != null && TryReconnectButton.action.triggered)
        {
            Debug.Log("TryReconnectButton activated");
            TryReconnect();
        }*/

        OtherUpdate();
    }


    

    private void updateAnimation()
    {

        foreach (String n in infoAnimation.names) {
            if (!geometryMap.ContainsKey(n)) continue;            
            List<object> o = geometryMap[n];
            
            if (o == null && o.Count == 0) continue;
            GameObject obj = (GameObject)o[0];

            Animator m_animator = obj.GetComponent<Animator>();
            if (m_animator == null)
            {
                m_animator = obj.GetComponentInChildren<Animator>();
            }

            if (m_animator != null)
            {
                foreach (ParameterVal p in infoAnimation.parameters)
                {
                    if (p.type.Equals("int"))
                        m_animator.SetInteger(p.key, p.intVal);
                    else if (p.type.Equals("float"))
                        m_animator.SetFloat(p.key, p.floatVal);
                    else if (p.type.Equals("bool"))
                        m_animator.SetBool(p.key, p.boolVal);
                }
                foreach (String t in infoAnimation.triggers)
                {
                    m_animator.SetTrigger(t);

                }
            }
           
        }
       
    }
    private void manageTeleportationArea()
    {
        if (polyGen == null)
        {
            polyGen = PolygonGenerator.GetInstance();
            polyGen.Init(converter);
        }
        UnityEngine.XR.Interaction.Toolkit.Locomotion.Teleportation.TeleportationArea ta = null;
        GameObject[] objs = GameObject.FindGameObjectsWithTag("Teleportation");
        foreach (GameObject o in objs)
        {
            if (o.name.Equals(dataTeleport.teleportId))
            {
                ta = o.GetComponent<UnityEngine.XR.Interaction.Toolkit.Locomotion.Teleportation.TeleportationArea>();
                if (ta != null)
                {
                    foreach(Collider col in ta.colliders)
                    {
                        GameObject.DestroyImmediate(col.gameObject);
                    }
                    ta.colliders.Clear(); 
                }
                break;
                
            }
        }
        if (ta == null)
        {
            GameObject prefabObj = Resources.Load("Prefabs/Player/TeleportAreaRaw") as GameObject;
            GameObject obj = Instantiate(prefabObj);
           
            ta = obj.GetComponent<UnityEngine.XR.Interaction.Toolkit.Locomotion.Teleportation.TeleportationArea>();
            obj.name = dataTeleport.teleportId;
            obj.tag = "Teleportation";
        }
        
      
        for (int i = 0; i < dataTeleport.pointsGeom.Count; i++)
        {
            List<int> pt = dataTeleport.pointsGeom[i].c;
            float YoffSet = (0.0f + dataTeleport.offsetYGeom[i]) / (0.0f + parameters.precision);

            PropertiesGAMA prop = new PropertiesGAMA();
            prop.id = dataTeleport.teleportId + "_"+ i;
            prop.hasCollider = true;
            prop.isInteractable = false; 
            prop.isGrabable = false;
            prop.hasPrefab = false;
            prop.visible = true;
            prop.is3D = true;
            prop.height = dataTeleport.height;
            prop.toFollow = false;

            GameObject obj = polyGen.GeneratePolygons(false, prop.id, pt.ToArray(), prop, parameters.precision);

            obj.transform.position = new Vector3(obj.transform.position.x, obj.transform.position.y + YoffSet, obj.transform.position.z);
            MeshCollider mc = obj.AddComponent<MeshCollider>();
            mc.sharedMesh = polyGen.bottomMesh;
            obj.transform.parent = ta.gameObject.transform;
            ta.colliders.Add(mc);
           

        }
        //to take into account the new colliders
        ta.enabled = false;
        ta.enabled = true;

        dataTeleport = null;
    }

    private void manageWalls()
    {
       
       if (polyGen == null)
        {
            polyGen = PolygonGenerator.GetInstance();
            polyGen.Init(converter);
        }

        GameObject wallObj = new GameObject("Walls");

        GameObject[] objs =   GameObject.FindGameObjectsWithTag("InvisibleWall");
        foreach (GameObject o in objs)
        {
            if (o.name.Equals(dataWall.wallId))
            GameObject.DestroyImmediate(o);

        }

        for (int i = 0; i < dataWall.pointsGeom.Count;i++ )
        {
            List<int> pt = dataWall.pointsGeom[i].c;
            float YoffSet = (0.0f + dataWall.offsetYGeom[i]) / (0.0f + parameters.precision);

            PropertiesGAMA prop = new PropertiesGAMA();
            prop.id = dataWall.wallId;
            prop.hasCollider = true;
            prop.tag = "InvisibleWall";
            prop.isInteractable = false;
            prop.isGrabable = false;
            prop.hasPrefab = false;
            prop.visible = false;
            prop.height = dataWall.height;
            prop.is3D = true;
            prop.toFollow = false;

           GameObject obj = polyGen.GeneratePolygons(false, dataWall.wallId, pt.ToArray(), prop, parameters.precision);
        
            obj.transform.position = new Vector3(obj.transform.position.x, obj.transform.position.y + YoffSet, obj.transform.position.z);
            obj.transform.parent = wallObj.transform;
            MeshCollider mc = obj.AddComponent<MeshCollider>();
            mc.sharedMesh = polyGen.surroundMesh;
            
        }

        dataWall = null;
    }


    private void manageSetValueTerrain()
    {
        Terrain[] terrains = Terrain.activeTerrains;
        if (dataLoc.rows.Count == 0) return;
        foreach (Terrain t in terrains)
        {

            if (t.name == dataLoc.id)
            {
                float valMax = t.terrainData.size.y;

                int resolution = t.terrainData.heightmapResolution;

                if (dataLoc.valMax > valMax)
                {
                    float oldV = valMax;
                    valMax = dataLoc.valMax;
                    float[,] heightsT = new float[t.terrainData.heightmapResolution, t.terrainData.heightmapResolution];
                    for (int j = 0; j < resolution; j++)
                    {
                        for (int i = 0; i < resolution; i++)
                        {
                            float v = t.terrainData.GetHeight(i, j);
                            heightsT[i, j] = v * oldV / valMax;
                        }
                    }

                    t.terrainData.SetHeights(0, 0, heightsT);
                }
                float[,] heights = new float[dataLoc.rows[0].h.Count, dataLoc.rows.Count];
                int x = 1;
                foreach (Row r in dataLoc.rows)
                {
                   int y = 0;
                   foreach (int v in r.h)
                   {
                        heights[dataLoc.rows.Count - x, y] = ((v + 0.0f) / (valMax + 0.0f));
                        y++;
                   }
                   x++;
                }

                t.terrainData.SetHeights(dataLoc.indexX, resolution - 1 - dataLoc.indexY, heights);
                break;
            }
        }
        dataLoc = null;
    }

    private void manageUpdateTerrain()
    {
        Terrain[] terrains = Terrain.activeTerrains;

        foreach (Terrain t in terrains)
        {

            if (t.name == data.id)
            {
                t.gameObject.transform.position = new Vector3(0, 0,-1 * data.sizeY);
                t.terrainData.size = new Vector3(data.sizeX, data.valMax, data.sizeY);
                float[,] heights = new float[t.terrainData.heightmapResolution, t.terrainData.heightmapResolution];
                int x = 1;
                foreach (Row r in data.rows)
                {
                    int y = 0;
                    foreach (int v in r.h)
                    {
                        heights[data.rows.Count - x, y] = ((v + 0.0f) / (data.valMax + 0.0f));

                        y++;
                    }
                    x++;
                }
                t.terrainData.SetHeights(0, 0, heights);

                break;
            }
        }
        data = null;
    }
    

    void playerMovement(Boolean active)
    {
        foreach (GameObject loc in locomotion)
        {
            loc.SetActive(active);
        }
         if (mh != null)
         {
             mh.enabled = active;
         }
         if (mv != null)
         {
             mv.enabled = active;
         }
        readyToSendPositionInit = active;
    }


    void GenerateGeometries(bool initGame, HashSet<string> toRemove)
    {


        if (infoWorld.position != null && infoWorld.position.Count > 1 && (initGame || !sendMessageToReactivatePositionSent))
        {
            Vector3 pos = converter.fromGAMACRS(infoWorld.position[0], infoWorld.position[1], infoWorld.position[2]);
            XROrigin.localPosition = pos;
            sendMessageToReactivatePositionSent = true;
            readyToSendPosition = true;
            TimerSendPosition = TimeSendPositionAfterMoving;

            playerMovement(true);
        }

        if(toRemove != null) foreach (string n in infoWorld.keepNames) toRemove.Remove(n);

        int cptPrefab = 0;
        int cptGeom = 0;
       Debug.Log("GenerateGeometries : initGame: " + initGame);

        for (int i = 0; i < infoWorld.names.Count; i++)
        {
            string name = infoWorld.names[i];
            string propId = infoWorld.propertyID[i];
           
            PropertiesGAMA prop = propertyMap[propId];

            GameObject obj = null;
            Debug.Log("name: " + name + " propId: " + propId + " prop:" + prop);

            if (prop.hasPrefab)
            {
                if (initGame || !geometryMap.ContainsKey(name))
                {
                    obj = instantiatePrefab(name, prop, initGame);
                    Debug.Log("initGame: " + name);

                }
                else
                {
                    List<object> o = geometryMap[name];
                    GameObject obj2 = (GameObject)o[0];
                    PropertiesGAMA p = (PropertiesGAMA)o[1];
                    if (p == prop)
                    {
                        obj = obj2;
                    }
                    else
                    {

                        obj2.transform.position = new Vector3(0, -100, 0);
                        geometryMap.Remove(name);
                        if (toFollow != null && toFollow.Contains(obj2))
                            toFollow.Remove(obj2);

                        GameObject.Destroy(obj2);
                        obj = instantiatePrefab(name, prop, initGame);

                    }

                }
                List<int> pt = infoWorld.pointsLoc[cptPrefab].c;
                Vector3 pos = converter.fromGAMACRS(pt[0], pt[1], pt[2]);
                pos.y += prop.yOffsetF;
                float rot = prop.rotationCoeffF * ((0.0f + pt[3]) / parameters.precision) + prop.rotationOffsetF;
                obj.transform.SetPositionAndRotation(pos, Quaternion.AngleAxis(rot, Vector3.up));
                //obj.SetActive(true);
                if(toRemove != null) toRemove.Remove(name);
                cptPrefab++;

            }
            else
            {

                if (polyGen == null)
                {
                    polyGen = PolygonGenerator.GetInstance();
                    polyGen.Init(converter);
                }

                int[] pt = infoWorld.pointsGeom[cptGeom].c.ToArray();
                float yOffset = (0.0f + infoWorld.offsetYGeom[cptGeom]) / (0.0f + parameters.precision);

                if(initGame || !geometryMap.ContainsKey(name))
                {
                    obj = polyGen.GeneratePolygons(false, name, pt, prop, parameters.precision);
                    obj.transform.position = new Vector3(obj.transform.position.x, obj.transform.position.y + yOffset, obj.transform.position.z);
                    instantiateGO(obj, name, prop);
                    if(!initGame) geometryMap.Add(name, new List<object> { obj, prop });
                    if(prop.hasCollider)
                    {
                        MeshCollider mc = obj.AddComponent<MeshCollider>();
                        mc.sharedMesh = obj.GetComponent<MeshFilter>().sharedMesh;
                        if (prop.isGrabable) mc.convex = true;
                    }
                }
                else
                {
                    List<object> o = geometryMap[name];
                    GameObject obj2 = (GameObject)o[0];
                    PropertiesGAMA p = (PropertiesGAMA)o[1];
                    if (p == prop)
                    {
                        obj = obj2;
                        polyGen.UpdatePolygon(obj, pt);
                        if(prop.hasCollider) obj.GetComponent<MeshCollider>().sharedMesh = obj.GetComponent<MeshFilter>().sharedMesh;
                    }
                }
                
                if(toRemove != null) toRemove.Remove(name);
                cptGeom++;
            }


        }

       
        if (infoWorld.attributes != null && infoWorld.attributes.Count > 0)
            ManageAttributes(infoWorld.attributes);


        if (initGame)
            AdditionalInitAfterGeomLoading();

        infoWorld = null;
    }


    bool loadedAlready = false;

    // ############################################ GAMESTATE UPDATER ############################################
    public void UpdateGameState(GameState newState)
    {

        switch (newState)
        {

            case GameState.MENU:
                Debug.Log("SimulationManager: UpdateGameState -> MENU");
                break;

            case GameState.WAITING:
                Debug.Log("SimulationManager: UpdateGameState -> WAITING");
                break;

            case GameState.LOADING_DATA:
                if (!loadedAlready)
                {
                    Debug.Log("SimulationManager: UpdateGameState -> LOADING_DATA");
                    if (ConnectionManager.Instance.getUseMiddleware())
                    {
                        Dictionary<string, string> args = new Dictionary<string, string> {
                             {"id", ConnectionManager.Instance.GetConnectionId() }
                        };
                        ConnectionManager.Instance.SendExecutableAsk("send_init_data", args);
                    }
                    TimerSendInit = TimeSendInit;
                    loadedAlready = true;
                }
                break;

            case GameState.GAME:
                Debug.Log("SimulationManager: UpdateGameState -> GAME");
                loadedAlready = false;
                if (ConnectionManager.Instance.getUseMiddleware())
                {
                    Dictionary<string, string> args = new Dictionary<string, string> {
                         {"id", ConnectionManager.Instance.GetConnectionId() }
                    };
                    ConnectionManager.Instance.SendExecutableAsk("player_ready_to_receive_geometries", args);
                }
                break;

            case GameState.END:
                Debug.Log("SimulationManager: UpdateGameState -> END");
                break;

            case GameState.CRASH:
                Debug.Log("SimulationManager: UpdateGameState -> CRASH");
                break;

            default:
                Debug.Log("SimulationManager: UpdateGameState -> UNKNOWN");
                break;
        }

        currentState = newState;
        OnGameStateChanged?.Invoke(currentState);
    }



    // ############################# INITIALIZERS ####################################


    private void InitGroundParameters()
    {
        Debug.Log("GroundParameters : Beginnig ground initialization");
        if (Ground == null)
        {
           // Debug.LogError("SimulationManager: Ground not set");
            return;
        }
        Vector3 ls = converter.fromGAMACRS(parameters.world[0], parameters.world[1], 0);

        if (ls.z < 0)
            ls.z = -ls.z;
        if (ls.x < 0)
            ls.x = -ls.x;
        ls.y = Ground.transform.localScale.y;

        Ground.transform.localScale = ls;
        Vector3 ps = converter.fromGAMACRS(parameters.world[0] / 2, parameters.world[1] / 2, 0);

        Ground.transform.position = ps;
        Debug.Log("SimulationManager: Ground parameters initialized");
    }


    private void UpdateGameToFollowPosition()
    {
        if (toFollow.Count > 0)
        {


            String names = "";
            String points = "";
            string sep = ConnectionManager.Instance.GetMessageSeparator();

            foreach (GameObject obj in toFollow)
            {
                names += obj.name + sep;
                List<int> p = converter.toGAMACRS3D(obj.transform.position);

                points += p[0] + sep;

                points += p[1] + sep;
                points += p[2] + sep;

            }
            Dictionary<string, string> args = new Dictionary<string, string> {
            {"ids", names  },
            {"points", points},
            {"sep", sep}
            };

            ConnectionManager.Instance.SendExecutableAsk("move_geoms_followed", args);

        }
    }


    // ############################################ UPDATERS ############################################
    private void UpdatePlayerPosition()
    {
        Vector2 vF = new Vector2(Camera.main.transform.forward.x, Camera.main.transform.forward.z);
        Vector2 vR = new Vector2(transform.forward.x, transform.forward.z);
        vF.Normalize();
        vR.Normalize();
        float c = vF.x * vR.x + vF.y * vR.y;
        float s = vF.x * vR.y - vF.y * vR.x;
        int angle = (int)(((s > 0) ? -1.0 : 1.0) * (180 / Math.PI) * Math.Acos(c) * parameters.precision);



      //  Vector3 v = new Vector3(Camera.main.transform.position.x, Camera.main.transform.position.y - yOffsetCamera, Camera.main.transform.position.z);
        Vector3 v = new Vector3(XROrigin.localPosition.x, XROrigin.localPosition.y, XROrigin.localPosition.z);

        List<int> p = converter.toGAMACRS3D(v);
        Dictionary<string, string> args = new Dictionary<string, string> {
            {"id",ConnectionManager.Instance.getUseMiddleware() ? ConnectionManager.Instance.GetConnectionId()  : ("\"" + ConnectionManager.Instance.GetConnectionId() +  "\"") },
            {"x", "" +p[0]},
            {"y", "" +p[1]},
            {"z", "" +p[2]},
            {"angle", "" +angle}
        };
        
            
        ConnectionManager.Instance.SendExecutableAsk("move_player_external", args);

        TimerSendPosition = TimeSendPosition;
    }
   

    private void instantiateGO(GameObject obj, String name, PropertiesGAMA prop)
    {
        obj.name = name;
        if (prop.toFollow)
        {
            toFollow.Add(obj);
        }
        if (prop.tag != null && !string.IsNullOrEmpty(prop.tag))
            obj.tag = prop.tag;

        if (prop.isInteractable)
        {
            if (interactionManager == null)
                interactionManager = GameObject.FindFirstObjectByType<XRInteractionManager>();
          
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
            interaction.interactionManager = interactionManager;
            interaction.ProcessInteractable(XRInteractionUpdateOrder.UpdatePhase.Dynamic);
            interaction.selectEntered.AddListener(SelectInteraction);
            interaction.firstHoverEntered.AddListener(HoverEnterInteraction);
            interaction.hoverExited.AddListener(HoverExitInteraction);

        }
    }



    private GameObject instantiatePrefab(String name, PropertiesGAMA prop, bool initGame)
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
                    Collider c = b.GetComponent<Collider>();
                    if (c == null)
                    {
                        BoxCollider bc = b.AddComponent<BoxCollider>();
                    }
                    // b.tag = obj.tag;
                    // b.name = obj.name;
                    //bc.isTrigger = prop.isTrigger;
                }

            }
            else
            {
                Collider c = obj.GetComponent<Collider>();
                if (c == null)
                {
                    BoxCollider bc = obj.AddComponent<BoxCollider>();
                }

                // bc.isTrigger = prop.isTrigger;
            }
        }
        List<object> pL = new List<object>();
        pL.Add(obj); pL.Add(prop);
        if (!initGame) geometryMap.Add(name, pL);
        instantiateGO(obj, name, prop);
        return obj;
    }



    private void UpdateAgentsList()
    {


        ManageOtherInformation();
        toRemove.Clear();
        toRemove.UnionWith(geometryMap.Keys);

        // foreach (List<object> obj in geometryMap.Values) {
        //((GameObject) obj[0]).SetActive(false);
        //}
        // toRemove.addAll(toRemoveAfter.k);
        GenerateGeometries(false, toRemove);


        // List<string> ids = new List<string>(geometryMap.Keys);
        foreach (string id in toRemove)
        {
            List<object> o = geometryMap[id];
            GameObject obj = (GameObject)o[0];
            obj.transform.position = new Vector3(0, -100, 0);
            geometryMap.Remove(id);
            if (toFollow.Contains(obj))
                toFollow.Remove(obj);
            GameObject.Destroy(obj);
        }
    }

    protected virtual void ManageAttributes(List<Attributes> attributes)
    {

    }

    protected virtual void ManageOtherInformation()
    {

    }

    // ############################################# HANDLERS ########################################
    private void HandleConnectionStateChanged(ConnectionState state)
    {
        Debug.Log("HandleConnectionStateChanged: " + state);
        // player has been added to the simulation by the middleware
        if (state == ConnectionState.AUTHENTICATED)
        {
            Debug.Log("SimulationManager: Player added to simulation, waiting for initial parameters");
            UpdateGameState(GameState.LOADING_DATA);
        }
    }


    protected virtual void OtherUpdate()
    {

    }

    protected virtual void TriggerMainButton()
    {

    }

    protected virtual void HoverEnterInteraction(HoverEnterEventArgs ev)
    {
    }

    protected virtual void HoverExitInteraction(HoverExitEventArgs ev)
    {

    }

    protected virtual void SelectInteraction(SelectEnterEventArgs ev)
    {

    }

    static public void ChangeColor(GameObject obj, Color color)
    {
        Renderer[] renderers = obj.gameObject.GetComponentsInChildren<Renderer>();
        for (int i = 0; i < renderers.Length; i++)
        {
            renderers[i].material.color = color;
        }
    }
    protected virtual void AdditionalInitAfterGeomLoading()
    {
         
    }
    protected virtual void ManageOtherMessages(string content)
    {

    }

    private void HandleServerMessageReceived(String firstKey, String content)
    {

        if (content == null || content.Equals("{}")) return;
        if (firstKey == null)
        {
            if (content.Contains("pong"))
            {
                currentTimePing = 0;
                return;
            }
            else if (content.Contains("pointsLoc"))
                firstKey = "pointsLoc";
            else if (content.Contains("precision"))
                firstKey = "precision";
            else if (content.Contains("properties"))
                firstKey = "properties";
            else if (content.Contains("endOfGame"))
                firstKey = "endOfGame";
            else if (content.Contains("rows"))
                firstKey = "rows";
            else if (content.Contains("wallId"))
                firstKey = "wallId";
            else if (content.Contains("teleportId"))
                firstKey = "teleportId";
            else if (content.Contains("indexX"))
                firstKey = "indexX";
            else if (content.Contains("enableMove"))
                firstKey = "enableMove";
            else if (content.Contains("triggers"))
                firstKey = "triggers";

            else
            {
                ManageOtherMessages(content);
                return;
            }

        }


        switch (firstKey)
        {
            // handle general informations about the simulation
            case "precision":
                parameters = ConnectionParameter.CreateFromJSON(content);
                converter = new CoordinateConverter(parameters.precision, GamaCRSCoefX, GamaCRSCoefY, GamaCRSCoefY, GamaCRSOffsetX, GamaCRSOffsetY, GamaCRSOffsetZ);
                TimeSendPosition = (0.0f + parameters.minPlayerUpdateDuration) / (parameters.precision + 0.0f);
                GameObject loc = (locomotion != null && locomotion.Count > 0) ? locomotion[0] : null;
                if (loc != null)
                {
                    MoveHorizontal h = loc.GetComponent<MoveHorizontal>();
                    MoveVertical v = loc.GetComponent<MoveVertical>();
                   
                    if (h != null)
                    {
                   
                        if (parameters.speedx != -1) h.speed = Convert.ToSingle(parameters.speedx);
                        if (parameters.speedrotation != -1) h.speedRotation = Convert.ToSingle(parameters.speedrotation);
                        h.Strafe = parameters.strafe;
                    }
                    if (v != null)
                    {
                        if ( parameters.miny != -1) v.minY = Convert.ToSingle(parameters.miny);
                        if ( parameters.maxy != -1) v.maxY = Convert.ToSingle(parameters.maxy);
                        if (parameters.speedy != -1) v.Speed = Convert.ToSingle(parameters.speedy);

                    } 
                }

                GameObject moveObj = GameObject.FindGameObjectWithTag("move");
                if (moveObj != null)
                {
                    UnityEngine.XR.Interaction.Toolkit.Samples.StarterAssets.DynamicMoveProvider p = moveObj.GetComponent<UnityEngine.XR.Interaction.Toolkit.Samples.StarterAssets.DynamicMoveProvider>();
                    if (parameters.speedx != -1) p.moveSpeed = Convert.ToSingle(parameters.speedx);
                    p.enableStrafe = parameters.strafe;


                }
                handleGroundParametersRequested = true;
                handleGeometriesRequested = true;

                if ( parameters.cameraclippingfar != -1) Camera.main.farClipPlane = Convert.ToSingle(parameters.cameraclippingfar);

                if (parameters.cameraclippingnear != -1) Camera.main.nearClipPlane = Convert.ToSingle(parameters.cameraclippingnear);


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
            case "endOfGame":
                EndOfGameInfo infoEoG = EndOfGameInfo.CreateFromJSON(content);
                StaticInformation.endOfGame = infoEoG.endOfGame;
                SceneManager.LoadScene("End of Game Menu");
                break;
            case "rows":
                data = DEMData.CreateFromJSON(content);
                break;
            case "wallId":
                dataWall = WallInfo.CreateFromJSON(content);
                break;
            case "teleportId":
                dataTeleport = TeleoportAreaInfo.CreateFromJSON(content);
                break;
            case "indexX":
                dataLoc = DEMDataLoc.CreateFromJSON(content);
                break;
            case "enableMove":
                enableMove = EnableMoveInfo.CreateFromJSON(content);
                break;
            case "triggers":
                infoAnimation = AnimationInfo.CreateFromJSON(content);
                break;
            default:
                ManageOtherMessages(content);
                break;
        }

    }

    private void HandleConnectionAttempted(bool success)
    {
        Debug.Log("SimulationManager: Connection attempt " + (success ? "successful" : "failed"));
        if (success)
        {
            if (IsGameState(GameState.MENU))
            {
                Debug.Log("SimulationManager: Successfully connected to middleware");
                UpdateGameState(GameState.WAITING);
            }
        }
        else
        {
            // stay in MENU state
            Debug.Log("Unable to connect to middleware");
        }
    }

    private void TryReconnect()
    {
        ConnectionManager.Instance.SendExecutableAsk("ping_GAMA", connectionID);

        currentTimePing = maxTimePing;
        Debug.Log("Sent Ping test");

    }

    // ############################################# UTILITY FUNCTIONS ########################################


    public void RestartGame()
    {
        OnGameRestarted?.Invoke();
        SceneManager.LoadScene(SceneManager.GetActiveScene().buildIndex);
    }

    public bool IsGameState(GameState state)
    {
        return currentState == state;
    }


    public GameState GetCurrentState()
    {
        return currentState;
    }


}


// ############################################################
public enum GameState
{
    // not connected to middleware
    MENU,
    // connected to middleware, waiting for authentication
    WAITING,
    // connected to middleware, authenticated, waiting for initial data from middleware
    LOADING_DATA,
    // connected to middleware, authenticated, initial data received, simulation running
    GAME,
    END,
    CRASH
}



public static class Extensions
{
    public static bool TryGetComponent<T>(this GameObject obj, T result) where T : Component
    {
        return (result = obj.GetComponent<T>()) != null;
    }
}