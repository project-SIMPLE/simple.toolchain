using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System;

public class TutorialManager : MonoBehaviour
{
    [SerializeField] private List<TutorialEvent> events;

    void Update()
    {
        for (var i = 0; i < events.Count; i++)
        {
            if (events[i].done) continue;
            switch (events[i].triggerType)
            {
                case TriggerType.Enabled:
                    if (events[i].triggerObject.activeSelf == true)
                    {
                        if (events[i].sequentialEvent && events[i-1].done == false) continue;
                        events[i].UI.SetActive(true);
                        events[i].done = true;
                    }
                    break;
                case TriggerType.Disabled:
                    if (events[i].triggerObject.activeSelf == false)
                    {
                        if (events[i].sequentialEvent && events[i-1].done == false) continue;
                        events[i].UI.SetActive(true);
                        events[i].done = true;
                    }
                    break;

            }
        }
    }

    [Serializable]
    class TutorialEvent {
        public GameObject triggerObject;
        public TriggerType triggerType;
        [Tooltip("if true, previous event must be done for this event to be checked")] public bool sequentialEvent;
        public GameObject UI;
        [HideInInspector] public bool done;
    }

    enum TriggerType {
        Enabled,
        Disabled
    }
}
