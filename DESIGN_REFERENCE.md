# Dasurv Android - Design Reference Guide
> Sourced from vivi-music project. Use this as a basis for theming and UI decisions.

## Documentation Index

| File | Contents |
|------|----------|
| **DESIGN_REFERENCE.md** (this file) | Colors, Typography, Shapes, Dimensions, Quick Reference |
| **DESIGN_COMPONENTS_CORE.md** | List Items, Grid Items, Thumbnails, Buttons, Search Bar |
| **DESIGN_COMPONENTS_SURFACES.md** | Dialogs, Bottom Sheets, Chips, Mini Player, Sliders, Swipe |
| **DESIGN_COMPONENTS_MISC.md** | Preferences, Sort, Navigation, Loading States, Text, Lyrics, Checkbox |
| **DESIGN_THEME_AND_MOTION.md** | Theme Architecture, Icons, Animations, Gradients, Settings, Widgets |

---

## 1. Color System

### Current Dasurv Colors (Rose Theme)
| Token | Light | Dark |
|-------|-------|------|
| Primary | `#CE5B78` | `#CE5B78` |
| Secondary | `#D4737E` | `#D4737E` |
| Tertiary | `#F4A7B9` | `#F4A7B9` |
| Primary Dark | `#9E4B5E` | `#9E4B5E` |
| Surface | `#FFF8F9` | `#1A1114` |
| OnSurface | `#201A1C` | `#EDE0E2` |
| Error | `#BA1A1A` | `#BA1A1A` |

### Vivi-Music Reference Colors (Adoptable Ideas)

