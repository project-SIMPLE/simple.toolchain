using UnityEngine;
using TMPro;

public class LocalizedKey : MonoBehaviour
{
    public string localizationKey;
    public AudioSource audioSource;
    public TextMeshProUGUI textComponent;

    private void Start()
    {
        if (textComponent == null) GetComponent<TextMeshProUGUI>();

        UpdateText();
        LocalizationManager.OnLanguageChanged += UpdateText;

        if (audioSource != null)
        {
            UpdateAudioClip();
            LocalizationManager.OnLanguageChanged += UpdateAudioClip;
        }
    }

    private void OnDestroy()
    {
        LocalizationManager.OnLanguageChanged -= UpdateText;
        if (audioSource != null)
        {
            LocalizationManager.OnLanguageChanged -= UpdateAudioClip;
        }
    }

    public void UpdateText()
    {
        if (string.IsNullOrEmpty(localizationKey)) return;

        if (textComponent != null)
        {
            string localizedText = LocalizationManager.Instance.GetLocalizedValue(localizationKey);
            if (!string.IsNullOrEmpty(localizedText))
            {
                textComponent.text = localizedText;
            }
        }
        else
        {
            Debug.LogWarning("TextMeshProUGUI component not found on " + gameObject.name);
        }
    }

    public void UpdateAudioClip()
    {
        if (string.IsNullOrEmpty(localizationKey)) return;

        string currentLanguage = LocalizationManager.Instance.GetLanguage();
        string audioClipPath = $"Localization/Audio/{currentLanguage}/{localizationKey}";

        AudioClip loadedClip = Resources.Load<AudioClip>(audioClipPath);

        if (loadedClip != null)
        {
            audioSource.clip = loadedClip;
        }
        else
        {
            Debug.LogWarning($"AudioClip not found at path: '{audioClipPath}'");
        }
    }
}