using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class PlayFGAmbienceOnEnable : MonoBehaviour
{
    [SerializeField] private AudioClip audioClip;

    void OnEnable()
    {
        if (AudioManager.instance)
            AudioManager.instance.PlayFGAmbience(audioClip);
    }
}
