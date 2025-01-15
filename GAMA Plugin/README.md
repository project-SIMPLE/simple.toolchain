# Overview
The gaml.extension.unity GAMA plugin  provides a number of tools and GAML extensions (built-in species and new experiment type) to transform a GAMA model into a Unity 3D universe.
It works with the [simple UNITY template](https://github.com/project-SIMPLE/simple.toolchain/tree/2024-06/Unity%20Template) and the [simple server middleware](https://github.com/project-SIMPLE/GamaServerMiddleware).

The plugin integrates:
* 2 new abstract agent species for GAMA:
    * An abstract species, abstract_unity_linker, which links a GAMA simulation to a Unity game.
    * An abstract species, abstract_unity_player, which represents a Unity player in a GAMA simulation.
* A new type of experiment, unity, which creates a unity_linker at initialization.
* 3 new types of variables:
    * unity_property: a type representing a set of properties for the geometry/agent to send to Unity, in particular a unity_aspect, and a unity_interaction
    * unity_aspect: a type representing the way a geometry will be displayed in Unity"
    * unity_interaction: a type representing a set of properties concerning the interaction for the geometry/agent to send to Unity

In addition, the plugin integrates a tool accessible from the UnityVR menu that allows you to generate a new VR model from a GAMA model, extending it and linking it to Unity.

Finally, the plugin includes several templates illustrating how to use the plugin (Single player and Multi-player Demos) or how to send or receive geometries from GAMA to Unity or from Unity to GAMA.

A description of the use of the plugin with a tutorial can be found [here](https://github.com/project-SIMPLE/simple.toolchain/wiki/02-Tutorial-%E2%80%90-From-GAMA-model-to-Virtual-Universe-%E2%80%90-case-of-a-traffic-model).

# Installation

## From GAMA
To use the plugin from a release version of GAMA (version 2024-03), it can be installed directly from the plugin installer.
Specifically, select "Support/Install new plugins..." from the menu
<img width="1065" alt="InstallPlugin" src="https://github.com/user-attachments/assets/e2df2f5d-a579-46df-8622-708be654a121" />

then "Work with", copy the address [https://project-simple.github.io/simple.toolchain/](https://project-simple.github.io/simple.toolchain/) and select the plugin afterwards.
<img width="1796" alt="Plugin" src="https://github.com/user-attachments/assets/923f923f-93e1-41de-8e6e-a0d7abeae414" />

## Developer mode
If you have a developer version of GAMA (branch 2024/06), you can clone the project directly to add the plugin to GAMA.
