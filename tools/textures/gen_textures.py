#!/usr/bin/env python3
"""Procedurally generates the textures for the Shibuya Incident / Culling Game content.

  * entity/npc/<profile>.png   64x64 player-layout skins for the Culling Game players
  * entity/curse/<variant>.png recolours of the Grade 4 curse texture
  * item/prison_realm.png      the Prison Realm cube

Run from the repository root: python3 tools/textures/gen_textures.py
"""
import colorsys
import os
import random

from PIL import Image

ROOT = os.path.join(os.path.dirname(__file__), "..", "..", "src", "main", "resources", "assets", "cursed_domain", "textures")


def rgb(h):
    h = h.lstrip("#")
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c)


def mix(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))


# ---------------------------------------------------------------------------------------------- skins

class Skin:
    def __init__(self, seed):
        self.im = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
        self.rand = random.Random(seed)

    def px(self, x, y, c, a=255):
        self.im.putpixel((x, y), (c[0], c[1], c[2], a))

    def fill(self, x0, y0, x1, y1, c, noise=0.06):
        for y in range(y0, y1):
            for x in range(x0, x1):
                self.px(x, y, shade(c, 1.0 + self.rand.uniform(-noise, noise)))

    def box(self, ox, oy, w, h, d, c, noise=0.06):
        """Fills every face of a cuboid laid out in the standard skin format."""
        self.fill(ox + d, oy, ox + d + w, oy + d, shade(c, 1.08), noise)            # top
        self.fill(ox + d + w, oy, ox + d + 2 * w, oy + d, shade(c, 0.8), noise)     # bottom
        self.fill(ox, oy + d, ox + d, oy + d + h, shade(c, 0.9), noise)             # right
        self.fill(ox + d, oy + d, ox + d + w, oy + d + h, c, noise)                 # front
        self.fill(ox + d + w, oy + d, ox + 2 * d + w, oy + d + h, shade(c, 0.9), noise)  # left
        self.fill(ox + 2 * d + w, oy + d, ox + 2 * d + 2 * w, oy + d + h, shade(c, 0.85), noise)  # back

    # body part origins: (ox, oy, w, h, d)
    HEAD = (0, 0, 8, 8, 8)
    BODY = (16, 16, 8, 12, 4)
    RARM = (40, 16, 4, 12, 4)
    LARM = (32, 48, 4, 12, 4)
    RLEG = (0, 16, 4, 12, 4)
    LLEG = (16, 48, 4, 12, 4)

    def front_rows(self, part, y0, y1, c, faces=("right", "front", "left", "back")):
        """Colours rows y0..y1 (relative to the side faces) of a part all the way round."""
        ox, oy, w, h, d = part
        spans = {
            "right": (ox, ox + d),
            "front": (ox + d, ox + d + w),
            "left": (ox + d + w, ox + 2 * d + w),
            "back": (ox + 2 * d + w, ox + 2 * d + 2 * w),
        }
        for f in faces:
            x0, x1 = spans[f]
            self.fill(x0, oy + d + y0, x1, oy + d + y1, c)

    def save(self, path):
        os.makedirs(os.path.dirname(path), exist_ok=True)
        self.im.save(path)


