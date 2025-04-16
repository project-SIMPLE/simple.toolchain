using System.Collections.Generic;
using UnityEngine;


[System.Serializable]
public class EnableMoveInfo
{

    public bool enableMove;

    public static EnableMoveInfo CreateFromJSON(string jsonString)
    {
        return JsonUtility.FromJson<EnableMoveInfo>(jsonString);
    } 

} 


