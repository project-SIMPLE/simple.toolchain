/*
 * Triangulation.cs
 *
 * Description: Class to triangulate (create render triangles for) a custom polygon mesh.
 * The triangulation process now uses BurstTriangulator instead of Triangle.NET.
 *
 * Supported Unity version: 2021.3.16f1 Personal (tested)
 *
 * Author: Nico Reski (adapted)
 * Web: https://reski.nicoversity.com
 * Twitter: @nicoversity
 * GitHub: https://github.com/nicoversity
 *
 * Note: Make sure the BurstTriangulator package (and its dependencies, such as Unity.Collections,
 * Unity.Mathematics, and Unity.Jobs) is imported into your project.
 */

using UnityEngine;
using System.Collections.Generic;
using Unity.Collections;
using Unity.Mathematics;
using andywiecko.BurstTriangulator;  // Ensure this namespace is available

public class Triangulation
{
    /// <summary>
    /// Triangulates a custom polygon with optional holes using BurstTriangulator.
    /// </summary>
    /// <param name="points">List of Vector2 points for the outer boundary (in order).</param>
    /// <param name="holes">List of lists of Vector2 points for holes (each in order). Can be null.</param>
    /// <param name="vertexY">Y-coordinate value for the resulting 3D vertices.</param>
    /// <param name="outIndices">Output list of triangle indices.</param>
    /// <param name="outVertices">Output list of 3D vertices.</param>
    /// <returns>True if triangulation was completed successfully.</returns>
    public static bool triangulate(
        List<Vector2> points,
        List<List<Vector2>> holes,
        float vertexY,
        out List<int> outIndices,
        out List<Vector3> outVertices
    )
    {
        // ---------------------------------------------------------------------
        // 1) REMOVE DUPLICATES OR NEAR-DUPLICATES
        // ---------------------------------------------------------------------
        // This step is crucial to avoid zero-length or collinear edges.
        RemoveDuplicatePoints(points, 1e-6f); // Adjust epsilon if needed
        if (holes != null)
        {
            foreach (var hole in holes)
            {
                RemoveDuplicatePoints(hole, 1e-6f);
            }
        }

        // ---------------------------------------------------------------------
        // 2) PREPARE DATA
        // ---------------------------------------------------------------------
        // Combined list for all vertices (outer boundary first, then holes)
        var allVertices = new List<double2>();
        // List for constraint edges; each pair of indices represents a segment.
        var constraints = new List<int>();
        // For each hole, one seed point (typically the centroid) is required.
        var holeSeeds = new List<double2>();

        // --- Process Outer Boundary ---
        int outerCount = points.Count;
        for (int i = 0; i < outerCount; i++)
        {
            Vector2 p = points[i];
            allVertices.Add(new double2(p.x, p.y));
        }

        // Add constraint edges to form a closed loop for the outer boundary.
        for (int i = 0; i < outerCount; i++)
        {
            int next = (i + 1) % outerCount;
            constraints.Add(i);
            constraints.Add(next);
        }

        // --- Process Holes ---
        if (holes != null)
        {
            foreach (List<Vector2> hole in holes)
            {
                int startIndex = allVertices.Count;
                int holeCount = hole.Count;
                double2 sum = new double2(0, 0);

                // Add hole vertices.
                for (int j = 0; j < holeCount; j++)
                {
                    Vector2 p = hole[j];
                    double2 dp = new double2(p.x, p.y);
                    allVertices.Add(dp);
                    sum += dp;
                }

                // Add constraint edges for the hole boundary.
                for (int j = 0; j < holeCount; j++)
                {
                    int current = startIndex + j;
                    int next = startIndex + ((j + 1) % holeCount);
                    constraints.Add(current);
                    constraints.Add(next);
                }

                // Compute the centroid of the hole (used as a hole seed).
                double2 centroid = sum / holeCount;
                holeSeeds.Add(centroid);
            }
        }

        // ---------------------------------------------------------------------
        // 3) CREATE NATIVEARRAYS FOR BURSTTRIANGULATOR
        // ---------------------------------------------------------------------
        NativeArray<double2> nativePositions = new NativeArray<double2>(allVertices.ToArray(), Allocator.TempJob);
        NativeArray<int> nativeConstraints = new NativeArray<int>(constraints.ToArray(), Allocator.TempJob);
        NativeArray<double2> nativeHoleSeeds = new NativeArray<double2>(holeSeeds.ToArray(), Allocator.TempJob);

        // ---------------------------------------------------------------------
        // 4) SETUP AND RUN THE TRIANGULATION
        // ---------------------------------------------------------------------
        using (var triangulator = new Triangulator(Allocator.TempJob))
        {
            triangulator.Input = new InputData<double2>()
            {
                Positions = nativePositions,
                ConstraintEdges = nativeConstraints,
                HoleSeeds = nativeHoleSeeds
            };

            // Adjust Triangulator settings
            triangulator.Settings.AutoHolesAndBoundary = false; // We'll define holes manually
            triangulator.Settings.RestoreBoundary = true;       // Keep all constraints in final mesh
            triangulator.Settings.ValidateInput = true;         // Validate constraints, positions, etc.
            triangulator.Settings.Verbose = true;               // Print warnings/errors in the console

            // Execute triangulation synchronously.
            triangulator.Run();

            // --- Retrieve and convert output ---
            var outputPositions = triangulator.Output.Positions;
            var outputTriangles = triangulator.Output.Triangles;

            // Convert positions to Vector3 using the provided vertexY value.
            outVertices = new List<Vector3>(outputPositions.Length);
            for (int i = 0; i < outputPositions.Length; i++)
            {
                double2 pos = outputPositions[i];
                outVertices.Add(new Vector3((float)pos.x, vertexY, (float)pos.y));
            }

            // Copy the triangle indices.
            outIndices = new List<int>(outputTriangles.Length);
            for (int i = 0; i < outputTriangles.Length; i++)
            {
                outIndices.Add(outputTriangles[i]);
            }
        }

        // Dispose native arrays.
        nativePositions.Dispose();
        nativeConstraints.Dispose();
        nativeHoleSeeds.Dispose();

        return true;
    }

    /// <summary>
    /// Removes consecutive duplicates or near-duplicates from a list of points,
    /// ensuring no zero-length edges. Also removes the final point if it matches the first.
    /// </summary>
    /// <param name="list">The polygon or hole points in order.</param>
    /// <param name="epsilon">Distance tolerance to consider points "equal".</param>
    private static void RemoveDuplicatePoints(List<Vector2> list, float epsilon)
    {
        if (list.Count <= 1) return;

        // Remove consecutive duplicates
        for (int i = list.Count - 1; i > 0; i--)
        {
            if (Vector2.Distance(list[i], list[i - 1]) < epsilon)
            {
                list.RemoveAt(i);
            }
        }
        // If the shape is closed (first == last), remove the last if it's effectively the same as the first
        if (list.Count > 1 && Vector2.Distance(list[0], list[list.Count - 1]) < epsilon)
        {
            list.RemoveAt(list.Count - 1);
        }
    }
}
