using UnityEngine;
using UnityEngine.InputSystem;

public class MoveVertical  : InputData
{
    
    public float Speed = 2.0f; 
   
    public float minY = 0.0f;
    public float maxY = 150.0f;

    [SerializeField] private InputActionReference Stick;

     
    private void FixedUpdate()
    {
        if (SimulationManager.Instance.IsGameState(GameState.GAME))
        {
            MoveVertially();
        }
    }

    private void MoveVertially() { 
        Vector2 val = Stick.action.ReadValue<Vector2>();
        transform.parent.Translate(Vector3.up * Time.fixedDeltaTime * Speed * val.y);
        if (transform.parent.position.y < minY)
        {
            Vector3 v = new Vector3(transform.parent.position.x, minY, transform.parent.position.z);
            transform.parent.position = v ;
        }
        else if (transform.parent.position.y > maxY)
        {
            Vector3 v = new Vector3(transform.parent.position.x, maxY, transform.parent.position.z);
            transform.parent.position = v ;
        }
    }
}    