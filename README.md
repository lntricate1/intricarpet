# Intricate's Carpet Addons: "intricarpet"
## Supported Versions
`1.16.5` and `1.17.1`
## Features
### Rules
`optimizedTNTExtra`: EVEN MORE optimized tnt.

`disableTNTChainReaction`: Makes TNT only get broken but not ignited when blown up.

`slimeChunks` Changes Slime Chunk behavior. Options: `all`, `none`, `vanilla`.
### Interaction Command
Syntax: `interaction [interaction_name] [true/false]`

`interaction`: list interactions.

**Interactions**

- `all`: Selects all interactions.
- `blocks`: Activating pressure plates and tripwire; activating redstone ore; trampling farmland, breaking turtle eggs.
- `entities`: Collisions, both with normal entities and projectiles; picking up items and xp; attracting xp.
- `chunkloading`: Disables all chunkloading, allows you to work in lazy chunks while in any game mode.
- `updates`: Per-player interactionUpdates. Disables block updates caused by player actions.

### Logging
`/log explosions compact`: Groups explosions by position, reduces spam in big cannons and makes it easier to find different positions in a sea of explosions.
### Scarpet
`on_move` entity event: Gets called every time an entity changes position, at the `TAIL` of the `setPos` method. Required arguments: `entity, motion, pos1, pos2`.
