/*
 * PolyExtruderLight.cs
 *
 * Description: Lightweight implementation of the original PolyExtruder.cs class
 *              combining the three original meshes (bottom, top, surround) into one mesh at runtime.
 *
 * New in this version:
 * - Accepts a custom Material.
 * - Falls back to "Universal Render Pipeline/Lit" if no material is provided.
 * - Applies the provided color to the final material.
 *
 * ATTENTION: No holes-support in polygon extrusion (Prism 3D) implemented!
 *
 * Supported Unity version: 2022.3.20f1 Personal (tested)
 *
 * Version: 2024.11
 * Author: Nico Reski
 * GitHub: https://github.com/nicoversity
 *
 */

using UnityEngine;
using System.Collections.Generic;
using Unity.Collections;
using Unity.Jobs;
using Unity.Burst;

public class PolyExtruderLight : MonoBehaviour
{
    #region Properties

    [Header("Prism Configuration")]
    public string prismName;          // reference to name of the prism
    public Color32 prismColor;        // reference to prism color
    public float polygonArea;         // reference to area of (top) polygon
    public Vector2 polygonCentroid;   // reference to centroid of (top) polygon

    [Header("Material Configuration")]
    public string fallbackShader = "Universal Render Pipeline/Lit";

    // Custom or fallback material reference.
    private Material prismMaterial;

    private static readonly float DEFAULT_BOTTOM_Y = 0.0f;
    private static readonly float DEFAULT_TOP_Y = 1.0f;
    private float extrusionHeightY = 1.0f;

    // Original polygon vertices (2D)
    private Vector2[] originalPolygonVertices;
    public Vector2[] OriginalPolygonVertices { get { return originalPolygonVertices; } }

    // Cached component references
    private Transform prismTransform;
    private MeshFilter prismMeshFilter;
    private MeshRenderer prismMeshRenderer;

    #endregion

    #region BurstJobs

    [BurstCompile]
    public struct CalculateAreaCentroidJob : IJob
    {
        [ReadOnly] public NativeArray<Vector2> vertices;
        // results[0] = doubleArea, results[1] = centroidX, results[2] = centroidY
        public NativeArray<float> results;

        public void Execute()
        {
            float doubleArea = 0f;
            float centroidX = 0f;
            float centroidY = 0f;
            int len = vertices.Length;
            for (int i = 0; i < len; i++)
            {
                Vector2 vCurr = vertices[i];
                Vector2 vNext = vertices[(i + 1) % len];
                float cross = vCurr.x * vNext.y - vNext.x * vCurr.y;
                doubleArea += cross;
                centroidX += (vCurr.x + vNext.x) * cross;
                centroidY += (vCurr.y + vNext.y) * cross;
            }
            results[0] = doubleArea;
            results[1] = centroidX;
            results[2] = centroidY;
        }
    }

    [BurstCompile]
    public struct AreVerticesClockwiseJob : IJob
    {
        [ReadOnly] public NativeArray<Vector2> vertices;
        // result[0] will hold the computed edgesSum.
        public NativeArray<float> result;

        public void Execute()
        {
            float edgesSum = 0f;
            int len = vertices.Length;
            for (int i = 0; i < len; i++)
            {
                Vector2 vCurr = vertices[i];
                Vector2 vNext = vertices[(i + 1) % len];
                edgesSum += (vNext.x - vCurr.x) * (vNext.y + vCurr.y);
            }
            result[0] = edgesSum;
        }
    }

    #endregion

    #region MeshCreator

    /// <summary>
    /// Create a prism based on the input parameters, combining everything into a single mesh.
    /// </summary>
    public void createPrism(string prismName, float height, Vector2[] vertices, Color32 color, Material mat = null)
    {
        // Store data.
        this.prismName = prismName;
        this.extrusionHeightY = height;
        this.originalPolygonVertices = vertices;
        this.prismColor = color;
        this.polygonArea = 0.0f;
        this.polygonCentroid = Vector2.zero;
        this.prismTransform = this.transform;
        this.prismMeshFilter = this.gameObject.AddComponent<MeshFilter>();
        this.prismMeshRenderer = this.gameObject.AddComponent<MeshRenderer>();

        // Store the custom material (may be null)
        this.prismMaterial = mat;

        // Ensure vertices are clockwise
        if (!areVerticesOrderedClockwise(this.originalPolygonVertices))
            System.Array.Reverse(this.originalPolygonVertices);

        // Calculate area and centroid
        if (calculateAreaAndCentroid(this.originalPolygonVertices))
        {
            initPrism();
        }
        else
        {
            Debug.LogWarning("[PolyExtruderLight] createPrism failed. Area is zero for prism: " + this.prismName);
        }
    }

