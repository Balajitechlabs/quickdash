/* GENERATED FROM tokens.json -- DO NOT EDIT. Run scripts/build-tokens.mjs. */
// Portable design tokens (colors as hex). Web consumes the theme via
// src/index.css; mobile (Expo) and any other platform import this object so the
// whole product shares one source of truth.
export const tokens = {
  "color": {
    "light": {
      "background": "#ffffff",
      "foreground": "#0a0a0a",
      "border": "#e5e5e5",
      "card": "#ffffff",
      "cardForeground": "#0a0a0a",
      "popover": "#ffffff",
      "popoverForeground": "#0a0a0a",
      "primary": "#171717",
      "primaryForeground": "#fafafa",
      "secondary": "#f5f5f5",
      "secondaryForeground": "#171717",
      "muted": "#f5f5f5",
      "mutedForeground": "#737373",
      "accent": "#f5f5f5",
      "accentForeground": "#171717",
      "destructive": "#ef4444",
      "destructiveForeground": "#fafafa",
      "input": "#e5e5e5",
      "ring": "#171717",
      "chart1": "#e8643c",
      "chart2": "#2aa198",
      "chart3": "#274b5a",
      "chart4": "#e4b04a",
      "chart5": "#f08c3c",
      "sidebar": "#fafafa",
      "sidebarForeground": "#404040",
      "sidebarBorder": "#e5e5e5",
      "sidebarPrimary": "#171717",
      "sidebarPrimaryForeground": "#fafafa",
      "sidebarAccent": "#f5f5f5",
      "sidebarAccentForeground": "#171717",
      "sidebarRing": "#171717"
    },
    "dark": {
      "background": "#0a0a0a",
      "foreground": "#fafafa",
      "border": "#262626",
      "card": "#0a0a0a",
      "cardForeground": "#fafafa",
      "popover": "#0a0a0a",
      "popoverForeground": "#fafafa",
      "primary": "#fafafa",
      "primaryForeground": "#171717",
      "secondary": "#262626",
      "secondaryForeground": "#fafafa",
      "muted": "#262626",
      "mutedForeground": "#a3a3a3",
      "accent": "#262626",
      "accentForeground": "#fafafa",
      "destructive": "#7f1d1d",
      "destructiveForeground": "#fafafa",
      "input": "#262626",
      "ring": "#d4d4d4",
      "chart1": "#3b6fe0",
      "chart2": "#2bbf9c",
      "chart3": "#f08c3c",
      "chart4": "#a855d6",
      "chart5": "#e0497a",
      "sidebar": "#171717",
      "sidebarForeground": "#f5f5f5",
      "sidebarBorder": "#262626",
      "sidebarPrimary": "#3b6fe0",
      "sidebarPrimaryForeground": "#ffffff",
      "sidebarAccent": "#262626",
      "sidebarAccentForeground": "#f5f5f5",
      "sidebarRing": "#d4d4d4"
    }
  },
  "fontFamily": {
    "sans": [
      "Inter",
      "sans-serif"
    ],
    "serif": [
      "Georgia",
      "serif"
    ],
    "mono": [
      "Menlo",
      "monospace"
    ]
  },
  "radius": "0.5rem",
  "spacing": "0.25rem"
} as const;

export type Tokens = typeof tokens;
export default tokens;