**Default Accent Color**: `#ED5564` (Red/Pink - close to Dasurv's rose theme)

**Widget Colors - Light Mode**:
| Token | Value | Use |
|-------|-------|-----|
| Widget BG | `#FFFFFF` | Widget card background |
| Primary Text | `#000000` | Main widget text |
| Secondary Text | `#757575` | Subtitle/secondary text |

**Widget Colors - Dark Mode**:
| Token | Value | Use |
|-------|-------|-----|
| Widget BG | `#1F1F1F` | Widget card background |
| Primary Text | `#FFFFFF` | Main widget text |
| Secondary Text | `#B3FFFFFF` (70% white) | Subtitle/secondary text |

**App Icon Background**: `#2D184C` (Dark Purple)

### Dynamic Color Strategy
1. **Android 12+**: Use `dynamicDarkColorScheme()` / `dynamicLightColorScheme()` for system wallpaper colors
2. **Custom Seed Color**: Use MaterialKolor library with `TonalSpot` palette style
3. **Pure Black Mode**: Replace surface/background with `Color.Black` for AMOLED screens
4. **Fallback**: Static color scheme for older Android versions

### Material 3 Color Scheme Slots
| Slot | Purpose |
|------|---------|
| primary | Main brand color, key actions |
| onPrimary | Text/icons on primary |
| primaryContainer | Filled containers using primary |
| onPrimaryContainer | Text/icons on primary container |
| secondary | Supporting color |
| tertiary | Accent color for highlights |
| error | Error states |
| surface | Card/sheet backgrounds |
| onSurface | Primary text color |
| surfaceVariant | Alternate surface |
| onSurfaceVariant | Secondary text color |
| outline | Borders, dividers |
| background | Screen background |

---

## 2. Typography

### Standard Typography (System Default Font)
| Style | Size | Line Height | Letter Spacing | Weight |
|-------|------|-------------|----------------|--------|
| displayLarge | 57sp | 64sp | -0.25sp | Normal |
| displayMedium | 45sp | 52sp | 0sp | Normal |
| displaySmall | 36sp | 44sp | 0sp | Normal |
| headlineLarge | 32sp | 40sp | 0sp | Normal |
| headlineMedium | 28sp | 36sp | 0sp | Normal |
| headlineSmall | 24sp | 32sp | 0sp | Normal |
| titleLarge | 22sp | 28sp | 0sp | Normal |
| titleMedium | 16sp | 24sp | 0.15sp | Medium |
| titleSmall | 14sp | 20sp | 0.1sp | Medium |
| bodyLarge | 16sp | 24sp | 0.5sp | Normal |
| bodyMedium | 14sp | 20sp | 0.25sp | Normal |
| bodySmall | 12sp | 16sp | 0.4sp | Normal |
| labelLarge | 14sp | 20sp | 0.1sp | Medium |
| labelMedium | 12sp | 16sp | 0.5sp | Medium |
| labelSmall | 11sp | 16sp | 0.5sp | Medium |

### Expressive Typography (Dasurv already uses this)
**Font**: Roboto Flex (Google Variable Font)

| Style | Standard | Expressive | Difference |
|-------|----------|------------|------------|
| displayLarge | 57sp | 64sp | +7sp |
| displayMedium | 45sp | 52sp | +7sp |
| displaySmall | 36sp | 44sp | +8sp |
| headlineLarge | 32sp | 40sp | +8sp |
| headlineMedium | 28sp | 36sp | +8sp |
| headlineSmall | 24sp | 32sp | +8sp |

---

## 3. Shapes & Corner Radii

### Standard Shapes
| Token | Radius | Use Case |
|-------|--------|----------|
| extraSmall | 4dp | Tags, small chips |
| small | 8dp | Cards, dialogs |
| medium | 12dp | Bottom sheets, large cards |
| large | 16dp | FABs, large containers |
| extraLarge | 28dp | Full screen sheets/panels |

### Expressive Shapes (Dasurv already uses these)
| Token | Radius | Use Case |
|-------|--------|----------|
| extraSmall | 12dp | Tags, small chips |
| small | 16dp | Cards, dialogs |
| medium | 24dp | Bottom sheets, large cards |
| large | 32dp | FABs, large containers |
| extraLarge | 48dp | Full screen sheets/panels |

### Custom Component Shapes
| Component | Shape | Value |
|-----------|-------|-------|
| Thumbnail | RoundedCorner | 6dp |
| Icon Buttons | Circle | CircleShape |

---

## 4. Dimensions & Spacing

### Navigation & Player
| Element | Height |
|---------|--------|
| Navigation Bar | 80dp |
| Slim Navigation Bar | 64dp |
| Mini Player | 64dp |
| Mini Player Bottom Spacing | 8dp |
| Queue Peek | 64dp |
| App Bar | 64dp |

### List & Grid Items
| Element | Size |
|---------|------|
| List Item Height | 64dp |
| Suggestion Item | 56dp |
| Search Filter | 48dp |
| List Thumbnail | 48dp |
| Small Grid Thumbnail | 104dp |
| Grid Thumbnail | 128dp |
| Album Thumbnail | 144dp |

### Common Spacing Scale
| Size | Value | Use |
|------|-------|-----|
| xs | 4dp | Tight gaps |
| sm | 8dp | Small spacing, icon gaps |
| md | 16dp | Standard padding |
| lg | 24dp | Section spacing, dialog padding |
| xl | 32dp | Player horizontal padding |
| xxl | 48dp | Minimum touch target |

### Touch Targets
- **Minimum interactive size**: 48dp x 48dp
- **Ripple radius**: 24dp (unbounded for icon buttons)

---

## Quick Reference: What to Adopt

### Already Matching
- Expressive shapes (12dp, 16dp, 24dp, 32dp, 48dp)
- Expressive typography sizes
- Material3 + Dynamic Color support
- Dark/Light theme support

### Recommended to Add
1. **Pure Black mode** for AMOLED screens
2. **Thumbnail corner radius** (6dp) for consistency
3. **Icon button pattern** (48dp, CircleShape, unbounded ripple)
4. **Spring animations** (StiffnessMediumLow) for navigation transitions
5. **Widget color system** with Android 12+ and fallback support
6. **Player gradient extraction** from album artwork
7. **Slider style options** (Default, Wavy, Slim)
8. **Disabled state alpha** (0.5f pattern)
9. **Slim nav bar option** (64dp vs 80dp)
10. **Roboto Flex font** for expressive typography
