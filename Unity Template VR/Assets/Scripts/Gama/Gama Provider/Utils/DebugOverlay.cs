using System.Collections;
using System.Collections.Generic;
using System.Text;
using UnityEngine;

public class DebugManager : MonoBehaviour
{
    public TMPro.TextMeshProUGUI debugOverlay;
    public int maxLines = 20;
    private Queue<string> queue = new Queue<string>();
    private string currentText = "";

    void OnEnable()
    {
        Application.logMessageReceivedThreaded += HandleLog;
    }

    void OnDisable()
    {
        Application.logMessageReceivedThreaded -= HandleLog;
    }


    void HandleLog(string logString, string stackTrace, LogType type)
    {
        // Delete oldest message
        if (queue.Count >= maxLines) queue.Dequeue();

        queue.Enqueue(logString);

        var builder = new StringBuilder();
        builder.Append("Debug Logs\n\n");
        foreach (string st in queue)
        {
            builder.Append(st).Append("\n");
        }

        currentText = builder.ToString();

        debugOverlay.text = currentText;
    }
}
