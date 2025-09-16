using UnityEngine;
using TMPro;

[RequireComponent(typeof(TextMeshProUGUI))] 
public class LocalizedText : MonoBehaviour
{
    public string localizationKey;
    private TextMeshProUGUI textComponent; 

    private void Start()
    {
        textComponent = GetComponent<TextMeshProUGUI>();
        UpdateText();
        LocalizationManager.OnLanguageChanged += UpdateText;
    }

    private void OnDestroy()
    {
        LocalizationManager.OnLanguageChanged -= UpdateText;
    }

    private void UpdateText()
    {
        if (textComponent != null)
        {
            textComponent.text = LocalizationManager.Instance.GetLocalizedValue(localizationKey);
        }
    }
}