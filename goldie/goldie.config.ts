import type { GoldieConfig } from "/Users/btl/.gemini/antigravity/scratch/goldie/src/config.ts";

const APP_ROOT = "/Users/btl/Documents/btl-all-projects/quickdash";

const config: GoldieConfig = {
  appRoot: APP_ROOT,
  bundleId: "com.balajitechlabs.quickdash",

  devices: ["pixel-10-pro"],
  locales: ["en-US"],
  appearance: "dark",
  frame: { variant: "17-pro-blue" },

  theme: {
    background: "linear-gradient(165deg, #07090E 0%, #0E131E 45%, #182030 100%)",
    headlineColor: "#FFFFFF",
    subheadColor: "#94A3B8",
    fontFamily: '"DM Sans", system-ui, -apple-system, sans-serif',
    copyHeightRatio: 0.22,
    deviceWidthRatio: 0.85,
    layout: "classic",
  },

  store: {
    name: "QuickDash",
    subtitle: { "en-US": "Floating Productivity Companion" },
    developer: "balajitechlabs",
    category: "Productivity",
    rating: 4.9,
    ratingCount: "2.5K Ratings",
    ageRating: "3+",
    price: "Free",
    description: {
      "en-US": "All-in-one floating productivity tools for Android. Access 16+ daily utilities anywhere with a single gesture.",
    },
  },

  scenes: [
    {
      kind: "screenshot",
      id: "home",
      flow: "home",
      headline: { "en-US": "16+ Floating Tools Anywhere" },
      subhead: { "en-US": "Instant clipboard, calculator, notes, and QR tools on top of any app." },
      decorations: [
        {
          kind: "badge",
          text: { "en-US": "16+ DAILY TOOLS" },
          position: "top-right",
          background: "rgba(15, 23, 42, 0.85)",
          color: "#38BDF8",
        },
      ],
    },
    {
      kind: "screenshot",
      id: "about_me",
      flow: "about_me",
      headline: { "en-US": "Crafted by balajitechlabs" },
      subhead: { "en-US": "Open source, zero trackers, and direct developer community profiles." },
      decorations: [
        {
          kind: "badge",
          text: { "en-US": "DEVELOPER PROFILES" },
          position: "top-right",
          background: "rgba(15, 23, 42, 0.85)",
          color: "#A78BFA",
        },
      ],
    },
    {
      kind: "screenshot",
      id: "settings",
      flow: "settings",
      headline: { "en-US": "True Pitch Black AMOLED" },
      subhead: { "en-US": "Material 3 Expressive theming, custom haptics, and gesture bubbles." },
      decorations: [
        {
          kind: "badge",
          text: { "en-US": "PITCH BLACK AMOLED" },
          position: "top-right",
          background: "rgba(15, 23, 42, 0.85)",
          color: "#4ADE80",
        },
      ],
    },
    {
      kind: "screenshot",
      id: "about_dash",
      flow: "about_dash",
      headline: { "en-US": "Instant Seamless Updates" },
      subhead: { "en-US": "Direct GitHub releases and built-in background APK updater." },
      decorations: [
        {
          kind: "badge",
          text: { "en-US": "IN-APP UPDATER" },
          position: "top-right",
          background: "rgba(15, 23, 42, 0.85)",
          color: "#F472B6",
        },
      ],
    },
    {
      kind: "screenshot",
      id: "community",
      flow: "community",
      headline: { "en-US": "Active Developer Community" },
      subhead: { "en-US": "Join our Telegram, Discord, and GitHub open-source discussions." },
      decorations: [
        {
          kind: "badge",
          text: { "en-US": "OPEN SOURCE" },
          position: "top-right",
          background: "rgba(15, 23, 42, 0.85)",
          color: "#FBBF24",
        },
      ],
    },
  ],
};

export default config;
