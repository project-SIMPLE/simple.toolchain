using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class CoordinateConverter 
{

    // optional: define a scale between GAMA and Unity for the location given
    public float GamaCRSCoefX = 1.0f;
    public float GamaCRSCoefY = 1.0f;
    public float GamaCRSCoefZ = 1.0f;
    public float GamaCRSOffsetX = 1.0f;
    public float GamaCRSOffsetY = 1.0f;
    public float GamaCRSOffsetZ = 1.0f;

    public int precision;

    

    public CoordinateConverter(int p, float x, float y, float ox, float oy)
    {
        precision = p;
        GamaCRSCoefX = x;
        GamaCRSCoefY = -1 * y;
        GamaCRSOffsetX = ox;
        GamaCRSOffsetY = oy;
    }

    public CoordinateConverter(int p, float x, float y, float z, float ox, float oy, float oz)
    {
        precision = p;
        GamaCRSCoefX = x;
        GamaCRSCoefY = -1 * y;
        GamaCRSCoefZ = z;
        GamaCRSOffsetX = ox;
        GamaCRSOffsetY = oy;
        GamaCRSOffsetZ = oz;
    }
    public Vector2 fromGAMACRS2D(int x, int y )
    {
        return new Vector2((GamaCRSCoefX * x) / precision + GamaCRSOffsetX, (GamaCRSCoefY * y) / precision + GamaCRSOffsetY);
    }
    public Vector3 fromGAMACRS(int x, int y, int z)
    {
        return new Vector3((GamaCRSCoefX * x) / precision + GamaCRSOffsetX, (GamaCRSCoefZ * z) / precision + GamaCRSOffsetZ, (GamaCRSCoefY * y) / precision + GamaCRSOffsetY);
    }

    public List<int> toGAMACRS(Vector3 pos)
    {
        List<int> position = new List<int>();
        position.Add((int)((pos.x - GamaCRSOffsetX)/ GamaCRSCoefX * precision));
        position.Add((int)((pos.z - GamaCRSOffsetY)/ GamaCRSCoefY * precision));

        return position;
    }

    public List<int> toGAMACRS3D(Vector3 pos)
    {
        List<int> position = new List<int>();
         position.Add((int)((pos.x - GamaCRSOffsetX)/ GamaCRSCoefX * precision));
        position.Add((int)((pos.z - GamaCRSOffsetY) / GamaCRSCoefY * precision));
        position.Add((int)((pos.y - GamaCRSOffsetZ) / GamaCRSCoefZ * precision));

        return position; 
    }


}