def paint(spec, seed):
    s = Skin(seed)
    skin = rgb(spec.get("skin", "#e0b48c"))
    hair = rgb(spec.get("hair", "#2a2420"))
    top = rgb(spec.get("top", "#30343c"))
    pants = rgb(spec.get("pants", "#24262c"))
    shoes = rgb(spec.get("shoes", "#16161a"))
    sleeves = rgb(spec["sleeves"]) if "sleeves" in spec else top
    eyes = rgb(spec.get("eyes", "#3a2a1e"))

    # head and face
    s.box(*Skin.HEAD, skin, 0.03)
    style = spec.get("style", "short")
    if style != "bald":
        s.fill(8, 0, 16, 8, hair)                     # top of head
        s.fill(24, 8, 32, 16 if style in ("long", "bun", "pompadour") else 12, hair)  # back
        s.fill(0, 8, 8, 10, hair)                     # right side
        s.fill(16, 8, 24, 10, hair)                   # left side
        s.fill(8, 8, 16, 9, hair)                     # fringe
        if style in ("long", "bun"):
            s.fill(0, 10, 2, 16, hair)
            s.fill(22, 10, 24, 16, hair)
            s.fill(6, 10, 8, 14, hair)
            s.fill(16, 10, 18, 14, hair)
        if style == "spiky":
            for x in range(8, 16, 2):
                s.px(x, 9, hair)
            for x in (0, 2, 4, 6, 16, 18, 20, 22):
                s.px(x, 10, hair)
        if style == "parted":
            s.fill(8, 9, 11, 10, hair)
        if style == "pompadour":
            s.fill(40, 0, 48, 8, shade(hair, 1.1))     # hat top
            s.fill(40, 8, 48, 10, hair)                # hat front, a big quiff
    if style == "bun":
        s.fill(42, 0, 46, 4, shade(hair, 0.9))        # bun on the hat layer top
    # eyes and mouth
    white = (238, 238, 232)
    s.px(9, 12, white); s.px(10, 12, eyes); s.px(13, 12, eyes); s.px(14, 12, white)
    s.px(11, 14, shade(skin, 0.75)); s.px(12, 14, shade(skin, 0.75))
    for (x, y, c) in spec.get("face", []):
        s.px(x, y, rgb(c))

    # body, arms, legs
    s.box(*Skin.BODY, top)
    s.box(*Skin.RARM, sleeves)
    s.box(*Skin.LARM, sleeves)
    if spec.get("bare_arms"):
        s.front_rows(Skin.RARM, 3, 12, skin)
        s.front_rows(Skin.LARM, 3, 12, skin)
    else:
        s.front_rows(Skin.RARM, 11, 12, skin)        # hands
        s.front_rows(Skin.LARM, 11, 12, skin)
    if spec.get("bare_torso"):
        s.fill(20, 20, 28, 28, skin)
        s.fill(32, 20, 40, 28, shade(skin, 0.9))
    s.box(*Skin.RLEG, pants)
    s.box(*Skin.LLEG, pants)
    s.front_rows(Skin.RLEG, 10, 12, shoes)
    s.front_rows(Skin.LLEG, 10, 12, shoes)

    # accents
    if "belt" in spec:
        s.front_rows(Skin.BODY, 9, 10, rgb(spec["belt"]))
    if "collar" in spec:
        s.fill(22, 20, 26, 22, rgb(spec["collar"]))
    if "tie" in spec:
        s.fill(23, 21, 25, 27, rgb(spec["tie"]))
    if "sash" in spec:
        c = rgb(spec["sash"])
        for i in range(8):
            s.px(20 + i, 20 + i, c)
            s.px(20 + i, 21 + i, c) if i < 7 else None
    if "open_coat" in spec:
        s.fill(23, 20, 25, 32, rgb(spec["open_coat"]))
    if "speckle" in spec:
        c = rgb(spec["speckle"])
        for _ in range(28):
            x = s.rand.randrange(16, 40)
            y = s.rand.randrange(20, 32)
            s.px(x, y, c)
    if spec.get("stitches"):
        for x in range(8, 16):
            s.px(x, 9 if x % 2 else 10, (60, 30, 30))
    if spec.get("glasses"):
        for x in (9, 10, 13, 14):
            s.px(x, 12, (40, 40, 48))
        s.px(11, 12, (40, 40, 48)); s.px(12, 12, (40, 40, 48))
    if spec.get("halo"):
        for x in range(41, 47):
            s.px(x, 0, (250, 222, 120))
    return s


