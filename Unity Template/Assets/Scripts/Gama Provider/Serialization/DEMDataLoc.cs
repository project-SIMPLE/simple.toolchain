using System.Collections.Generic;
using UnityEngine;


[System.Serializable]
public class DEMDataLoc
{
    public List<Row> rows;
    public string id;
    public int indexX; 
    public int indexY;
    public int valMax;
  

    public static DEMDataLoc CreateFromJSON(string jsonString)
    {
        return JsonUtility.FromJson<DEMDataLoc>(jsonString);
    }
     
} 
 




