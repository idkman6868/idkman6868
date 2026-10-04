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


