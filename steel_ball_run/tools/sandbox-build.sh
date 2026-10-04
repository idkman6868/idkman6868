#!/bin/bash
# Builds dist/steel_ball_run-<version>.jar WITHOUT the Minecraft/NeoForge toolchain.
#
# The normal way to build is `./gradlew build` (see README). This script exists because the environment the mod
# was written in could not reach maven.neoforged.net or Mojang. It:
#   1. generates compile-only stubs of the Minecraft/NeoForge API from the bytecode of the Cursed Domain jar in the
#      parent repository (../dist/cursed_domain-0.3.0.jar), plus the parent's stub-spec.txt and this mod's
#      tools/stub-spec-extra.txt (hand-written signatures of every 1.21.1 API this mod uses that the stubs lack),
#   2. compiles the mod against those stubs,
#   3. runs the pure-Java logic tests (tools/LogicTests.java),
#   4. writes assets/steel_ball_run/lang/en_us.json from SbrLang,
#   5. checks every class with ASM's structural verifier and zips the jar.
# Stubs only prove the code type-checks against the signatures written down; they don't prove those signatures
# match the real game. Build with Gradle to be sure.
#
# Usage: tools/sandbox-build.sh [--compile-only]
set -euo pipefail
export JAVA_TOOL_OPTIONS=
HERE=$(cd "$(dirname "$0")" && pwd)
ROOT=$(cd "$HERE/.." && pwd)
PARENT=$(cd "$ROOT/.." && pwd)
TOOLS=$PARENT/tools/sandbox-build
VERSION=$(grep '^mod_version=' "$ROOT/gradle.properties" | cut -d= -f2)
WORK=${WORK:-$ROOT/build/sandbox}
mkdir -p "$WORK"/{lib,orig,stubs,tools,classes,stage,tests}
for a in asm asm-tree asm-analysis asm-util; do
  [ -f "$WORK/lib/$a.jar" ] || curl -sSfL -o "$WORK/lib/$a.jar" "https://repo1.maven.org/maven2/org/ow2/asm/$a/9.7/$a-9.7.jar"
done
CP_ASM="$WORK/lib/asm.jar:$WORK/lib/asm-tree.jar:$WORK/lib/asm-analysis.jar:$WORK/lib/asm-util.jar"
rm -rf "$WORK/orig" "$WORK/stubs" "$WORK/classes" "$WORK/stage" && mkdir -p "$WORK"/{orig,stubs,classes,stage}
(cd "$WORK/orig" && unzip -q "$PARENT/dist/cursed_domain-0.3.0.jar")
javac -nowarn -cp "$CP_ASM" -d "$WORK/tools" "$TOOLS/StubGen.java" "$TOOLS/Verify.java" "$HERE/StubPrune.java"
cat "$TOOLS/stub-spec.txt" "$HERE/stub-spec-extra.txt" > "$WORK/spec.txt"
java -cp "$WORK/tools:$CP_ASM" StubGen "$WORK/orig/com" "$WORK/spec.txt" "$WORK/stubs" >/dev/null
java -cp "$WORK/tools:$CP_ASM" StubPrune "$WORK/stubs"
javac -nowarn -d "$WORK/stubs" $(find "$TOOLS/annotations" -name '*.java')
# Brigadier (commands) and Netty's ByteBuf are open source, so compile against the real thing instead of stubs.
for a in netty-buffer netty-common; do
  [ -f "$WORK/lib/$a.jar" ] || curl -sSfL -o "$WORK/lib/$a.jar" "https://repo1.maven.org/maven2/io/netty/$a/4.1.97.Final/$a-4.1.97.Final.jar"
done
if [ ! -d "$WORK/brigadier/com" ]; then
  B=https://raw.githubusercontent.com/Mojang/brigadier/master/src/main/java/com/mojang/brigadier
  mkdir -p "$WORK/brigadier-src"
  for f in $(cat "$HERE/brigadier-files.txt"); do
    mkdir -p "$WORK/brigadier-src/$(dirname "$f")"
    curl -sSf -o "$WORK/brigadier-src/$f.java" "$B/$f.java"
  done
  javac -nowarn -d "$WORK/brigadier" $(find "$WORK/brigadier-src" -name '*.java')
fi
CP="$WORK/brigadier:$WORK/lib/netty-buffer.jar:$WORK/lib/netty-common.jar:$WORK/stubs"
javac -Xmaxerrs 400 -nowarn -Xlint:none -proc:none --release 21 -cp "$CP" -d "$WORK/classes" $(find "$ROOT/src/main/java" -name '*.java')
[ "${1:-}" = "--compile-only" ] && { echo "compiled"; exit 0; }
javac -nowarn -cp "$WORK/classes" -d "$WORK/tests" "$HERE/LogicTests.java"
java -ea -cp "$WORK/tests:$WORK/classes" LogicTests
java -cp "$WORK/classes" com.steelballrun.datagen.SbrLang "$ROOT/src/main/resources/assets/steel_ball_run/lang/en_us.json"
java -cp "$WORK/tools:$CP_ASM" Verify $(find "$WORK/classes" -name "*.class") | tee "$WORK/verify.txt" | tail -1; grep -q " 0 failures" "$WORK/verify.txt"
cp -r "$WORK/classes/com" "$WORK/stage/" && cp -r "$ROOT/src/main/resources/." "$WORK/stage/"
mkdir -p "$WORK/stage/META-INF" && printf 'Manifest-Version: 1.0\r\n\r\n' > "$WORK/stage/META-INF/MANIFEST.MF"
mkdir -p "$ROOT/dist" && rm -f "$ROOT/dist/steel_ball_run-$VERSION.jar"
(cd "$WORK/stage" && zip -qr -X "$ROOT/dist/steel_ball_run-$VERSION.jar" META-INF/MANIFEST.MF . -x '.cache/*')
echo "built $ROOT/dist/steel_ball_run-$VERSION.jar"
