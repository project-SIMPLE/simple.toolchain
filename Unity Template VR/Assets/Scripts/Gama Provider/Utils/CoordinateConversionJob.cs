using Unity.Burst;
using Unity.Collections;
using Unity.Jobs;
using UnityEngine;

[BurstCompile]
public struct CoordinateConversionJob : IJobParallelFor
{
    [ReadOnly] public NativeArray<int> points;
    public NativeArray<Vector2> results;

    public float coefX;
    public float coefY;
    public float offsetX;
    public float offsetY;
    public int precision;

    public void Execute(int index)
    {
        int i = index * 2;
        int x = points[i];
        int y = points[i + 1];
        results[index] = new Vector2((coefX * x) / precision + offsetX,
            (coefY * y) / precision + offsetY);
    }
}