    /// <summary>
    /// Determine whether the input vertices are ordered clockwise using a Burst job.
    /// </summary>
    private bool areVerticesOrderedClockwise(Vector2[] vertices)
    {
        NativeArray<Vector2> nativeVertices = new NativeArray<Vector2>(vertices, Allocator.TempJob);
        NativeArray<float> result = new NativeArray<float>(1, Allocator.TempJob);
        var job = new AreVerticesClockwiseJob
        {
            vertices = nativeVertices,
            result = result
        };
        job.Run();
        float edgesSum = result[0];
        result.Dispose();
        nativeVertices.Dispose();
        return edgesSum >= 0f;
    }

    /// <summary>
    /// Calculate area and centroid of the polygon (2D) using a Burst job.
    /// </summary>
    private bool calculateAreaAndCentroid(Vector2[] vertices)
    {
        NativeArray<Vector2> nativeVertices = new NativeArray<Vector2>(vertices, Allocator.TempJob);
        NativeArray<float> results = new NativeArray<float>(3, Allocator.TempJob); // [0]: doubleArea, [1]: centroidX, [2]: centroidY

        var job = new CalculateAreaCentroidJob
        {
            vertices = nativeVertices,
            results = results
        };
        job.Run();

        float doubleArea = results[0];
        float centroidX = results[1];
        float centroidY = results[2];

        // Compute absolute area.
        float polygonArea = (doubleArea < 0f) ? -0.5f * doubleArea : 0.5f * doubleArea;
        this.polygonArea = polygonArea;

        // The centroid formula: divide by (3 * doubleArea). (The sign cancels out.)
        float sixTimesArea = doubleArea * 3f;
        bool valid = !Mathf.Approximately(polygonArea, 0f);
        if (valid)
        {
            this.polygonCentroid = new Vector2(centroidX / sixTimesArea, centroidY / sixTimesArea);
        }

        results.Dispose();
        nativeVertices.Dispose();
        return valid;
    }

    /// <summary>
    /// Create bottom, top, and surrounding meshes, then combine them into a single mesh.
    /// </summary>
    private void initPrism()
    {
        CombineMesh();

        // Apply a valid material.
        applyFinalMaterial();

        // Adjust final transforms.
        updateHeight(this.extrusionHeightY);
        updateColor(this.prismColor);
        setAnchorPosToCentroid();
    }

    /// <summary>
    /// Rebuild mesh from vertices and indices.
    /// </summary>
    private void redrawMesh(Mesh mesh, List<Vector3> vertices, List<int> indices)
    {
        mesh.Clear();
        mesh.vertices = vertices.ToArray();
        mesh.triangles = indices.ToArray();
        mesh.RecalculateNormals();
        mesh.RecalculateBounds();
    }

    /// <summary>
    /// Assign the final material to the combined mesh.
    /// </summary>
    private void applyFinalMaterial()
    {
        if (this.prismMaterial != null)
        {
            this.prismMeshRenderer.sharedMaterial = this.prismMaterial;
        }
        else
        {
            Material fallbackMat = new Material(Shader.Find(fallbackShader));
            this.prismMeshRenderer.sharedMaterial = fallbackMat;
        }
    }

    #endregion

    #region MeshManipulator

    /// <summary>
    /// Adjusts the prism’s extrusion height by scaling the y-axis.
    /// </summary>
    public void updateHeight(float height)
    {
        if (!Mathf.Approximately(this.extrusionHeightY, height))
        {
            this.extrusionHeightY = height;
        }
        this.prismTransform.localScale = new Vector3(1f, this.extrusionHeightY, 1f);
    }

    /// <summary>
    /// Updates the color of the prism’s material.
    /// </summary>
    public void updateColor(Color32 color)
    {
        if (!this.prismColor.Equals(color))
        {
            this.prismColor = color;
        }
        if (this.prismMeshRenderer != null && this.prismMeshRenderer.material != null)
        {
            this.prismMeshRenderer.sharedMaterial.color = this.prismColor;
        }
    }

    /// <summary>
    /// Repositions the prism so that its centroid is at (x,z) = (centroid.x, centroid.y).
    /// </summary>
    private void setAnchorPosToCentroid()
    {
        this.gameObject.transform.localPosition = new Vector3(
            this.polygonCentroid.x,
            DEFAULT_BOTTOM_Y,
            this.polygonCentroid.y);
    }

    /// <summary>
    /// Update the mesh of the prism.
    /// </summary>
    private void CreateCombinedMesh()
    {
        CombineMesh();

        setAnchorPosToCentroid();
    }

