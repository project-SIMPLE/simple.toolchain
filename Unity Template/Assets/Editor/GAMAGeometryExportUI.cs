
using UnityEngine;
using UnityEditor;
using UnityEditorInternal;
using System.Diagnostics;

public class GAMAGeometryExportUI : EditorWindow
{
    public string ip = "localhost";
    public string port = "8080";
    public float GamaCRSCoefX = 1.0f;
    public float GamaCRSCoefY = 1.0f;
    public float GamaCRSOffsetX = 0.0f;
    public float GamaCRSOffsetY = 0.0f;

    private GAMAGeometryExport exporter;
   
    private const string _helpText = "Cannot find GameObjects to export!";
    private static Rect _helpRect = new Rect(0f, 0f, 600f, 300f);
    private static Vector2 _windowsMinSize = Vector2.one * 600f;
    private static Rect _listRect = new Rect(Vector2.zero, _windowsMinSize);
    GameObject toExport;
    

     
   
    private void OnGUI()
    {
       
        toExport = (GameObject)EditorGUILayout.ObjectField("Game Objects to export", toExport, typeof(GameObject), true);
        
        GUILayout.Space(10);
        ip = EditorGUILayout.TextField("IP: ", ip);
        GUILayout.Space(10);
        port = EditorGUILayout.TextField("Port: ", port);
        GUILayout.Space(10);
        GamaCRSCoefX = float.Parse(EditorGUILayout.TextField("X-scaling: ", GamaCRSCoefX + ""));
        GUILayout.Space(10);
        GamaCRSCoefY = float.Parse(EditorGUILayout.TextField("Z-scaling: ", GamaCRSCoefY + ""));
        GUILayout.Space(10);
        GamaCRSOffsetX = float.Parse(EditorGUILayout.TextField("X-Offset: ", GamaCRSOffsetX + ""));
        GUILayout.Space(10);
        GamaCRSOffsetY = float.Parse(EditorGUILayout.TextField("Z-Offset: ", GamaCRSOffsetY + ""));

        GUILayout.Space(30);

        EditorGUILayout.BeginHorizontal();
        GUILayout.Space(10);


        if (GUILayout. Button( "Export geometries" ))
        {
            UnityEngine.Debug.Log("Button press");

            EditorUtility.DisplayDialog("Exporting of Geometries",
                  "Waiting for exporting geometries to GAMA", "Ok");

            exporter = new GAMAGeometryExport();
           


            UnityEngine.Debug.Log("GAMAGeometryExport: " + toExport);
            exporter.ManageGeometries(toExport, ip, port, GamaCRSCoefX, GamaCRSCoefY, GamaCRSOffsetX, GamaCRSOffsetY);

            Close();
        }
        GUILayout.Space(30f);
        if (GUILayout. Button("Cancel" )) {
            Close();
        }
        GUILayout. Space(30f) ;
        EditorGUILayout. EndHorizontal();
    }

    private void OnInspectorUpdate()
    {
        Repaint();
    }

    
}