# Intricate's Carpet Addons: "intricarpet"
## Supported Versions
For now, only `1.16.5`.
## Features
### Rules
`optimizedTNTExtra`: EVEN MORE optimized tnt.
`disableTNTChainReaction`: Makes TNT only get broken but not ignited when blown up.
`slimeChunks` Changes Slime Chunk behavior. Options: `all`, `none`, `vanilla`.
### Logging
`/log explosions compact`: Groups explosions by position, reduces spam in big cannons and makes it easier to find different positions in a sea of explosions.
### Scarpet
`on_move` entity event: Gets called every time an entity changes position, at the `TAIL` of the `setPos` method. Required arguments: `entity, motion, pos1, pos2`.
