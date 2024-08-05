using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using TMPro;
using UnityEngine.UI;

public class HealthUI : MonoBehaviour
{
    public TextMeshProUGUI hp;
    IDamageable damageable;
    
    // Start is called before the first frame update
    void Start()
    {
       damageable = GetComponent<IDamageable>();
    }

    // Update is called once per frame
    void Update()
    {
        int health = damageable.Health;
        if (health <= 0) hp.text = "0";
        else hp.text = health.ToString();
    }
}
