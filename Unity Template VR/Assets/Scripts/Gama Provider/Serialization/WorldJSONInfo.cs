using System.Collections.Generic;
using UnityEngine;


[System.Serializable]
public class WorldJSONInfo
{

    public List<int> position;
    public List<string> names;
    public List<string> keepNames;
    public List<string> propertyID;
    public List<GAMAPoint> pointsLoc;

    public List<Attributes> attributes;


    public List<int> offsetYGeom;

    public List<GAMAPoint> pointsGeom;

    public List<int> ranking;
    public List<string> players;
    public int numTokens;
    public bool isInit;

    public static WorldJSONInfo CreateFromJSON(string jsonString)
    {
        return JsonUtility.FromJson<WorldJSONInfo>(jsonString);
    }

    public object getAttributeValue(string name, string attribute)
    {
        return attributes[names.IndexOf(name)];
    }

} 


[System.Serializable]
public class GAMAPoint
{
    public List<int> c;
}


