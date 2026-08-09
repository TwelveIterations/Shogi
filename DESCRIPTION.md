This is a library mod that adds a rule-based effect system for use in Minecraft mods. Its most notable use is [Waystones](https://www.curseforge.com/minecraft/mc-mods/waystones), where it controls the highly dynamic warp requirements, and [Unbreakables](https://www.curseforge.com/minecraft/mc-mods/unbreakables), where it controls the rules of which blocks are unbreakable.

To achieve this, mods implement the Shogi API and expose properties that can then be configured using a Shogi expression, which is a simple text-based format. For information on how to write Shogi rules or how to implement Shogi for your own mod, see the [documentation](https://shogi.twelveiterations.com/guides).

### ![Features](https://mods.twelveiterations.com/img/features-header.png)

- Dynamic rule evaluation based on player, dimension, or other context
- Simple expression syntax to configure rules as well as JSON support
- Conditional logic and aggregates for advanced calculations
- Powerful configuration expansion for mod developers with many inbuilt effects
- Embeddable lightweight API for mod developers to easily add support