    public void updatePrism(MeshFilter meshFilter, Vector2[] vertices)
    {
        this.originalPolygonVertices = vertices;
        this.prismMeshFilter = meshFilter;

        if (!areVerticesOrderedClockwise(this.originalPolygonVertices))
            System.Array.Reverse(this.originalPolygonVertices);

        if (calculateAreaAndCentroid(this.originalPolygonVertices))
        {
            CreateCombinedMesh();
        }
        else
        {
            Debug.LogWarning("[PolyExtruderLight] updatePrism failed. Area is zero for prism: " + this.prismName);
        }
    }

    #endregion

    /// <summary>
    /// Create and combine small meshes into a single mesh.
    /// </summary>
    private void CombineMesh()
    {
        // Create child objects for bottom, top, and surround.
        GameObject goB = new GameObject();
        goB.transform.parent = this.transform;
        MeshFilter mfB = goB.AddComponent<MeshFilter>();
        Mesh bottomMesh = mfB.mesh;

        GameObject goT = new GameObject();
        goT.transform.parent = this.transform;
        MeshFilter mfT = goT.AddComponent<MeshFilter>();
        Mesh topMesh = mfT.mesh;

        GameObject goS = new GameObject();
        goS.transform.parent = this.transform;
        MeshFilter mfS = goS.AddComponent<MeshFilter>();
        Mesh surroundMesh = mfS.mesh;

        // Triangulate bottom.
        List<Vector2> pointsB = new List<Vector2>();
        for (int i = 0; i < originalPolygonVertices.Length; i++)
            pointsB.Add(originalPolygonVertices[i] - polygonCentroid);

        List<List<Vector2>> holesB = new List<List<Vector2>>();

        Triangulation.triangulate(pointsB, holesB, DEFAULT_BOTTOM_Y,
                                  out List<int> indicesB, out List<Vector3> verticesB);
        redrawMesh(bottomMesh, verticesB, indicesB);

        // Flip bottom polygon so it's visible from outside.
        goB.transform.localScale = new Vector3(-1f, -1f, -1f);
        goB.transform.localRotation = Quaternion.Euler(0f, 180f, 0f);

        // Triangulate top.
        List<Vector2> pointsT = new List<Vector2>();
        for (int i = 0; i < originalPolygonVertices.Length; i++)
            pointsT.Add(originalPolygonVertices[i] - polygonCentroid);

        List<List<Vector2>> holesT = new List<List<Vector2>>();
        Triangulation.triangulate(pointsT, holesT, DEFAULT_TOP_Y,
                                  out List<int> indicesT, out List<Vector3> verticesT);
        redrawMesh(topMesh, verticesT, indicesT);

        // Triangulate surround.
        List<Vector3> verticesS = new List<Vector3>();
        List<int> indicesS = new List<int>();

        // The bottom set.
        foreach (Vector2 vb in pointsB)
            verticesS.Add(new Vector3(vb.x, DEFAULT_BOTTOM_Y, vb.y));

        // The top set.
        foreach (Vector2 vt in pointsT)
            verticesS.Add(new Vector3(vt.x, DEFAULT_TOP_Y, vt.y));

        int countB = pointsB.Count;
        int indexB = 0;
        int indexT = countB;
        int sumQuads = verticesS.Count / 2;
        for (int i = 0; i < sumQuads; i++)
        {
            if (i == (sumQuads - 1))
            {
                // Last quad.
                indicesS.Add(indexB);
                indicesS.Add(0);
                indicesS.Add(indexT);

                indicesS.Add(0);
                indicesS.Add(countB);
                indicesS.Add(indexT);
            }
            else
            {
                // Normal quad.
                indicesS.Add(indexB);
                indicesS.Add(indexB + 1);
                indicesS.Add(indexT);

                indicesS.Add(indexB + 1);
                indicesS.Add(indexT + 1);
                indicesS.Add(indexT);

                indexB++;
                indexT++;
            }
        }
        redrawMesh(surroundMesh, verticesS, indicesS);

        // Combine bottom, top, and surround into a single mesh.
        MeshFilter[] meshFilters = new MeshFilter[] { mfB, mfS, mfT };
        CombineInstance[] combine = new CombineInstance[meshFilters.Length];
        for (int i = 0; i < meshFilters.Length; i++)
        {
            combine[i].mesh = meshFilters[i].sharedMesh;
            combine[i].transform = meshFilters[i].transform.localToWorldMatrix;
        }

        Mesh combinedMesh = new Mesh();
        combinedMesh.CombineMeshes(combine);

        // Assign to main prism.
        this.prismMeshFilter.mesh = combinedMesh;

        // Clean up child objects.
        Destroy(goB);
        Destroy(goS);
        Destroy(goT);
    }
}
