# Shogi

Minecraft Mod. Rule-based, contextually dynamic configuration options for Fabric, NeoForge and Forge.

Shogi is a mod providing power users and modpack developers with an expression and rule layer
usable on supported configuration options. Through various conditions and resulting effects, this allows for
customizability that extends beyond the limited set of inbuilt behaviors and configurations.

Mods must implement the Shogi API in order to be customizable with Shogi. To allow for support without requiring a
hard dependency, the API is shipped as a separate embeddable jar. By defining your options as `ShogiValue` and resolving
them with a context, users who install Shogi will be able to override its behavior by specifying rules inside their
`config/shogi.json` file.

The separately downloaded Shogi mod is only a required dependency if your mod uses advanced features
(like `.networked()` values) or relies on Shogi rules as part of its main configuration
(like Waystones does for its `warpRequirements` config option). For all other cases, the API comes with a minimal
implementation that will simply fall back to your original value providers if the user does not have Shogi installed.

## Motivation

I created Shogi because time and time again I encounter config creep where players ask for more and more options to
cover every possible scenario: be it for teleportation costs in Waystones, fall-through rules in Forgiving Void, or
things that should happen after a player is revived in Hardcore Revival.
By wrapping these options in Shogi, I can keep a simple config file that can still optionally be expanded with powerful
rules. With Shogi, there's then only a single place that needs to be expanded if the current set of conditions and
effects don't cover a use case yet - the individual mods no longer need to worry about it.

## How to use as a Mod Developer

1\. Start by changing your Gradle files to have `shogi-api` be embedded in your mod's jar.

Add the following to your `build.gradle`:

```groovy
repositories {
    maven {
        url "https://maven.twelveiterations.com/repository/maven-public/"

        content {
            includeGroup "net.blay09.mods"
        }
    }
}
```

When defining the dependency below, replace the version with the version you want to depend on.
Shogi follows a versioning scheme where the major and minor version always match the minor and patch version of
Minecraft.
So for Minecraft 26.1, you would depend on 26.1.0.x where x is the patch version of Shogi itself.

Specifically on jarJar dependencies, you should also use a version range to ensure your mod will continue to function
even if another mod ships a later patch version of Shogi.

You can find the latest version for a given Minecraft version
at https://maven.twelveiterations.com/service/rest/repository/browse/maven-public/net/blay09/mods/shogi-common/

In your `gradle.properties`:

```ini
shogi_version = 26.1.0.0
shogi_version_range = [26.1,26.2)
```

For Common:

```groovy
dependencies {
    compileOnly "net.blay09.mods:shogi-api:$shogi_version"
}
```

For NeoForge:

```groovy
jarJar.enable() // Enable the Jar-in-Jar system

dependencies {
    jarJar("net.blay09.mods:shogi-api") {
        version {
            strictly shogi_version_range
            prefer shogi_version
        }
    }
}
```

For Fabric:

```groovy
dependencies {
    include api("net.blay09.mods:shogi-api:$shogi_version")
}
```

For Forge (ForgeGradle 6):

```groovy
jarJar.enable() // Enable the Jar-in-Jar system. Make sure to put this line *before* the minecraft block!

dependencies {
    jarJar(group: "net.blay09.mods", name: "shogi-api", version: shogi_version_range) {
        jarJar.pin(it, shogi_version)
    }
}
```

For Forge (ForgeGradle 7):

```groovy
plugins {
    id "net.minecraftforge.jarjar" version "0.2.3"
}

jarJar.register()

dependencies {
    jarJar("net.blay09.mods:shogi-api:${shogi_version}")
}
```

2\. Create some Shogi wrappers for the configuration options you want to make dynamic.

```java
public class ExampleModRules {
    // This value takes a player as a context. By default, its result falls back to the static config value.
    // Users could configure a Shogi rule to conditionally prevent void deaths based on the player's xp level, dimension, etc.
    public static final ShogiValue<Player, Boolean> PREVENT_DEATH = Shogi.booleanValue(Identifier.fromNamespaceAndPath("forgivingvoid", "prevent_death"), player -> ForgivingVoidConfig.getActive().preventDeath);

    // This value takes a level as a context. By default, its result falls back to the static config value.
    // Users could configure a Shogi rule to change the falling height based on the level's dimension, time of day, etc.
    public static final ShogiValue<Level, Integer> FALLING_HEIGHT = Shogi.intValue(Identifier.fromNamespaceAndPath("forgivingvoid", "falling_height"), level -> ForgivingVoidConfig.getActive().fallingHeight);

    // There is also a ShogiContext class you can use if you want to provide exact context values.
    // In this case, we define a CAN_REVIVE_OTHERS as an optional way for Shogi users to disable reviving for players under custom conditions. 
    public static final ShogiValue<ShogiContext, Boolean> CAN_REVIVE_OTHERS = Shogi.booleanValue(Identifier.fromNamespaceAndPath("hardcorerevival", "can_revive_others"), context -> true);
}
```

## Rules

