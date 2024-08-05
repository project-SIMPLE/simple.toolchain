using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class TestTerrain : MonoBehaviour
{
    // Start is called before the first frame update
    void Start()
    {
        Debug.Log("lala"); 
        Terrain t = Terrain.activeTerrain;

        t.terrainData.size = new Vector3(1000, 200, 1000);
        float[,] heights = new float[t.terrainData.heightmapResolution, t.terrainData.heightmapResolution];
        int x = 0;
       for (int j = 0; j < t.terrainData.heightmapResolution; j++)
        {
            for (int i = 0; i < t.terrainData.heightmapResolution; i++)

            {
              heights[i, j] = Random.Range(0.0f,1.0f);
              //  Debug.Log("height: " + heights[i, j]);
            }
            x++;
        }

        // set the new height
        t.terrainData.SetHeights(0, 0, heights);
    }

    // Update is called once per frame
    void Update()
    {
        
    }
}
