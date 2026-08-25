using UnityEngine;
using UnityEngine.InputSystem;





public class MoveHorizontal : InputData
{
    
    [SerializeField] public float speed = 2.0f;
    [SerializeField] public float speedRotation = 10.0f;
    [SerializeField] public bool Strafe = false;
    [SerializeField] private InputActionReference Stick;
     
    // ############################################################

    private void FixedUpdate()
    {
        if (SimulationManager.Instance.IsGameState(GameState.GAME))
            MoveHorizontally();
    }

    // ############################################################

    private void MoveHorizontally()
    {
        Vector2 val = Stick.action.ReadValue<Vector2>();
        Vector3 vectF = Camera.main.transform.forward;
        vectF.y = 0;
        vectF = Vector3.Normalize(vectF);

        transform.parent.position += (vectF * speed * Time.fixedDeltaTime * val.y);

        if (Strafe)
        {
            Vector3 vectR = Camera.main.transform.right;
            vectR.y = 0;
            vectR = Vector3.Normalize(vectR);

            transform.parent.position += (vectR * speed * Time.fixedDeltaTime * val.x);
        }
        else
        {

            transform.parent.Rotate(new Vector3(0, 1, 0), Time.fixedDeltaTime * speedRotation * val.x);
        }
    }
}
