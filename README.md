# JoJo Addon (para Roundabout 3.6.3 / Forge 1.20.1)

## Requisitos
- **JDK 17** (obrigatório; Gradle 8.1.1 + ForgeGradle 6 não rodam em Java 21)
- **IntelliJ IDEA** (Community serve)
- Internet na primeira execução (baixa Forge, mappings e dependências, ~1 GB)

## Primeiro uso
1. Abra a pasta no IntelliJ (`File > Open` e escolha esta pasta) e deixe o Gradle importar.
   Em `Settings > Build Tools > Gradle`, confira que o **Gradle JVM = 17**.
2. Gere as run configs: `./gradlew genIntellijRuns` (Windows: `gradlew.bat genIntellijRuns`).
3. Rode `runClient` (ou a config "runClient" que aparece no topo do IntelliJ).
4. No jogo: crie um mundo, abra o criativo na aba **Ingredients**: o "Item de Teste do Addon" deve estar lá.
   No log deve aparecer `Roundabout carregado: true`.

## Estrutura
- `libs/roundabout-3.6.3.jar`: o mod base (remapeado por `fg.deobf` no build)
- `src/main/java/com/example/jojoaddon/JojoAddon.java`: classe principal
- `src/main/resources/META-INF/mods.toml`: declara dependência do `roundabout` (carrega depois dele)
- `gradle.properties`: modid, versão, versão do Forge/Roundabout

## Renomear para o seu addon
1. `gradle.properties`: `mod_id`, `mod_name`, `mod_group_id`, `mod_authors`
2. Renomeie a pasta/pacote `com/example/jojoaddon` e o `MOD_ID` em `JojoAddon.java`
3. Renomeie `assets/jojoaddon` para `assets/<seu_mod_id>` (e ajuste os JSONs dentro)

## Atualizar a versão do Roundabout
Troque o jar em `libs/` para `roundabout-<nova>.jar` e altere `roundabout_version` em `gradle.properties`.

## Gerar o jar do addon
`./gradlew build` -> `build/libs/jojoaddon-0.1.0.jar` (coloque na pasta `mods/` junto com o Roundabout).

## Problemas comuns
- *"Unsupported class file / Java version"*: o Gradle está usando Java 21. Use JDK 17.
- *Mixins do Roundabout falham em dev*: rode `./gradlew createSrgToMcp` e depois o `runClient` de novo
  (o arquivo `build/createSrgToMcp/output.srg` precisa existir).
- *Crédito/licença*: a Roundabout License exige dar crédito ao Hydraheads se você redistribuir.
  Não inclua o jar do Roundabout dentro do seu addon.
