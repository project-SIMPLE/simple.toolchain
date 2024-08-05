using System.Collections.Generic;
using UnityEngine;



[System.Serializable]
public class WallInfo
{
    public string wallId;

    public List<int> offsetYGeom;

    public int height;

    public List<GAMAPoint> pointsGeom;

    public static WallInfo CreateFromJSON(string jsonString)
    {
        return JsonUtility.FromJson<WallInfo>(jsonString);
    }

}
