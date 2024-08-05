using System;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.XR.Interaction.Toolkit;


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



    public GameObject GeneratePolygons(bool editMode, String name, List<int> points, PropertiesGAMA prop, int precision)
    {
   
        List<Vector2> pts = new List<Vector2>();
        for (int i = 0; i < points.Count - 1; i = i+2)
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
            col = new Color32(BitConverter.GetBytes(prop.red)[0], BitConverter.GetBytes(prop.green)[0],
                    BitConverter.GetBytes(prop.blue)[0], BitConverter.GetBytes(prop.alpha)[0]);
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
    GameObject GeneratePolygon(bool editMode, String name, Vector2[] MeshDataPoints, float extrusionHeight, Material mat, Color32 color)
    {
        bool isUsingBottomMeshIn3D = false;
        bool isOutlineRendered = true;
        bool is3D = extrusionHeight != 0.0;

       
        // create new GameObject (as a child)
        GameObject polyExtruderGO = new GameObject();
       

        // reference to setup example poly extruder 
        PolyExtruder polyExtruder;

        
        // add PolyExtruder script to newly created GameObject and keep track of its reference
        polyExtruder = polyExtruderGO.AddComponent<PolyExtruder>();
       
        // global PolyExtruder configurations
        polyExtruder.isOutlineRendered = isOutlineRendered;
        Vector3 pos = polyExtruderGO.transform.position;
        pos.y += offsetYBackgroundGeom;
        polyExtruderGO.transform.position = pos;
        polyExtruder.createPrism(editMode, name, extrusionHeight, MeshDataPoints, color, mat, is3D, isUsingBottomMeshIn3D);
        surroundMesh = polyExtruder.surroundMesh;
        bottomMesh = polyExtruder.bottomMesh;
        topMesh = polyExtruder.topMesh;
        polyExtruderGO.name = name;
        return polyExtruderGO;
    }

  
}


