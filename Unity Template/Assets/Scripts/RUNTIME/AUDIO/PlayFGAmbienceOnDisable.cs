using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class PlayFGAmbienceOnDisable : MonoBehaviour
{
    [SerializeField] private AudioClip audioClip;

    void OnDisable()
    {
        if (AudioManager.instance)
            AudioManager.instance.PlayFGAmbience(audioClip);
    }
}
