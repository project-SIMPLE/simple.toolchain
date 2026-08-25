using System;
using Unity.Collections;
using Unity.Jobs;
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

    public GameObject GeneratePolygons(bool editMode, string name, int[] points, PropertiesGAMA prop, int precision)
    {
        int pointCount = points.Length;
        int vectorCount = pointCount / 2;

        // Copy the managed array into a NativeArray for Burst processing
        NativeArray<int> nativePoints = new NativeArray<int>(points, Allocator.TempJob);
        NativeArray<Vector2> nativeResults = new NativeArray<Vector2>(vectorCount, Allocator.TempJob);

        // Schedule the Burst-compiled job to convert points in parallel using full conversion logic.
        var job = new CoordinateConversionJob
        {
            points = nativePoints,
            results = nativeResults,
            coefX = converter.GamaCRSCoefX,
            coefY = converter.GamaCRSCoefY,
            offsetX = converter.GamaCRSOffsetX,
            offsetY = converter.GamaCRSOffsetY,
            precision = converter.precision
        };

        JobHandle handle = job.Schedule(vectorCount, 64);
        handle.Complete();

        // Copy results back to a managed array.
        Vector2[] pts = nativeResults.ToArray();

        // Dispose of the NativeArrays.
        nativePoints.Dispose();
        nativeResults.Dispose();

        return GeneratePolygons(editMode, name, pts, prop, precision);
    }

    /// <summary>
    /// Generate polygons from an array of Vector2 coordinates.
    /// </summary>
    public GameObject GeneratePolygons(bool editMode, string name, Vector2[] meshDataPoints, PropertiesGAMA prop, int precision)
    {
        // Prepare color from GAMA properties.
        Color32 col = Color.black;
        if (prop.visible)
        {
            col = new Color32(
                BitConverter.GetBytes(prop.red)[0],
                BitConverter.GetBytes(prop.green)[0],
                BitConverter.GetBytes(prop.blue)[0],
                BitConverter.GetBytes(prop.alpha)[0]);
        }

        // Load a custom material if specified.
        Material mat = null;
        if (prop.visible && !string.IsNullOrEmpty(prop.material))
        {
            // e.g. "Assets/Materials/MyMaterial" (without extension) if placed in the Resources folder.
            mat = Resources.Load<Material>(prop.material);
        }

        // Calculate the extrusion height.
        float extrHeight = (float)prop.height / precision;

        // Create the extruded polygon object.
        GameObject obj = GeneratePolygon(name, meshDataPoints, extrHeight, col, mat);

        // Hide mesh if not visible.
        if (!prop.visible)
        {
            MeshRenderer r = obj.GetComponent<MeshRenderer>();
            if (r != null)
                r.enabled = false;
            foreach (MeshRenderer rr in obj.GetComponentsInChildren<MeshRenderer>())
            {
                if (rr != null)
                    rr.enabled = false;
            }
        }

        return obj;
    }

    /// <summary>
    /// Internal helper that creates the GameObject with PolyExtruderLight.
    /// </summary>
    private GameObject GeneratePolygon(string name, Vector2[] meshDataPoints, float extrusionHeight, Color32 color, Material mat)
    {
        // Create a new GameObject with the given name.
        GameObject polyExtruderGO = new GameObject(name);

        // Optionally offset the Y position.
        Vector3 pos = polyExtruderGO.transform.position;
        pos.y += offsetYBackgroundGeom;
        polyExtruderGO.transform.position = pos;

        // Add PolyExtruderLight and call createPrism.
        PolyExtruderLight polyExtruderLight = polyExtruderGO.AddComponent<PolyExtruderLight>();
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
        int vectorCount = pointCount / 2;

        NativeArray<int> nativePoints = new NativeArray<int>(points, Allocator.TempJob);
        NativeArray<Vector2> nativeResults = new NativeArray<Vector2>(vectorCount, Allocator.TempJob);

        var job = new CoordinateConversionJob
        {
            points = nativePoints,
            results = nativeResults,
            coefX = converter.GamaCRSCoefX,
            coefY = converter.GamaCRSCoefY,
            offsetX = converter.GamaCRSOffsetX,
            offsetY = converter.GamaCRSOffsetY,
            precision = converter.precision
        };

        JobHandle handle = job.Schedule(vectorCount, 64);
        handle.Complete();

        Vector2[] pts = nativeResults.ToArray();

        nativePoints.Dispose();
        nativeResults.Dispose();

        if (polyExtruderGO != null)
        {
            polyExtruderGO.updatePrism(meshFilter, pts);
        }
    }

}


