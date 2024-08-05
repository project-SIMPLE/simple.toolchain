using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class PlaySoundEffectOnEnable : MonoBehaviour
{
    [SerializeField] private AudioClip audioClip;

    void OnEnable()
    {
        if (AudioManager.instance)
            AudioManager.instance.PlaySoundEffect(audioClip);
    }
}