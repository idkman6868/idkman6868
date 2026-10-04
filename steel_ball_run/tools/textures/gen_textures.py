#!/usr/bin/env python3
"""Procedurally generates every texture in Steel Ball Run. Nothing here is traced from the manga.

  * entity/stephen_steel.png, entity/rival/<skin>.png   64x64 player-layout skins
  * item/<item>.png                                      16x16 item icons
  * models/item/<item>.json                              plain generated item models

Run from the mod folder: python3 tools/textures/gen_textures.py   (needs Pillow)
"""
import os
import random
import zlib

from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
ASSETS = os.path.join(HERE, "..", "..", "src", "main", "resources", "assets", "steel_ball_run")
TEX = os.path.join(ASSETS, "textures")

exec(open(os.path.join(HERE, "_skinlib.py")).read())  # rgb, shade, mix, Skin, paint (shared with Cursed Domain)


# ---------------------------------------------------------------------------------------------- skins

def hat(s, colour, band=None, tall=False):
    """A hat on the overlay layer: crown on top, brim rows round the sides."""
    c = rgb(colour)
    s.fill(40, 0, 48, 8, shade(c, 1.05))                # crown top
    rows = 4 if tall else 2
    s.fill(32, 8, 64, 8 + rows, c)                      # all four sides
    if band:
        s.fill(32, 8 + rows - 1, 64, 8 + rows, rgb(band))
    # make sure the face below the brim stays visible: clear front overlay under the hat
    for y in range(8 + rows, 16):
        for x in range(40, 48):
            s.im.putpixel((x, y), (0, 0, 0, 0))


def moustache(s, colour):
    c = rgb(colour)
    for x in range(10, 14):
        s.px(x, 14, c)
    s.px(9, 15, c)
    s.px(14, 15, c)


SKINS = {
    # name: (paint spec, extras)
    "stephen_steel": (dict(hair="#d8d4cc", skin="#e8c4a0", top="#2a2a30", sleeves="#2a2a30", collar="#f0f0ea", tie="#7a1a20",
                           pants="#24242a", shoes="#141414", eyes="#3a3a3a"),
                      dict(hat=("#1a1a1e", "#5a1a1a", True), moustache="#d8d4cc")),
    "gyro_zeppeli": (dict(hair="#e8cc68", style="long", skin="#e8c09a", top="#3a5a3a", sleeves="#2e4a2e", belt="#c8a040", pants="#2a3a2a",
                          shoes="#3a2a1a", eyes="#3a6a3a", face=[(11, 15, "#e0c040"), (12, 15, "#e0c040")]),
                     dict(hat=("#3a5a3a", "#d8b040", False))),
    "johnny_joestar": (dict(hair="#d8b878", skin="#f0d0b0", top="#2a3858", sleeves="#2a3858", belt="#c09050", pants="#1e2840",
                            shoes="#2a1a12", eyes="#3a5a8a"),
                       dict(hat=("#e8e4d8", "#2a3858", False))),
    "diego_brando": (dict(hair="#e8d080", skin="#f0d4b4", top="#5a3a6a", sleeves="#4a2e5a", collar="#e8e0d0", pants="#2a1e30",
                          shoes="#141014", eyes="#3a2a5a"),
                     dict(hat=("#4a2e5a", "#c8a040", True))),
    "sandman": (dict(hair="#141012", style="long", skin="#a87050", top="#c8a878", sleeves="#a87050", sash="#8a3a2a", pants="#7a5a3a",
                     shoes="#5a3a22", eyes="#1a1210", bare_arms=True), {}),
    "pocoloco": (dict(hair="#141012", skin="#6a4630", top="#d8c070", sleeves="#d8c070", belt="#6a2a1a", pants="#3a3a5a", shoes="#2a1a12",
                      eyes="#1a1210"),
                 dict(hat=("#c84a2a", "#f0e0a0", False))),
    "hot_pants": (dict(hair="#c0a070", style="long", skin="#f0d0b8", top="#e8e2d0", sleeves="#e8e2d0", belt="#2a2a30", pants="#5a5a68",
                       shoes="#2a2a30", eyes="#4a6a3a"),
                  dict(hat=("#e8e2d0", "#3a3a40", True))),
    "mountain_tim": (dict(hair="#3a2a1a", skin="#e0b890", top="#b07040", sleeves="#9a6038", open_coat="#e8d8b0", belt="#3a2a1a",
                          pants="#4a5a7a", shoes="#3a2a1a", eyes="#3a2a1a"),
                     dict(hat=("#c8a878", "#3a2a1a", True))),
    "generic_0": (dict(hair="#4a3020", top="#8a4a2a", collar="#e0d8c8", pants="#3a3a44", shoes="#2a1a12"), dict(hat=("#5a3a22", None, True))),
    "generic_1": (dict(hair="#1a1612", skin="#c89068", top="#3a5a7a", belt="#5a3a22", pants="#2a3040", shoes="#1a1410"),
                  dict(hat=("#2a2a2a", "#8a2a2a", False))),
    "generic_2": (dict(hair="#8a4a22", style="long", top="#6a3a5a", sleeves="#5a2e4a", pants="#3a2a34", shoes="#2a1a1a", eyes="#3a5a3a"),
                  dict(hat=("#d8c8a8", "#6a3a5a", False))),
    "generic_3": (dict(hair="#e0d0a0", top="#e8e0cc", open_coat="#5a4a3a", pants="#4a4a52", shoes="#2a2420"), dict(hat=("#7a6a5a", None, True))),
}


