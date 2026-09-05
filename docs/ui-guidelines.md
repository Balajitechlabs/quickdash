# UI Guidelines and Design System

## Design Tokens

QuickDash adopts a unified, semantic token architecture defined in `Dimens.kt` and `Theme.kt`:

### Spacing Scale
- `Spacing.xxs`: 2dp
- `Spacing.xs`: 4dp
- `Spacing.sm`: 8dp
- `Spacing.md`: 12dp
- `Spacing.lg`: 16dp
- `Spacing.xl`: 20dp
- `Spacing.xxl`: 24dp
- `Spacing.hero`: 32dp

### Corner Radius Scale
- `Radius.sm`: 8dp
- `Radius.md`: 12dp
- `Radius.lg`: 16dp
- `Radius.xl`: 20dp
- `Radius.card`: 24dp
- `Radius.pill`: 28dp
- `Radius.full`: 999dp (Circle)

## Motion Physics

Animations are defined centrally in `QuickDashMotion.kt`. The motion system uses physics-based spring transitions with customizable damping and stiffness:
- Floating bubble drag-and-snap utilizes high-response spring stiffness.
- Modal bottom sheets expand with smooth overshoot damping.
- Accessibility: Motion respects the user's `reduceMotion` accessibility preference.

---
Copyright (c) 2026 ||BTL||™ (balajitechlabs)
