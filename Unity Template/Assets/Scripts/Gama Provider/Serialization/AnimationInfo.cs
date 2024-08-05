using System.Collections.Generic;
using UnityEngine;


[System.Serializable]
public class AnimationInfo
{

    public List<string> names;
   

    public List<string> triggers;
    public List<ParameterVal> parameters;

    public static AnimationInfo CreateFromJSON(string jsonString)
    {
        return JsonUtility.FromJson<AnimationInfo>(jsonString);
    } 

}


[System.Serializable]
public class ParameterVal
{
    public string key;
    public float floatVal;
    public int intVal;
    public bool boolVal;
    public string type;

    public object getVal()
    {
        if ("int".Equals(type))
            return intVal;
        else if ("float".Equals(type))
            return intVal;
        return boolVal;
    }
        
}


