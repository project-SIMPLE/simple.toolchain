using UnityEngine;
using UnityEngine.SceneManagement;
using System.Collections;
using UnityEngine.Rendering.Universal;

public class SceneTransition : MonoBehaviour
{
    public static SceneTransition Instance { get; private set; }

    [SerializeField] Canvas canvas;
    [SerializeField] CanvasGroup canvasGroup;
    [SerializeField] float fadeInDuration = 1.0f;
    [SerializeField] float fadeOutDuration = 2.0f;
    [SerializeField] bool fadeInOnStart = true;

    void Awake()
    {
        Instance = this;

        if (canvas.worldCamera == null)
        {
            Camera[] cameras = Camera.allCameras;
            for (int i = 0; i < cameras.Length; i++)
            {
                if (cameras[i].GetUniversalAdditionalCameraData().renderType == CameraRenderType.Overlay)
                {
                    canvas.worldCamera = cameras[i];
                    break;
                }
            }
        }
    }

    void Start()
    {
        if (fadeInOnStart)
        {
            canvasGroup.alpha = 1f;
            StartCoroutine(Fade(0f, fadeOutDuration));
        }
        else
        {
            canvasGroup.alpha = 0f;
        }
    }

    public void SwitchScene(string sceneName)
    {
        StartCoroutine(TransitionRoutine(sceneName));
    }

    IEnumerator TransitionRoutine(string sceneName)
    {
        yield return StartCoroutine(Fade(1f, fadeInDuration));

        yield return SceneManager.LoadSceneAsync(sceneName);
    }

    IEnumerator Fade(float targetAlpha, float fadeDuration)
    {
        float startAlpha = canvasGroup.alpha;
        float time = 0;

        while (time < fadeDuration)
        {
            time += Time.deltaTime;
            canvasGroup.alpha = Mathf.Lerp(startAlpha, targetAlpha, time / fadeDuration);
            yield return null;
        }
        canvasGroup.alpha = targetAlpha;
    }
}