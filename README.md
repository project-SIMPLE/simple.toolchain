# SIMPLE Toolchain

This repository contains the two components needed to author and run VR simulations with [SIMPLE](https://doc.project-simple.eu):

| Component | Description |
|---|---|
| [`GAMA Plugin/`](./GAMA%20Plugin/) | Eclipse plugin that extends [GAMA](https://gama-platform.org) with GAML species, operators, and an experiment type for connecting simulations to Unity |
| [`Unity Template VR/`](./Unity%20Template%20VR/) | Unity 6 project template that students open to join a SIMPLE session on a Meta Quest headset |

**Full documentation → [doc.project-simple.eu](https://doc.project-simple.eu)**

---

## Requirements

### GAMA Plugin

- **GAMA 2025-06** or later
- **JDK 21** and **Maven 3.x** (to build from source)

Install via GAMA's **Help > Install New Software**, using the update site:
`https://project-simple.github.io/simple.toolchain/`

### Unity Template VR

- **Unity Editor 6000.3.8f1** — exact version required
- Modules: **Android Build Support** (with OpenJDK + Android SDK & NDK Tools)

> **Windows:** verify that `Assets/Plugins/websocket-sharp.dll` is present before building.

---

## Documentation

| Topic | Link |
|---|---|
| GAMA Plugin installation | [doc.project-simple.eu/gama/installation](https://doc.project-simple.eu/gama/installation) |
| GAML API reference | [doc.project-simple.eu/gama/api](https://doc.project-simple.eu/gama/api) |
| Unity template installation | [doc.project-simple.eu/unity/installation](https://doc.project-simple.eu/unity/installation) |
| Unity template reference | [doc.project-simple.eu/unity/template-reference](https://doc.project-simple.eu/unity/template-reference) |
| How-to guides | [doc.project-simple.eu/unity/how-to](https://doc.project-simple.eu/unity/how-to) |
| Step-by-step tutorial | [doc.project-simple.eu/tutorials](https://doc.project-simple.eu/tutorials) |
| Building from source | [doc.project-simple.eu/advanced/building-from-source](https://doc.project-simple.eu/advanced/building-from-source) |
