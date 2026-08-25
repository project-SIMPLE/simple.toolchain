using System.Collections.Generic;
using UnityEngine;

[CreateAssetMenu(fileName = "ColorTexts", menuName = "Localization/Color Texts")]
public class ColorTexts: ScriptableObject
{
    public List<ColorText> colorTexts = new List<ColorText>();
}

[System.Serializable]
public class ColorText
{
    public string text;
    public Color color = Color.white;
}