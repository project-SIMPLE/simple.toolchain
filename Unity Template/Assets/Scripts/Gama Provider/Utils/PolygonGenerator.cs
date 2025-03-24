using System;
using System.Collections.Generic;
using UnityEngine;

public class PolygonGenerator
{
    CoordinateConverter converter;

    float offsetYBackgroundGeom;

    private static PolygonGenerator instance;

    public Mesh surroundMesh;
    public Mesh bottomMesh;
    public Mesh topMesh;



    public PolygonGenerator() { }

    public void Init(CoordinateConverter c)
    {
        converter = c;
    }

    public static PolygonGenerator GetInstance()
    {
        if (instance == null)
        {
            instance = new PolygonGenerator();
        }
        return instance;
    }

    public static void DestroyInstance()
    {
        instance = null;
    }



    public GameObject GeneratePolygons(bool editMode, String name, int[] points, PropertiesGAMA prop, int precision)
    {

    
        List <Vector2> pts = new List<Vector2>();
        for (int i = 0; i < points.Length - 1; i = i+2)
        {
            Vector2 p = converter.fromGAMACRS2D(points[i], points[i + 1]);
            pts.Add(p);
        }
        Vector2[] MeshDataPoints = pts.ToArray();
        //Color32 col = new Color32(BitConverter.GetBytes(prop.color[0])[0], BitConverter.GetBytes(prop.color[1])[0],
        //          BitConverter.GetBytes(prop.color[2])[0], BitConverter.GetBytes(prop.color[3])[0]);

       Color32 col = Color.black;
       Material mat = null;
        if (prop.visible)
        {
            if (prop.material != null && prop.material != "")
            {
                mat = Resources.Load<Material>(prop.material);
            }

            if (prop.red != -1 )
            {
                col = new Color32(BitConverter.GetBytes(prop.red)[0], BitConverter.GetBytes(prop.green)[0],
                   BitConverter.GetBytes(prop.blue)[0], BitConverter.GetBytes(prop.alpha)[0]);
            } else
            {
               if (mat != null)
                {
                    col = mat.color;
                }
            } 
           
        }
        GameObject obj = GeneratePolygon(editMode, name, MeshDataPoints, ((float)prop.height) / precision, mat, col);
        
        if (!prop.visible)
        {
            MeshRenderer r =  obj.GetComponent<MeshRenderer>();
            if (r != null) r.enabled = false;
            foreach (MeshRenderer rr in obj.GetComponentsInChildren<MeshRenderer>())
            {
                if (rr != null) rr.enabled = false;

            }
            LineRenderer lr = obj.GetComponent<LineRenderer>();
            if (lr != null)
                lr.enabled = false;
        }
        return obj;

    }


    // Start is called before the first frame update
    GameObject GeneratePolygon(bool editMode, String name, Vector2[] meshDataPoints, float extrusionHeight, Material mat, Color32 color)
    {
      
        GameObject polyExtruderGO = new GameObject(name);

        // Optionally offset the Y position
        Vector3 pos = polyExtruderGO.transform.position;
        pos.y += offsetYBackgroundGeom;
        polyExtruderGO.transform.position = pos;

        // Add PolyExtruderLight and call createPrism
        PolyExtruderLight polyExtruderLight = polyExtruderGO.AddComponent<PolyExtruderLight>();
        // The final parameter is the material, which can be null
        polyExtruderLight.createPrism(
            name,
            extrusionHeight,
            meshDataPoints,
            color,
            mat
        );

        return polyExtruderGO;
    }


    /// <summary>
    /// Update the mesh of a polygon GameObject with PolyExtruderLight.
    /// </summary>
    public void UpdatePolygon(GameObject obj, int[] points)
    {
        PolyExtruderLight polyExtruderGO = obj.GetComponent<PolyExtruderLight>();
        MeshFilter meshFilter = obj.GetComponent<MeshFilter>();

        int pointCount = points.Length;
        Vector2[] pts = new Vector2[pointCount / 2]; // Allocate array with required size

        for (int i = 0; i < pointCount - 1; i += 2)
        {
            pts[i / 2] = converter.fromGAMACRS2D(points[i], points[i + 1]);
        }

        if (polyExtruderGO != null)
        {
            polyExtruderGO.updatePrism(meshFilter, pts);
        }
    }


}


