# Intricate's Carpet Addons: "intricarpet"
## Supported Versions
For now, only `1.16.5`.
## Features
### Logging
`/log explosions compact`: Groups explosions by position, reduces spam in big cannons and makes it easier to find different positions in a sea of explosions.
### Scarpet
`on_move` entity event: Gets called every time an entity changes position, at the `TAIL` of the `setPos` method. Required arguments: `entity, motion, pos1, pos2`.
