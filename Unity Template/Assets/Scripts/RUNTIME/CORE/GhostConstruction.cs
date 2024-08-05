using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class GhostConstruction : MonoBehaviour
{
    [SerializeField] LayerMask collideLayerMask;
    [SerializeField] private LayerMask connectorLayerMask;
    [SerializeField] private float connectorCheckRadius = 1;
    [SerializeField] private bool useIdentityRotation = false;

    [Header("Ghost Construction Material Settings")]
    [SerializeField] private Material validMaterial;
    [SerializeField] private Material invalidMaterial;


    private bool buildable;
    private bool collide = false;

    // Getter
    public bool IsBuildable {
        get { return buildable; }
    }
    public bool UseIdentityRotation {
        get { return useIdentityRotation; }
    }

    void Start()
    {
        ghostifyModel(gameObject, invalidMaterial);
    }

    void Update()
    {
        UpdateConstructionValidity();
        if (buildable) ghostifyModel(gameObject, validMaterial);
        else ghostifyModel(gameObject, invalidMaterial);
    }

    void Awake()
    {
        collide = false;
    }

    void OnTriggerEnter(Collider other)
    {
        if (collideLayerMask == (collideLayerMask | (1 << other.gameObject.layer)))
            collide = true;           
    }

    void OnTriggerStay(Collider other)
    {
        if (collideLayerMask == (collideLayerMask | (1 << other.gameObject.layer)))
            collide = true;           
    }

    void OnTriggerExit(Collider other)
    {
        if (collideLayerMask == (collideLayerMask | (1 << other.gameObject.layer)))
            collide = false;
    }

    private void UpdateConstructionValidity(){
        Collider[] colliders = Physics.OverlapSphere(transform.position, connectorCheckRadius, connectorLayerMask);
        if(colliders.Length > 0){
            FindConnector(colliders);
        }
        else
        {
            buildable = false;
        }
    }

    private void FindConnector(Collider[] colliders){
        Connector bestsurfaceConnector = null;
        foreach(Collider collider in colliders){
            Connector connector = collider.GetComponent<Connector>();
            if(connector.canConnectTo && !connector.transform.IsChildOf(transform)){
                bestsurfaceConnector = connector;
                break;
            }
        }
        if (bestsurfaceConnector)
        {
            var constructionConnector = GetComponentInChildren<Connector>();
            SnapTwoConnector(bestsurfaceConnector, constructionConnector);
        }
        else
        {
            buildable = false;
        }
    }

    private void SnapTwoConnector(Connector surfaceConnector, Connector constructionConnector){  
        if (surfaceConnector.Type == constructionConnector.Type)
        {
            transform.position = surfaceConnector.transform.position - (constructionConnector.transform.position - transform.position);
            transform.rotation = useIdentityRotation ? Quaternion.identity : surfaceConnector.transform.rotation;
            //buildable = collidingObjects==0 ? true : false;
            buildable = !collide;
        }
    }

    // update ghost construction material
    private void ghostifyModel(GameObject model, Material ghostMaterial = null){
        if(ghostMaterial != null){
            MeshRenderer[] meshRenderers = model.GetComponentsInChildren<MeshRenderer>();
            foreach(MeshRenderer meshRenderer in meshRenderers){
                meshRenderer.material = ghostMaterial;
            }
        }
    }

}
