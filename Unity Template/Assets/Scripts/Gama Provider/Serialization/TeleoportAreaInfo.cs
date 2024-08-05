using System.Collections.Generic;
using UnityEngine;



[System.Serializable]
public class TeleoportAreaInfo
{
    public List<int> offsetYGeom;

    public List<GAMAPoint> pointsGeom;

    public int height;

    public string teleportId;

    public static TeleoportAreaInfo CreateFromJSON(string jsonString)
    {
        return JsonUtility.FromJson<TeleoportAreaInfo>(jsonString);
    }

}