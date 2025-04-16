using System.Collections.Generic;
using UnityEngine;


[System.Serializable]
public class ConnectionParameter
{
    public int precision;
    public List<int> position;
    public List<int> world;

    public List<string> hotspots;
    public int minPlayerUpdateDuration;

    public static ConnectionParameter CreateFromJSON(string jsonString) {
        return JsonUtility.FromJson<ConnectionParameter>(jsonString);
    }

}