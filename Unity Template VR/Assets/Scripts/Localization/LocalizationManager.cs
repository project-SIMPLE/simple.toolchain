using UnityEngine;
using System.Collections.Generic;
using System.Text;
using System.Text.RegularExpressions;

public class LocalizationManager : MonoBehaviour
{
    public static LocalizationManager Instance { get; private set; }

    private const string CsvFilePath = "Localization/LocalizationData"; 

    private Dictionary<string, Dictionary<string, string>> localizedData;
    [SerializeField] string currentLanguage = "Vietnamese";

    public delegate void LanguageChanged();
    public static event LanguageChanged OnLanguageChanged;

    private List<ColorText> colorTexts = new List<ColorText>();

    private void Awake()
    {
        if (Instance == null)
        {
            Instance = this;
            DontDestroyOnLoad(gameObject);
            LoadLocalizationData();
            SetLanguage(currentLanguage);
        }
        else
        {
            Destroy(gameObject);
        }
    }

    private void LoadLocalizationData()
    {
        localizedData = new Dictionary<string, Dictionary<string, string>>();
        TextAsset csvFile = Resources.Load<TextAsset>(CsvFilePath);

        if (csvFile == null)
        {
            Debug.LogError($"Localization CSV not found at 'Resources/{CsvFilePath}'.");
            return;
        }

        ColorTexts colorTexts = Resources.Load<ColorTexts>("Localization/ColorTexts");
        if (colorTexts != null)
        {
            this.colorTexts = colorTexts.colorTexts;
        }

        // The line splitting logic is safe, as line breaks inside quotes are handled by the parser.
        string[] lines = csvFile.text.Split(new[] { '\r', '\n' }, System.StringSplitOptions.RemoveEmptyEntries);
        if (lines.Length < 2) return;

        // Use the new parser for the header
        List<string> headers = ParseCsvLine(lines[0]);
        for (int i = 1; i < headers.Count; i++)
        {
            localizedData[headers[i].Trim()] = new Dictionary<string, string>();
        }

        for (int i = 1; i < lines.Length; i++)
        {
            // Use the new parser for each data row
            List<string> values = ParseCsvLine(lines[i]);
            string key = values[0].Trim();

            for (int j = 1; j < values.Count && j < headers.Count; j++)
            {
                string language = headers[j].Trim();
                string value = values[j].Trim(); // The parser handles quotes, so we can still trim.
                value = ApplyAutoColors(value);
                localizedData[language][key] = value;
            }
        }
    }

    private List<string> ParseCsvLine(string line)
    {
        var fields = new List<string>();
        var currentField = new StringBuilder();
        bool inQuotes = false;

        for (int i = 0; i < line.Length; i++)
        {
            char c = line[i];

            if (inQuotes)
            {
                // If we are in quotes, check for a closing quote
                if (c == '"')
                {
                    // Check if it's an escaped quote ("")
                    if (i + 1 < line.Length && line[i + 1] == '"')
                    {
                        currentField.Append('"');
                        i++; // Skip the next quote
                    }
                    else
                    {
                        inQuotes = false;
                    }
                }
                else
                {
                    currentField.Append(c);
                }
            }
            else
            {
                // If we are not in quotes
                if (c == '"')
                {
                    inQuotes = true;
                }
                else if (c == ',')
                {
                    fields.Add(currentField.ToString());
                    currentField.Clear();
                }
                else
                {
                    currentField.Append(c);
                }
            }
        }

        fields.Add(currentField.ToString());
        return fields;
    }

    private string ApplyAutoColors(string originalText)
    {
        if (string.IsNullOrEmpty(originalText) || colorTexts.Count == 0) 
            return originalText;

        string modifiedText = originalText;

        foreach (var text in colorTexts)
        {
            if (string.IsNullOrEmpty(text.text)) continue;

            // Convert the Unity Color wheel value into a Hex string (e.g., #FF0000)
            string hexColor = "#" + ColorUtility.ToHtmlStringRGB(text.color);

            // Escape the word in case it has special regex characters like ?, ., or *
            string pattern = Regex.Escape(text.text);

            // $0 is a special regex trick. It means "put the exact text you found right here".
            // This ensures if it finds "Sunday", it doesn't accidentally replace it with lowercase "sunday".
            string replacement = $"<color={hexColor}>$0</color>";

            // Replace the text, ignoring upper/lower case differences
            modifiedText = Regex.Replace(modifiedText, pattern, replacement, RegexOptions.IgnoreCase);
        }

        return modifiedText;
    }

    public void SetLanguage(string languageName)
    {
        if (localizedData.ContainsKey(languageName))
        {
            currentLanguage = languageName;
            OnLanguageChanged?.Invoke();
            Debug.Log($"Language changed to: {currentLanguage}");
        }
        else
        {
            Debug.LogWarning($"Language '{languageName}' not found in localization data.");
        }
    }

    public string GetLanguage()
    {
        return currentLanguage;
    }

    public string GetLocalizedValue(string key)
    {
        if (localizedData.ContainsKey(currentLanguage) && localizedData[currentLanguage].ContainsKey(key))
        {
            return localizedData[currentLanguage][key];
        }

        Debug.LogWarning($"Localization key '{key}' not found for language '{currentLanguage}'.");
        return key;
    }
}