Rules are the heart of Shogi. They define what conditions lead to which results. They can range from simple conditional
expressions (like `is_dimension('the_nether') -> failure('This feature is disabled in the Nether')`), to multistep
aggregates and calculations.

These rules can be defined inside `config/shogi.json`, keyed by the value they should target.

```json
{
  "hardcorerevival:can_revive_others": "is_mob_nearby(10) -> failure('You cannot revive someone while monsters are around.')"
}
```

Shogi supports both this custom expression format and JSON directly. The expression format can be a more readable
option,
and is designed to be embeddable within standard toml config files.

The above example in JSON would look as follows:

```json
{
  "hardcorerevival:can_revive_others": {
    "type": "shogi:if",
    "condition": {
      "type": "shogi:is_mob_nearby",
      "distance": 10
    },
    "then": {
      "type": "shogi:failure",
      "message": "You cannot revive someone while monsters are around."
    }
  }
}
```

## Technical Implementation

A `ShogiEffect` is a `Function<ShogiContext, Either<?, ?>>` that takes a context and returns an `Either` of a success or
failure type. Effects are registered on a `ShogiScope` via `MapCodec`s, making them fully serializable to and from JSON.

A parser translates text-based Shogi rules to JSON, allowing for rules to be defined in a slightly more readable format
without the limitations of relying solely on a custom format as rules can use JSON interchangeably.

When multiple rules are defined (as in an array, or an explicit `shogi:aggregate` effect), values are aggregated. Within
an aggregate, variables can be assigned and operated on, allowing for conditional calculations.
They can also define costs (like item or xp costs), which will only be consumed if all aggregate members succeeded.
They return a list of successes or a list of failures, which can either be handled individually,
or in the case of simple values be coerced to the last value (i.e. to get the result of a multistep computation).

```json
[
  "$xp_cost = $distance * 0.01",
  "is_target_global_waystone -> $xp_cost * 0.5",
  "xp_points_cost($xp_cost)"
]
```

## Fatal Failures

`ShogiFatalFailure`s are a special failure type that will cause the ruleset evaluation to fail immediately, skipping any
further processing and discarding all other failures.

## Scopes

Scopes determine which effects are available and keep track of any rule overrides that should be applied.
You can extend the default scope or create a custom scope to control which effects are available within your
evaluations.

## Ruleset Grammar

Shogi supports a ruleset format designed to be concise and embeddable within string literals
such as a config string list. This way, rule evaluations can be defined even in regular TOML config options.

```
(* --- Top-level rule --- *)
<rule>          ::= [ <conditions> "->" ] <action>

(* --- Conditions --- *)
<conditions>    ::= <condition> ( "," <condition> )*
<condition>     ::= <identifier> [ "(" <arguments> ")" ]

(* --- Actions --- *)
<action>        ::= <assignment> | <function_call>
<assignment>    ::= <variable> "=" <expression>
<function_call> ::= <identifier> [ "(" <arguments> ")" ]

(* --- Arguments --- *)
<arguments>     ::= <argument> ( "," <argument> )*
<argument>      ::= <named_parameter> | <expression>
<named_parameter>  ::= <identifier> "=" <value>

(* --- Expressions --- *)
<expression>   ::= <term> ( ("+" | "-") <term> )*
<term>         ::= <factor> ( ("*" | "/") <factor> )*
<factor>       ::= <number> | <variable> | <function_call> | "(" <expression> ")"

(* --- Variables --- *)
<variable>     ::= "$" <identifier> ( "." <identifier> )*

(* --- Values --- *)
<value>        ::= <string> | <number> | <boolean> | <variable>

(* --- Primitives --- *)
<identifier>   ::= [a-zA-Z_][a-zA-Z0-9_]*
<number>       ::= [0-9]+ ( "." [0-9]+ )?
<string>       ::= '"' [^"]* '"' | "'" [^']* "'"
<boolean>      ::= "true" | "false"
```

## Why not just use [insert scripting language] or [insert expression engine]?

Shogi uses a declarative rule format, not an imperative scripting language.

Despite its use of brackets, expressions, variables and parameters, it is much closer to a JSON file than it is to a
script. In fact, Shogi supports loading rulesets from JSON directly, as conditions and actions all
build upon `MapCodec`s. The custom Shogi format is just a more compact and lenient way of expressing the same data, and 
gets translated to JSON upon loading.

An important strength of Shogi is also that it can support multiple results and failures, deferred values that are 
resolved and synced from the server, as well as aggregate results.

## Contributing

If you're interested in contributing to the mod, you can check
out [issues labeled as "help wanted"](https://github.com/TwelveIterations/Shogi/issues?q=is%3Aopen+is%3Aissue+label%3A%22help+wanted%22).

When it comes to new features, it's best to confer with me first to ensure we share the same vision. You can join us
on [Discord](https://discord.gg/36qHFMNgAh) if you'd like to talk.

Contributions must be done through pull requests. I will not be able to accept translations, code or other assets
through any other channels.
