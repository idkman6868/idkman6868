#!/usr/bin/env python3
"""Merges CullingLang (and CullingConfig's config labels) into src/main/resources/.../en_us.json.

Normally `./gradlew runData` does this. Usage (from the repo root):
  java -cp <dir with compiled LangDump + CullingLang + langshim> LangDump > lang.dump
  python3 tools/sandbox-build/merge_lang.py lang.dump
"""
import json
import re
import sys

ROOT = __file__.rsplit("/tools/", 1)[0]
raw = open(sys.argv[1], encoding="utf-8").read()
new = {}
for rec in raw.split("\x01"):
    if rec:
        k, v = rec.split("\x00")
        new[k] = v

src = open(ROOT + "/src/main/java/com/curseddomain/cullinggame/CullingConfig.java").read()


def humanize(n):
    spaced = re.sub(r"([a-z])([A-Z0-9])", r"\1 \2", n.replace("_", " "))
    return " ".join(w[:1].upper() + w[1:] for w in spaced.split(" "))


pre = "cursed_domain.configuration."
for sec in re.findall(r'b\.push\("(\w+)"\)', src):
    new[pre + sec] = humanize(sec)
for m in re.finditer(r'comment\("((?:[^"\\]|\\.)*)"\)\s*\.(define|defineInRange)\("(\w+)",\s*([^)]*)\)', src):
    comment, kind, name, args = m.groups()
    comment = comment.replace('\\"', '"')
    if kind == "defineInRange":
        a = [x.strip() for x in args.split(",")]
        comment += "\nRange: %s ~ %s" % (a[1], a[2])
    new[pre + name] = humanize(name)
    new[pre + name + ".tooltip"] = comment
section = pre + "section.cursed.domain.culling.server.toml"
new[section] = "Shibuya & Culling Game Settings"
new[section + ".title"] = "Cursed Domain: Shibuya & Culling Game Settings"

path = ROOT + "/src/main/resources/assets/cursed_domain/lang/en_us.json"
data = json.load(open(path, encoding="utf-8"))
data.update(new)
open(path, "w", encoding="utf-8").write(json.dumps(dict(sorted(data.items())), indent=2, ensure_ascii=False) + "\n")
print("merged", len(new), "keys;", len(data), "total")
