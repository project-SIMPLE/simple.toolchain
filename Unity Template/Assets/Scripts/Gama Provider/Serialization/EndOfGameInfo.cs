
using UnityEngine;


[System.Serializable]
public class EndOfGameInfo
{
   
    public string endOfGame;

    public static EndOfGameInfo CreateFromJSON(string jsonString)
    {
        return JsonUtility.FromJson<EndOfGameInfo>(jsonString);
    } 

} 


