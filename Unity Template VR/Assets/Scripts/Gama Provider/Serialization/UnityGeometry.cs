using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.Rendering;
[System.Serializable]
public class UnityGeometry 
{
  
    public List<UnityPoint> points;
    public List<int> heights;
    public List<string> names;
     
    public UnityGeometry(GameObject obj, CoordinateConverter converter)
    {
         
        points = new List<UnityPoint>();
        heights = new List<int>();
        names = new List<string>();
        addObject(obj, converter);
    }

   private void addObject(GameObject obj, CoordinateConverter converter)
    {
        MeshFilter mf = null;
        float yV = obj.transform.localScale.y;
        Vector3 v = obj.transform.localScale;
        v.y = 0.0f;
        obj.transform.localScale = v;
        Mesh mesh = obj.GetComponent<Mesh>();
       
         if (mesh == null)
         {
             mf = obj.GetComponent<MeshFilter>();
             if (mf != null)
             {
                 mesh = mf.sharedMesh;
             }

         }
        
        if (mesh != null)
        {
            

           
            for (int index = 0; index < mesh.subMeshCount; index++)
            {
                for (int i = 0; i < mesh.GetTriangles(index).Length; i++)
                {
                    Debug.Log("Triangles: " + i);
                    names.Add(obj.name);
                    heights.Add((int)mesh.bounds.size.y);

                    Vector3 wv = mesh.vertices[mesh.GetTriangles(index)[i]];
                    if (mf != null)
                        wv = mf.transform.TransformPoint(wv);


                    UnityPoint pt = new UnityPoint(wv, converter);
                    points.Add(pt);

                }
            }
            
          
            points.Add(new UnityPoint());
        }
        v.y = yV;
        obj.transform.localScale = v;
        for (int i = 0; i < obj.transform.childCount; i++)
        {
            addObject(obj.transform.GetChild(i).gameObject, converter);
            
        }
       

    }


    public string ToJSON()
    {
        return JsonUtility.ToJson(this);
    }
}

[System.Serializable]
public class UnityPoint
{
    public List<int> c;

    public UnityPoint()
    {
        c = new List<int>();
    }
    public UnityPoint(Vector3 vect, CoordinateConverter converter)
    {
       c = converter.toGAMACRS(vect);
    }   
}
