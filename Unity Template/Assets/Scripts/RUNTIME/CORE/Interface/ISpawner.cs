using System.Collections;
using System.Collections.Generic;
using UnityEngine;


public interface ISpawner
{
    string SpawnName {get;}
    float SpawnRate {get;}

    public void Spawn();
}