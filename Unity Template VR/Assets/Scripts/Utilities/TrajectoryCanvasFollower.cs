using UnityEngine;

public class TrajectoryCanvasFollower : MonoBehaviour
{
    [Header("References")]
    public Transform targetObject;

    [Header("Trajectory Settings")]
    [Tooltip("X: Right Offset, Y: Height Offset, Z: Forward Distance (Arc Radius)")]
    public Vector3 distanceFromCamera = new Vector3(2f, 0f, 5f);
    
    [Tooltip("How fast the canvas slides along the track to catch up.")]
    public float slideSpeed = 5f;

    Transform playerCamera;

    void Start()
    {
        playerCamera = Camera.main.transform;
    }

    public void SetPosition()
    {
        if (playerCamera == null || targetObject == null) return;

        Vector3 directionToTarget = targetObject.position - playerCamera.position;
        
        directionToTarget.y = 0; 
        
        if (directionToTarget.sqrMagnitude < 0.001f) return; 
        Vector3 customForward = directionToTarget.normalized;

        Vector3 customRight = Vector3.Cross(Vector3.up, customForward).normalized;

        Vector3 idealPosition = playerCamera.position 
                              + (customForward * distanceFromCamera.z) 
                              + (customRight * distanceFromCamera.x);
        
        idealPosition.y = playerCamera.position.y + distanceFromCamera.y;

        Vector3 targetOffset = idealPosition - playerCamera.position;
        transform.position = playerCamera.position + targetOffset;

        transform.rotation = Quaternion.LookRotation(playerCamera.position - transform.position);
    }
}