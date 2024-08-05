using UnityEngine;
using UnityEngine.XR.Interaction.Toolkit;
using UnityEngine.XR;




public class MoveHorizontal : InputData
{
    public bool RightHand = true;

    [SerializeField] private float speed = 2.0f;
    [SerializeField] private float speedRotation = 10.0f;
    [SerializeField] private bool Strafe = false;
    public InputHelpers.Axis2D stick = InputHelpers.Axis2D.PrimaryAxis2D;

    // ############################################################

    private void FixedUpdate()
    {
        if (SimulationManager.Instance.IsGameState(GameState.GAME))
            MoveHorizontally();
    }

    // ############################################################

    private void MoveHorizontally()
    {
        InputDevice hand = RightHand ? _rightController : _leftController;
        Vector2 val;
        hand.TryReadAxis2DValue(stick, out val);
        Vector3 vectF = Camera.main.transform.forward;
        vectF.y = 0;
        vectF = Vector3.Normalize(vectF);

        transform.position += (vectF * speed * Time.fixedDeltaTime * val.y);

        if (Strafe)
        {
            Vector3 vectR = Camera.main.transform.right;
            vectR.y = 0;
            vectR = Vector3.Normalize(vectR);

            transform.position += (vectR * speed * Time.fixedDeltaTime * val.x);
        }
        else
        {

            transform.Rotate(new Vector3(0, 1, 0), Time.fixedDeltaTime * speedRotation * val.x);
        }
    }
}
