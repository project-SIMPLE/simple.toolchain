using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class ConstructionRemover : MonoBehaviour
{
    [SerializeField] private LayerMask connectorLayerMask;
    [HideInInspector] public BuildSystemManager buildSystemManager;


    public void RemoveConstruction(int constructionID)
    {
        // increase construction quantity by 1
        if (buildSystemManager) buildSystemManager.Constructions[constructionID].IncreaseQuantity();
        // re-enable connectors
        const float colliderRadius = 0.5f;
        Collider[] colliders = Physics.OverlapSphere(transform.position, colliderRadius * transform.lossyScale.x);
        foreach (Collider collider in colliders)
        {
            Connector foundConnector = collider.GetComponent<Connector>();
            if (foundConnector)
            {
                foundConnector.canConnectTo = true;
            }
        }
        // destroy object
        Destroy(this.gameObject);
        
    }
}
