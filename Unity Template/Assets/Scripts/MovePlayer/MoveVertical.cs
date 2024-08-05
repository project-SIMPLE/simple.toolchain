using UnityEngine;
using UnityEngine.XR.Interaction.Toolkit;
using UnityEngine.XR;

public class MoveVertical  : InputData
{
    
    public float Speed = 2.0f; 
    public bool RightHand = false;

    public float minY = 0.0f;
    public float maxY = 150.0f;

    public InputHelpers.Axis2D stick = InputHelpers.Axis2D.PrimaryAxis2D;

   
    private void FixedUpdate()
    {
        if (SimulationManager.Instance.IsGameState(GameState.GAME))
        {
            MoveVertially();
        }
    }

    private void MoveVertially() {
        InputDevice hand = RightHand ? _rightController : _leftController;
        Vector2 val;
        hand.TryReadAxis2DValue(stick, out val);
        transform.Translate(Vector3.up * Time.fixedDeltaTime * Speed * val.y);
        if (transform.position.y < minY)
        {
            Vector3 v = new Vector3(transform.position.x, minY, transform.position.z);
            transform.position = v ;
        }
        else if (transform.position.y > maxY)
        {
            Vector3 v = new Vector3(transform.position.x, maxY, transform.position.z);
            transform.position = v ;
        }
    }
}   