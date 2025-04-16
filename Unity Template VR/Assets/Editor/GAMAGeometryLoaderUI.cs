
using UnityEngine;
using UnityEditor;

public class GAMAGeometryLoaderUI : EditorWindow
{
    public string ip = "localhost";
    public string port = "8080"; 
    public float GamaCRSCoefX = 1.0f;
    public float GamaCRSCoefY = 1.0f;
    public float offsetYBackgroundGeom = 0.0f;
    public float GamaCRSOffsetX = 0.0f;
    public float GamaCRSOffsetY = 0.0f;
    /*  public string GamaPath = " bash /Applications/Gama.app/Contents/headless/gama-headless.sh";
      public string GamaModelPath = "/Users/patricktaillandier/Documents/GitHub/simple.universe.template_/Template GAMA model/Utilities/SentGemetriesToUnity.gaml";
      public string GamaExperiment = "sendGeometriesToUnity_batch";
      public bool runGAMA = false;
    */
    private GAMAGeometryLoader loader;


    void OnGUI()
    {
        GUILayout.Space(10);
        EditorGUILayout.LabelField("Define the connection parameter ", EditorStyles.wordWrappedLabel);
        GUILayout.Space(10);
        ip = EditorGUILayout.TextField("IP: ", ip);
        GUILayout.Space(10);
        port = (EditorGUILayout.TextField("Port: ", port ));
        GUILayout.Space(10);
        GamaCRSCoefX = float.Parse(EditorGUILayout.TextField("X-scaling: ", GamaCRSCoefX + ""));
        GUILayout.Space(10);
        GamaCRSCoefY = float.Parse(EditorGUILayout.TextField("Z-scaling: ", GamaCRSCoefY + ""));
        GUILayout.Space(10);
        GamaCRSOffsetX = float.Parse(EditorGUILayout.TextField("X-Offset: ", GamaCRSOffsetX + ""));
        GUILayout.Space(10);
        GamaCRSOffsetY = float.Parse(EditorGUILayout.TextField("Z-Offset: ", GamaCRSOffsetY + ""));

        GUILayout.Space(10);

        offsetYBackgroundGeom = float.Parse(EditorGUILayout.TextField("Y-Offset: ", offsetYBackgroundGeom + ""));
       /* runGAMA = EditorGUILayout.Toggle("Let Unity run GAMA", runGAMA);
        if (runGAMA)
        {
            GUILayout.Space(10);
            GamaPath = EditorGUILayout.TextField("Path of the headless script of GAMA: ", GamaPath);
            GUILayout.Space(10);
            GamaModelPath = EditorGUILayout.TextField("Path of the GAMA model: ", GamaModelPath);
            GUILayout.Space(10);
            GamaExperiment = EditorGUILayout.TextField("Name of the experiment to run: ", GamaExperiment);

        }
       */
        GUILayout.Space(60);
        EditorGUILayout.BeginHorizontal();
        if (GUILayout.Button("Import"))
        {
            EditorUtility.DisplayDialog("Importation of GAMA Geometries",
                   "Click on Ok, then add the player in the middleware to load the data", "Ok");

            /*if (runGAMA) { 
                
                ProcessStartInfo startInfo = new ProcessStartInfo("bash" + GamaPath);
                startInfo.WindowStyle = ProcessWindowStyle.Normal;
                startInfo.Arguments = "-batch " + GamaExperiment  + " " + GamaModelPath;
                 Process.Start(startInfo);
               

            }*/
             

            loader = new GAMAGeometryLoader(); 
             loader.GenerateGeometries(ip, port, GamaCRSCoefX, GamaCRSCoefY, GamaCRSOffsetX, GamaCRSOffsetY, offsetYBackgroundGeom);


            Close();
        } 
        if (GUILayout.Button("Cancel")) Close();
        EditorGUILayout.EndHorizontal();
    }
} 