using UnityEngine;

public class CanvasFollower : MonoBehaviour
{
    [SerializeField] Vector3 distanceFromCamera = new Vector3(0, 0, 6);
    [SerializeField] float yRotationOffset = 0;
    [SerializeField] float followSpeed = 8.0f;
    [SerializeField] bool followCameraHeight = false;

    Transform cameraTransform;

    void OnEnable()
    {
        cameraTransform = Camera.main.transform;

        Vector3 targetPosition = cameraTransform.position + (cameraTransform.forward * distanceFromCamera.z) + (cameraTransform.right * distanceFromCamera.x);
        Quaternion targetRotation;
        if (followCameraHeight)
        {
            targetPosition += cameraTransform.up * distanceFromCamera.y;
            targetRotation = Quaternion.LookRotation(targetPosition - cameraTransform.position);
        }
        else
        {
            targetPosition.y = cameraTransform.position.y + distanceFromCamera.y;
            targetRotation = Quaternion.Euler(0, cameraTransform.eulerAngles.y + yRotationOffset, 0);
        }
        transform.position = targetPosition;
        transform.rotation = targetRotation;
    }

    void LateUpdate()
    {
        if (cameraTransform == null) cameraTransform = Camera.main.transform;

        Vector3 targetPosition = cameraTransform.position + (cameraTransform.forward * distanceFromCamera.z) + (cameraTransform.right * distanceFromCamera.x);
        Quaternion targetRotation;
        if (followCameraHeight)
        {
            targetPosition += cameraTransform.up * distanceFromCamera.y;
            targetRotation = Quaternion.LookRotation(targetPosition - cameraTransform.position);
        }
        else
        {
            targetPosition.y = cameraTransform.position.y + distanceFromCamera.y;
            targetRotation = Quaternion.Euler(0, cameraTransform.eulerAngles.y + yRotationOffset, 0);
        }
        transform.position = Vector3.Lerp(transform.position, targetPosition, Time.deltaTime * followSpeed);
        transform.rotation = Quaternion.Slerp(transform.rotation, targetRotation, Time.deltaTime * followSpeed);
    }
}