SKINS = {
    "hiromi_higuruma": dict(hair="#1c1a1a", style="parted", top="#3a3d44", collar="#e8e8e8", tie="#8c1c24", pants="#2e3036", shoes="#141414", eyes="#20160e"),
    "reggie_star": dict(hair="#d8c070", style="spiky", top="#1e1f24", speckle="#f2f0e6", pants="#30323a"),
    "iori_hazenoki": dict(hair="#5a3c24", top="#b49a74", open_coat="#42322a", pants="#3a3026"),
    "chizuru_hari": dict(hair="#4a3020", top="#2f6b46", collar="#e0e0d8", pants="#22262a"),
    "remi": dict(hair="#7a3c8c", style="long", top="#d86fa0", pants="#3a2440", eyes="#5a2a6a"),
    "hanyu": dict(hair="#e2c860", style="spiky", top="#b82a2a", pants="#262a34"),
    "haba": dict(style="bald", top="#8a8a86", bare_arms=True, pants="#3a3a42"),
    "fumihiko_takaba": dict(hair="#141414", style="parted", top="#e6c23a", collar="#ffffff", tie="#2a4fb0", pants="#e6c23a", shoes="#2a1a12"),
    "hana_kurusu": dict(hair="#c49a6a", style="long", top="#f0ede6", sleeves="#e4e0d6", pants="#e8e4dc", shoes="#d8d2c4", eyes="#4a6a9a", halo=True),
    "hajime_kashimo": dict(hair="#8ad4e0", style="bun", skin="#f0d2b8", top="#f2f2f2", belt="#2a2a30", pants="#2a2c34", eyes="#2a6a8a", bare_arms=True),
    "charles_bernard": dict(hair="#e4cc80", top="#e8eef6", sleeves="#3c5a8c", collar="#3c5a8c", pants="#2c3a56", glasses=True),
    "ryu_ishigori": dict(hair="#121212", style="pompadour", top="#f0f0ec", open_coat="#1a1a1a", pants="#1c1c22"),
    "takako_uro": dict(hair="#20202a", style="long", top="#4a2a5a", sleeves="#3a2048", pants="#2a1a34", eyes="#3a2a5a"),
    "dhruv_lakdawalla": dict(hair="#141012", style="long", skin="#8a5a3a", top="#8c2a20", sash="#d8a83a", pants="#5a1c16"),
    "hagane_daido": dict(hair="#141414", style="bun", top="#2c3e6a", sleeves="#2c3e6a", belt="#c8b48a", pants="#1e2a48", shoes="#3a2a1a"),
    "rokujushi_miyo": dict(hair="#141414", style="bun", skin="#e8b890", bare_torso=True, bare_arms=True, top="#e8b890", belt="#f0f0f0", pants="#e8b890", shoes="#e8b890"),
    "kenjaku": dict(hair="#141216", style="long", top="#2a2420", sleeves="#2a2420", sash="#8a6a3a", pants="#1a1614", stitches=True, eyes="#2a1a12"),
    "sukuna": dict(hair="#1a1a24", style="spiky", top="#ece6d8", sleeves="#ece6d8", belt="#2a2a2a", pants="#2a2a2e", eyes="#b01818",
                   face=[(9, 13, "#2a1010"), (14, 13, "#2a1010"), (9, 11, "#b01818"), (14, 11, "#b01818")]),
    "awakened_player": dict(hair="#3a2a20", top="#1e2a44", collar="#d8d8d8", pants="#1e2232"),
    "incarnated_sorcerer": dict(hair="#18161a", style="long", top="#6a6a70", sash="#2a2a30", pants="#3a3a40"),
}


def make_skins():
    for i, (name, spec) in enumerate(sorted(SKINS.items())):
        paint(spec, 1000 + i).save(os.path.join(ROOT, "entity", "npc", name + ".png"))


# ---------------------------------------------------------------------------------------------- curses

CURSES = {
    # shadow, mid, highlight
    "grade_3": ("#2a0c10", "#6a1c22", "#a8343a"),
    "grade_2": ("#100c2a", "#2a2266", "#4e46a8"),
    "grade_1": ("#0a0a0c", "#24222a", "#8a7430"),
    "jogo": ("#2a2622", "#6a645a", "#e2621c"),
    "hanami": ("#1e2a14", "#5a7038", "#e8e2c8"),
    "dagon": ("#0a2026", "#1c5a64", "#4cb0a8"),
    "mahito": ("#1c2230", "#5a6a8a", "#c8d0dc"),
    "kurourushi": ("#140c06", "#3a2412", "#7a5430"),
    "naoya": ("#3a3428", "#b0a890", "#f4e8b8"),
}


