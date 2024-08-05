
using UnityEngine;
using UnityEditor;


public class GAMAMenu : ScriptableObject
{

    public string ip = "192.168.0.186";
    public int port = 8000;


    private const string CustomMenuBasePath = "GAMA/";
	private const string LoadGeometriesPath = CustomMenuBasePath + "Load geometries from GAMA";
    private const string ExportGeometriesPath = CustomMenuBasePath + "Export geometries to GAMA";

    [MenuItem(LoadGeometriesPath)]
	private static void LoadGeometries()
	{
        CreateGeometryImportationWaitingDialog();

    }

    [MenuItem(ExportGeometriesPath)]
    private static void ExportGeometries()
    {
        CreateGeometryExportWaitingDialog();

    }

    static void CreateGeometryExportWaitingDialog()
    {
        GAMAGeometryExportUI window = CreateInstance<GAMAGeometryExportUI>();
        window.position = new Rect(Screen.width / 2, Screen.height / 2, 300, 400);
        window.ShowUtility();
    }

    static void CreateGeometryImportationWaitingDialog()
    {
        GAMAGeometryLoaderUI window = CreateInstance<GAMAGeometryLoaderUI>();
        window.position = new Rect(Screen.width / 2, Screen.height / 2, 300, 400);
        window.ShowUtility();
    }
}