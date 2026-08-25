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

    public double speedx;
    public double speedy; 
    public double speedrotation;
    public double miny;
    public double maxy;
    public double cameraclippingnear;
    public double cameraclippingfar;
    public bool strafe;

    public static ConnectionParameter CreateFromJSON(string jsonString) {
        return JsonUtility.FromJson<ConnectionParameter>(jsonString);
    }

}