using System.Collections.Generic;
using UnityEngine;


[System.Serializable]
public class DEMData
{

    public List<Row> rows;
    public string id;
    public int valMax;
    public int sizeX;
    public int sizeY;

    public static DEMData CreateFromJSON(string jsonString)
    {
        return JsonUtility.FromJson<DEMData>(jsonString);
    }

}


[System.Serializable]
public class Row
{
    public List<int> h;
}




