using System.Collections;
using System.Collections.Generic;
using TMPro;
using UnityEngine;
public class Gun : MonoBehaviour
{
    public int damage = 1;
    public float range = 5f;
    public float fireRate = 15f;
    private float nextTimeToFire = 0f;

    public int maxAmmo = 25;
    private int currentAmmo = -1;
    public float reloadTime = 2f;
    private bool isReloading = false;


    public TextMeshProUGUI ammoText;

    public Camera playerCam;

    public ParticleSystem muzzleFlash;
    public GameObject impactEffect;

    void Start()
    {
        if(currentAmmo == -1)
        {
            currentAmmo = maxAmmo;
        }
    }
    // Update is called once per frame
    void Update()
    {
        if (isReloading)
        {
            return;
        }
        ammoText.text = currentAmmo.ToString();
        if(Input.GetMouseButton(0) && currentAmmo > 0 && Time.time >= nextTimeToFire)
        {
            currentAmmo--;
            nextTimeToFire = Time.time + 1f / fireRate;
            Shoot();
        }
        if(Input.GetKeyDown(KeyCode.R) && currentAmmo != maxAmmo)
        {
            StartCoroutine(Reload());
        }
    }

    IEnumerator Reload()
    {
        isReloading = true;
        Debug.Log("Reloadinng...");
        yield return new WaitForSeconds(reloadTime);
        currentAmmo = maxAmmo;
        isReloading = false;
    }

    void Shoot()
    {
        muzzleFlash.Play();
        RaycastHit hit;
        if (Physics.Raycast(playerCam.transform.position, playerCam.transform.forward, out hit, range))
        {
            Enemy enemyTarget = hit.transform.GetComponent<Enemy>();
            if (enemyTarget != null)
            {
                enemyTarget.TakeDamage(damage);
            }

            GameObject impactEff = Instantiate(impactEffect, hit.point, Quaternion.LookRotation(hit.normal));
            Destroy(impactEff, .5f);
        }


    }
}
