### Localization System for SIMPLE Unity template

1. Create/update language data: Edit the LocalizationData.csv in the Resources/Localization using the correct format. (DO NOT USE "," "/n" "/r" FOR VALUE)
2. Make text switch language based on the current setting: Require TextMeshPro. Make sure LocalizationManager (DontDestroyOnLoad) is in your first scene, add the component LocalizedText to that text gameobject and add the key for that text.
3. Change current language: call LocalizationManager.Instance.SetLanguage("language_name");. Make sure all language names and key names are the same as their corresponding ones in the csv file.