def make_skins():
    for name, (spec, extra) in SKINS.items():
        s = paint(spec, zlib.crc32(name.encode()))
        if "hat" in extra:
            colour, band, tall = extra["hat"]
            hat(s, colour, band, tall)
        if "moustache" in extra:
            moustache(s, extra["moustache"])
        path = os.path.join(TEX, "entity", "stephen_steel.png" if name == "stephen_steel" else os.path.join("rival", name + ".png"))
        s.save(path)


# ---------------------------------------------------------------------------------------------- items

def icon(draw):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw(im)
    return im


def rect(im, x0, y0, x1, y1, c, noise=0.0, seed=0):
    r = random.Random(seed)
    for y in range(y0, y1):
        for x in range(x0, x1):
            col = shade(rgb(c), 1.0 + r.uniform(-noise, noise)) if noise else rgb(c)
            im.putpixel((x, y), col + (255,))


def outline(im, c):
    """Darkens the edge pixels of the drawn shape."""
    px = im.load()
    edge = []
    for y in range(16):
        for x in range(16):
            if px[x, y][3] == 0:
                continue
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                nx, ny = x + dx, y + dy
                if not (0 <= nx < 16 and 0 <= ny < 16) or px[nx, ny][3] == 0:
                    edge.append((x, y))
                    break
    for x, y in edge:
        im.putpixel((x, y), rgb(c) + (255,))


def map_of_america(im):
    rect(im, 1, 3, 15, 13, "#e8d8a8", 0.06, 1)
    outline(im, "#8a6a3a")
    for x, y in ((3, 10), (4, 9), (5, 9), (6, 8), (7, 8), (8, 7), (9, 7), (10, 6), (11, 6), (12, 5)):
        im.putpixel((x, y), rgb("#a02020") + (255,))
    im.putpixel((2, 10), rgb("#2a2a2a") + (255,))
    im.putpixel((13, 5), rgb("#2a2a2a") + (255,))
    for y in (4, 12):
        for x in range(2, 14, 3):
            im.putpixel((x, y), rgb("#c0a870") + (255,))


def horse_brush(im):
    rect(im, 3, 4, 13, 9, "#8a5a32", 0.08, 2)
    outline(im, "#4a2e18")
    for x in range(4, 13, 2):
        for y in range(9, 13):
            im.putpixel((x, y), rgb("#d8c8a0" if y < 12 else "#a89870") + (255,))


def race_number(im):
    rect(im, 2, 2, 14, 14, "#f0ece0", 0.03, 3)
    outline(im, "#8a8478")
    rect(im, 2, 2, 14, 4, "#2a4a8a")
    digits = {"7": [(6, 6), (7, 6), (8, 6), (9, 6), (9, 7), (8, 8), (8, 9), (7, 10), (7, 11)]}
    for x, y in digits["7"]:
        im.putpixel((x, y), rgb("#a01818") + (255,))


def dollar(im):
    rect(im, 1, 4, 15, 12, "#7aa070", 0.05, 4)
    outline(im, "#3a5a32")
    rect(im, 6, 6, 10, 10, "#d8e8c8")
    for x, y in ((7, 6), (8, 6), (7, 7), (8, 8), (7, 9), (8, 9)):
        im.putpixel((x, y), rgb("#2a4a22") + (255,))


def prize_cheque(im):
    rect(im, 1, 4, 15, 12, "#f4ecd0", 0.03, 5)
    outline(im, "#a08a50")
    for x in range(3, 13):
        im.putpixel((x, 6), rgb("#9a9078") + (255,))
    for x in range(3, 9):
        im.putpixel((x, 9), rgb("#2a2a6a") + (255,))
    rect(im, 10, 8, 13, 11, "#c8a030")


def trophy(im):
    gold, dark = "#e8c040", "#a07818"
    rect(im, 3, 2, 13, 4, gold)
    rect(im, 4, 4, 12, 8, gold, 0.06, 6)
    rect(im, 6, 8, 10, 10, gold)
    rect(im, 7, 10, 9, 12, gold)
    rect(im, 4, 12, 12, 14, "#6a4a2a")
    im.putpixel((2, 3), rgb(gold) + (255,))
    im.putpixel((2, 4), rgb(gold) + (255,))
    im.putpixel((13, 3), rgb(gold) + (255,))
    im.putpixel((13, 4), rgb(gold) + (255,))
    outline(im, dark)
    im.putpixel((6, 4), rgb("#fff4b0") + (255,))
    im.putpixel((6, 5), rgb("#fff4b0") + (255,))


ITEMS = {
    "map_of_america": map_of_america,
    "horse_brush": horse_brush,
    "race_number": race_number,
    "dollar": dollar,
    "prize_cheque": prize_cheque,
    "trophy": trophy,
}


def make_items():
    for name, draw in ITEMS.items():
        path = os.path.join(TEX, "item", name + ".png")
        os.makedirs(os.path.dirname(path), exist_ok=True)
        icon(draw).save(path)
        model = os.path.join(ASSETS, "models", "item", name + ".json")
        os.makedirs(os.path.dirname(model), exist_ok=True)
        with open(model, "w") as f:
            f.write('{\n  "parent": "minecraft:item/generated",\n  "textures": {\n    "layer0": "steel_ball_run:item/%s"\n  }\n}\n' % name)


if __name__ == "__main__":
    make_skins()
    make_items()
    print("textures written to", os.path.normpath(TEX))
