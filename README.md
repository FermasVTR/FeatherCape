# FeatherCape — Fabric 1.21.11

A prototype Fabric mod for **server-synchronised custom animated capes**.

## What it does
- Press **K** in-game to choose a `.gif`.
- GIF must be **64x32** (the vanilla cape texture size).
- Maximum upload size: **512 KB**.
- The client sends the GIF to the server.
- The server stores it under `world/feathercape/<player>.gif` and broadcasts it to other modded clients.
- Other players who have this mod installed can see the animated cape.
- The same mod works on both the client and dedicated server.

## Important limitation
A normal vanilla Minecraft client cannot magically see a custom cape from a Fabric mod. For the “visible to everyone” requirement, **every player who should see the custom cape needs this mod installed**. The dedicated server also needs it for upload/synchronisation.

## Current prototype notes
This source is intentionally a small prototype. The server-side persistence/broadcast class and the mixin accessor still need final mapping adjustments for the exact 1.21.11 Yarn/Fabric API combination before building a release JAR. The rendering/networking APIs changed substantially around 1.21.11, so do not treat this ZIP as a ready-to-install JAR.

Fabric 1.21.11 is an obfuscated release and Fabric recommends using current 1.21.11 mappings/tooling. See the official docs:
- https://docs.fabricmc.net/
- https://fabricmc.net/2025/12/05/12111.html
