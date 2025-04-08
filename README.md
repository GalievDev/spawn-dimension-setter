# Spawn Dimension Setter

With this mod you can change the world you spawn in. **(Doesn't work with other mods yet)**.

Changes need to be made to the *config.json* file.

**Example:**

```json
{
  "dimension": "minecraft:the_nether",
  "safeCheck": false,
  "isRangeSpawn": false,
  "isExactSpawn": true,
  "rangeSpawn": {
    "rangeX": 0,
    "rangeZ": 0
  },
  "exactSpawn": {
    "x": 6904,
    "y": 64,
    "z": 9668
  }
}
```

**dimension** - the world in which you appear.

**safeCheck** - only needed if range spawn is enabled

**isRangeSpawn** - allows to set spawn point in random range

**isExactSpawn** - allows to set spawn pint in specified position

**rangeX and rangeZ** - the spawn range from 0 coordinates.

**x, y, z** - coordinates of specified point

Added support for dimensions from other mods.

**Proven dimensions:**

- Twilight Forest: ```twilightforest:twilight_forest```

- Paradise Lost: ```paradise_lost:paradise_lost```

- Eden Ring: ```edenring:edenring```

- The Bumblezone: ```the_bumblezone:the_bumblezone```

# Additional Commands

- Set spawn dimension: ```/sws setspawndimension dimension_id x y z```

- Teleport player to another dimension: ```/sws teleport dimension_id x y z```

- Get all worlds in game: ```/sws worlds```

- Get player world: ```/sws playerWorld```