def is_feature(c):
    """Eyes and teeth of the base curse: bright, warm pixels."""
    r, g, b = c[:3]
    h, l, sat = colorsys.rgb_to_hls(r / 255, g / 255, b / 255)
    return l > 0.55 and (h < 0.2 or sat < 0.35)


def make_curses():
    base = Image.open(os.path.join(ROOT, "entity", "grade_4_curse.png")).convert("RGBA")
    lums = [sum(p[:3]) / 3 for p in base.getdata() if p[3] > 0 and not is_feature(p)]
    lo, hi = min(lums), max(lums)
    for name, (a, b, c) in CURSES.items():
        a, b, c = rgb(a), rgb(b), rgb(c)
        rand = random.Random(name)
        out = Image.new("RGBA", base.size)
        for y in range(base.size[1]):
            for x in range(base.size[0]):
                p = base.getpixel((x, y))
                if p[3] == 0:
                    out.putpixel((x, y), p)
                    continue
                if is_feature(p):
                    feat = p[:3]
                    if name == "jogo":
                        feat = mix(feat, (255, 120, 30), 0.6)
                    elif name in ("mahito", "naoya"):
                        feat = mix(feat, (230, 240, 255), 0.5)
                    out.putpixel((x, y), feat + (p[3],))
                    continue
                t = (sum(p[:3]) / 3 - lo) / max(1, hi - lo)
                col = mix(a, b, t * 2) if t < 0.5 else mix(b, c, (t - 0.5) * 2)
                # a few variant-specific marks
                if name == "hanami" and rand.random() < 0.04:
                    col = (90, 160, 60)
                elif name == "mahito" and (x + y) % 9 == 0:
                    col = shade(col, 0.6)
                elif name == "jogo" and y < 12 and rand.random() < 0.15:
                    col = (230, 90, 20)
                elif name == "dagon" and y % 4 == 0:
                    col = shade(col, 0.8)
                elif name == "grade_1" and rand.random() < 0.03:
                    col = (200, 170, 70)
                out.putpixel((x, y), col + (p[3],))
        path = os.path.join(ROOT, "entity", "curse", name + ".png")
        os.makedirs(os.path.dirname(path), exist_ok=True)
        out.save(path)


# ---------------------------------------------------------------------------------------------- prison realm

def make_prison_realm():
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    rand = random.Random(4)
    edge, face_l, face_r, top = rgb("#1a1420"), rgb("#4a3a52"), rgb("#362a3e"), rgb("#5e4a66")
    # an isometric-ish cube: top diamond rows 2..6, faces rows 6..14
    for y in range(2, 15):
        for x in range(2, 14):
            if y < 7:
                half = (y - 2) + 2
                if 8 - half <= x < 8 + half:
                    im.putpixel((x, y), shade(top, rand.uniform(0.9, 1.1)) + (255,))
            else:
                c = face_l if x < 8 else face_r
                im.putpixel((x, y), shade(c, rand.uniform(0.9, 1.1)) + (255,))
    for y in range(7, 15):
        im.putpixel((2, y), edge + (255,)); im.putpixel((13, y), edge + (255,)); im.putpixel((8, y), edge + (255,))
    for x in range(2, 14):
        im.putpixel((x, 14), edge + (255,))
    # eyes on the faces
    for (x, y) in [(4, 9), (6, 11), (4, 12), (10, 9), (11, 12), (9, 11), (7, 4), (9, 5)]:
        im.putpixel((x, y), (236, 230, 214, 255))
        if x + 1 < 16:
            im.putpixel((x + 1, y), (30, 20, 20, 255))
    path = os.path.join(ROOT, "item", "prison_realm.png")
    im.save(path)


if __name__ == "__main__":
    make_skins()
    make_curses()
    make_prison_realm()
    print("textures written to", os.path.abspath(ROOT))
