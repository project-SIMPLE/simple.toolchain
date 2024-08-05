using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class BuildSystem : MonoBehaviour
{
    [Header("Build Objects")]
    [SerializeField] private List<GameObject> turretObjects = new List<GameObject>();
    [SerializeField] private List<GameObject> sandBagsBarrierObjects = new List<GameObject>();
    [SerializeField] private List<GameObject> lockObjects = new List<GameObject>();
    [SerializeField] private List<GameObject> lakeObjects = new List<GameObject>();
    [Header("Build Settings")]
    [SerializeField] public SelectedBuildType currentVirtualConstruction;
    [SerializeField] private LayerMask connectorLayer;
    [Header("Ghost Settings")]
    [SerializeField] private Material ghostMaterialValid;
    [SerializeField] private Material ghostMaterialInvalid;
    [SerializeField] private float connectorOverlapRadius = 1;
    [SerializeField] private float maxGroundAngle = 45f;
    [Header("Internal State")]
    [SerializeField] public bool building = false;
    [SerializeField] private bool isBuilding = false;
    [SerializeField] private int currentBuildingIndex;
    private GameObject ghostBuildGameobject;
    private bool isGhostInValidPosition = false;
    private Transform ModelParent = null;

    public bool canPlace = true;
    private void Update()
    {
        if (building)
        {
            isBuilding = !isBuilding;
            building = !building;
        }
        if (isBuilding)
        {
            StartBuilding();
            //Debug.Log("test " +canPlace);
            if (Input.GetMouseButtonDown(0) && canPlace)
            {
                Build();
            }
            if (Input.GetKeyDown(KeyCode.B))
            {
                Destroy(ghostBuildGameobject);
                ghostBuildGameobject = null;
                isBuilding = false;
                building = false;
            }
        }
        else if (ghostBuildGameobject)
        {
            Destroy(ghostBuildGameobject);
            ghostBuildGameobject = null;
        }
    }

    private void StartBuilding()
    {
        GameObject currentBuild = getCurrentBuild();
        createGhostPrefab(currentBuild);
        moveGhostPrefabToRaycast();
        checkBuildValidity();
    }

    private void createGhostPrefab(GameObject currentBuild)
    {
        if (ghostBuildGameobject == null)
        {
            ghostBuildGameobject = Instantiate(currentBuild);

            ModelParent = ghostBuildGameobject.transform.GetChild(0);

            ghostifyModel(ModelParent, ghostMaterialValid);
            ghostifyModel(ghostBuildGameobject.transform);
        }
    }

    private void moveGhostPrefabToRaycast()
    {
        Ray ray = Camera.main.ScreenPointToRay(Input.mousePosition);
        RaycastHit hit;
        if (Physics.Raycast(ray, out hit, Mathf.Infinity))
        {
            if (currentVirtualConstruction == SelectedBuildType.turret)
                ghostBuildGameobject.transform.position = new Vector3(hit.point.x, hit.point.y + ghostBuildGameobject.transform.localScale.y, hit.point.z);
            else
                ghostBuildGameobject.transform.position = new Vector3(hit.point.x, hit.point.y, hit.point.z);
        }
    }

    private void checkBuildValidity()
    {
        Collider[] colliders = Physics.OverlapSphere(ghostBuildGameobject.transform.position, connectorOverlapRadius, connectorLayer);
        if (colliders.Length > 0)
        {
            ghostConnectBuild(colliders);
        }
        else
        {
            ghostSeperateBuild();
        }
    }

    //private void OnDrawGizmos()
    //{
    //    Gizmos.color = Color.green;
    //    Gizmos.DrawWireSphere(ghostBuildGameobject.transform.position, transform.lossyScale.x);
    //}

    private bool CheckCollisionWithBuiltObjects(Vector3 position, Vector3 size)
    {
        float radius = Mathf.Max(size.x, size.y, size.z);
        Collider[] colliders = Physics.OverlapSphere(position, radius, LayerMask.GetMask("BuiltObjects"));
        return colliders.Length > 0;
    }

    private void ghostConnectBuild(Collider[] colliders)
    {
        Connector bestConnector = null;

        //bool checkCollision = CheckCollisionWithBuiltObjects(ghostBuildGameobject.transform.position, ghostBuildGameobject.transform.localScale);
        foreach (Collider collider in colliders)
        {
            Connector connector = collider.GetComponent<Connector>();
            if (connector.canConnectTo)
            {
                bestConnector = connector;
                break;
            }
        }
        if (bestConnector == null || bestConnector.canConnectTo || canPlace == false)
        {
            ghostifyModel(ModelParent, ghostMaterialInvalid);
            isGhostInValidPosition = false;
            return;
        }

        snapGhostPrefabToConnector(bestConnector);
    }
    private void snapGhostPrefabToConnector(Connector connector)
    {
        Transform ghostConnector = findSnapConnector(connector.transform, ghostBuildGameobject.transform.GetChild(1));
        ghostBuildGameobject.transform.position = connector.transform.position - (ghostConnector.position - ghostBuildGameobject.transform.position);
        ghostifyModel(ModelParent, ghostMaterialValid);
        isGhostInValidPosition = true;
    }

    private void ghostSeperateBuild()
    {
        Ray ray = Camera.main.ScreenPointToRay(Input.mousePosition);
        RaycastHit hit;
        if (Physics.Raycast(ray, out hit))
        {
            if (currentVirtualConstruction == SelectedBuildType.turret || currentVirtualConstruction == SelectedBuildType.sluiceGate)
            {
                ghostifyModel(ModelParent, ghostMaterialInvalid);
                isGhostInValidPosition = false;
                return;
            }
            // if (hit.collider.transform.root.CompareTag("Buildables"))
            // {
            //     ghostifyModel(ModelParent, ghostMaterialInvalid);
            //     isGhostInValidPosition = false;
            //     return;
            // }

            if (Vector3.Angle(hit.normal, Vector3.up) < maxGroundAngle)
            {
                ghostifyModel(ModelParent, ghostMaterialValid);
                isGhostInValidPosition = true;
            }
            else
            {
                ghostifyModel(ModelParent, ghostMaterialInvalid);
                isGhostInValidPosition = false;
            }
        }
    }

    private Transform findSnapConnector(Transform snapConnector, Transform ghostConnectorParent)
    {
        Connector[] connectors = ghostConnectorParent.GetComponentsInChildren<Connector>();
        foreach (Connector connector in connectors)
        {
            if (connector.connectorPosition == ConnectorPosition.bottom)
            {
                return connector.transform;
            }
        }
        return null;
    }



    private void ghostifyModel(Transform modelParent, Material ghostMaterial = null)
    {
        if (ghostMaterial != null)
        {
            MeshRenderer[] meshRenderers = modelParent.GetComponentsInChildren<MeshRenderer>();
            foreach (MeshRenderer meshRenderer in meshRenderers)
            {
                meshRenderer.material = ghostMaterial;
            }
        }
        else
        {
            Collider[] modelColliders = modelParent.GetComponentsInChildren<Collider>();
            foreach (Collider modelCollider in modelColliders)
            {
                if(modelCollider is SphereCollider)
                {
                    modelCollider.enabled = false;
                }
            }
        }
    }

    private GameObject getCurrentBuild()
    {
        switch (currentVirtualConstruction)
        {
            case SelectedBuildType.lake:
                return lakeObjects[currentBuildingIndex];
            case SelectedBuildType.turret:
                return turretObjects[currentBuildingIndex];
            case SelectedBuildType.sluiceGate:
                return lockObjects[currentBuildingIndex];
            case SelectedBuildType.sandBag:
                return sandBagsBarrierObjects[currentBuildingIndex];
        }
        return null;
    }

    private void Build()
    {
        if (ghostBuildGameobject != null && isGhostInValidPosition)
        {
            GameObject newBuild = Instantiate(getCurrentBuild(), ghostBuildGameobject.transform.position, ghostBuildGameobject.transform.rotation);

            Destroy(ghostBuildGameobject);
            ghostBuildGameobject = null;

            isBuilding = false;

            Connector[] connectors = newBuild.GetComponentsInChildren<Connector>();

            foreach (Connector connector in connectors)
            {
                connector.UpdateConnector(false);
            }
        }
    }

}

[System.Serializable]
public enum SelectedBuildType
{
    lake,
    turret,
    sluiceGate,
    sandBag,
}
