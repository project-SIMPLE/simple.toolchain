using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class AudioManager : MonoBehaviour
{
    public static AudioManager instance;

    [SerializeField] private AudioSource sfxAudioSource;
    [SerializeField] private AudioSource ambfgAudioSource;
    [SerializeField] private AudioSource ambbgAudioSource;
    [Header("Audio Clips")]
    [SerializeField] private AudioClip handPumping;
    [SerializeField] private AudioClip timeOver;

    void Awake()
    {
        if (instance == null)
        {
            instance = this;
            DontDestroyOnLoad(gameObject);
        }
        else
        {
            Destroy(gameObject);
        }
    }

    void Update()
    {
    }

    public void PlaySoundEffect(AudioClip clip)
    {
        sfxAudioSource.PlayOneShot(clip);
    }

    public void PlayFGAmbience(AudioClip clip)
    {
        ambfgAudioSource.PlayOneShot(clip);
    }

    public void PlayBGAmbience(AudioClip clip)
    {
        ambbgAudioSource.clip = clip;
        ambbgAudioSource.Play();
    }

    public void PlayPumpSound()
    {
        sfxAudioSource.PlayOneShot(handPumping);
    }

    public void PlayTimeOverSound()
    {
        sfxAudioSource.PlayOneShot(timeOver);
    }
}
