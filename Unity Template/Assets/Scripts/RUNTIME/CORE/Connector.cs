using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Connector : MonoBehaviour
{
    [SerializeField] private ConnectorType type;
    
    public ConnectorPosition connectorPosition;
    public SelectedBuildType connectorParentType;

    [HideInInspector] public bool canConnectTo = true;


    //Getters
    public ConnectorType Type {
        get { return type; }
    }

    private void OnDrawGizmos()
    {
        Gizmos.color = canConnectTo ? Color.blue : Color.red;
        Gizmos.DrawWireSphere(transform.position, transform.lossyScale.x / 2f);
    }

    public void UpdateConnector(bool value)
    {
        const float colliderRadius = 0.5f;
        Collider[] colliders = Physics.OverlapSphere(transform.position, colliderRadius * transform.lossyScale.x);
        foreach (Collider collider in colliders)
        {
            Connector foundConnector = collider.GetComponent<Connector>();
            if (foundConnector)
            {
                foundConnector.canConnectTo = false;
            }
        }
        canConnectTo = value;
    }

    /*public void updateConnectors(bool rootCall = false)
    {
        const float colliderRadius = 0.5f;
        
        Collider[] colliders = Physics.OverlapSphere(transform.position, colliderRadius * transform.lossyScale.x);
        isConnectedToLake = false;

        foreach (Collider collider in colliders)
        {
            if (collider == GetComponent<Collider>())
            {
                continue;
            }

            Connector foundConnector = collider.GetComponent<Connector>();
            if (foundConnector != null && foundConnector.connectorParentType == SelectedBuildType.lake)
            {
                isConnectedToLake = true;
            }
            
            if (rootCall && foundConnector != null)
            {
                foundConnector.updateConnectors();
            }
        }
        
        if (isConnectedToLake)
        {
            canConnectTo = false;
        }
    }*/


}

[System.Serializable]
public enum ConnectorPosition{
    bottom,
}

[System.Serializable]
public enum ConnectorType {
    lake,
    surface,
}

