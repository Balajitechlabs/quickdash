#!/usr/bin/env python3
import os
from PIL import Image, ImageDraw

# Exact bounding box of QuickDash floating dialog window
# (left, top, right, bottom)
BOX = (65, 319, 1015, 2134)
CORNER_RADIUS = 80

INPUT_DIR = "/Users/btl/.gemini/antigravity/brain/bae8e2f0-3282-4f9c-8e97-85c44178af87"
OUTPUT_DIR = "/Users/btl/Documents/btl-all-projects/quickdash/goldie/out/cropped"
RAW_DIR = "/Users/btl/Documents/btl-all-projects/quickdash/goldie/out/raw/pixel-10-pro"

os.makedirs(OUTPUT_DIR, exist_ok=True)
os.makedirs(RAW_DIR, exist_ok=True)

SCREENS = [
    ("home", "quickdash_home_screen.png"),
    ("about_dash", "quickdash_about_screen.png"),
    ("about_me", "quickdash_about_me_scrolled.png"),
    ("settings", "quickdash_settings_screen.png"),
    ("community", "quickdash_settings_community.png"),
]

print("Starting screenshot cropping...")

for scene_id, filename in SCREENS:
    src_path = os.path.join(INPUT_DIR, filename)
    if not os.path.exists(src_path):
        print(f"Warning: {src_path} not found, skipping.")
        continue

    img = Image.open(src_path)
    cropped = img.crop(BOX).convert("RGBA")

    # 1. Save pure cropped rectangle
    rect_out = os.path.join(OUTPUT_DIR, f"{scene_id}_rect.png")
    cropped.save(rect_out)

    # 2. Save with smooth rounded corners + alpha transparency
    mask = Image.new("L", cropped.size, 0)
    draw = ImageDraw.Draw(mask)
    draw.rounded_rectangle([(0, 0), cropped.size], radius=CORNER_RADIUS, fill=255)
    rounded = cropped.copy()
    rounded.putalpha(mask)
    rounded_out = os.path.join(OUTPUT_DIR, f"{scene_id}_rounded.png")
    rounded.save(rounded_out)

    # 3. Save standard raw capture for Goldie in raw/pixel-10-pro/
    # Goldie expects input images for framing.
    raw_out = os.path.join(RAW_DIR, f"{scene_id}.png")
    # For Goldie device framing, standard cropped rectangle fits cleanly inside the bezel
    cropped.save(raw_out)

    print(f"✓ Processed {scene_id} -> {cropped.size}")

print("All screenshots cropped successfully!")
