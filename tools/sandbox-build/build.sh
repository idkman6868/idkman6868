#!/bin/bash
# Offline-ish rebuild of the 0.2.0 jar WITHOUT the Minecraft/NeoForge toolchain.
#
# The normal way to build is `./gradlew build` (see README). This script exists because the environment the
# update was written in could not reach maven.neoforged.net or Mojang. It:
#   1. reads every Minecraft/NeoForge reference in the original 0.1.0 jar and generates compile-only stubs
#      (plus the hand-checked additions in stub-spec.txt),
#   2. compiles ONLY the new Shibuya / Culling Game classes against those stubs + the original classes,
#   3. raises the ability slots from 4 to 5 by patching six original classes in place (SlotPatch.java, which
#      checks every edit against the exact bytecode it expects),
#   4. copies the original classes, the new classes and src/main/resources into dist/cursed_domain-0.3.0.jar.
# The original classes are never recompiled; apart from the slot patch their bytecode is exactly what 0.1.0 shipped.
#
# Usage: tools/sandbox-build/build.sh path/to/cursed_domain-0.1.0.jar
set -euo pipefail
ORIG=$(realpath "$1")
HERE=$(cd "$(dirname "$0")" && pwd)
ROOT=$(cd "$HERE/../.." && pwd)
WORK=${WORK:-$ROOT/build/sandbox}
J=$ROOT/src/main/java/com/curseddomain
rm -rf "$WORK" && mkdir -p "$WORK"/{lib,orig,stubs,tools,classes,stage}
for a in asm asm-tree asm-analysis; do curl -sSfL -o "$WORK/lib/$a.jar" "https://repo1.maven.org/maven2/org/ow2/asm/$a/9.7/$a-9.7.jar"; done
(cd "$WORK/orig" && unzip -q "$ORIG")
CP_ASM="$WORK/lib/asm.jar:$WORK/lib/asm-tree.jar"
javac -nowarn -cp "$CP_ASM" -d "$WORK/tools" "$HERE/StubGen.java" "$HERE/SlotPatch.java"
java -cp "$WORK/tools:$CP_ASM" StubGen "$WORK/orig/com" "$HERE/stub-spec.txt" "$WORK/stubs"
javac -nowarn -d "$WORK/stubs" $(find "$HERE/annotations" -name '*.java')
FILES="$(find $J/cullinggame $J/shibuya $J/client/culling $J/landmark $J/school $J/incarnation -name '*.java') $J/datagen/lang/CullingLang.java"
javac -nowarn -Xlint:none -proc:none --release 21 -cp "$WORK/stubs:$WORK/orig" -d "$WORK/classes" $FILES
cp -r "$WORK/orig/com" "$WORK/stage/" && cp -r "$WORK/classes/com" "$WORK/stage/"
java -cp "$WORK/tools:$CP_ASM" SlotPatch "$WORK/stage" && cp -r "$ROOT/src/main/resources/." "$WORK/stage/"
mkdir -p "$WORK/stage/META-INF" && printf 'Manifest-Version: 1.0\r\n\r\n' > "$WORK/stage/META-INF/MANIFEST.MF"
mkdir -p "$ROOT/dist" && rm -f "$ROOT/dist/cursed_domain-0.3.0.jar"
(cd "$WORK/stage" && zip -qr -X "$ROOT/dist/cursed_domain-0.3.0.jar" META-INF/MANIFEST.MF . -x '.cache/*')
echo "built $ROOT/dist/cursed_domain-0.3.0.jar"
