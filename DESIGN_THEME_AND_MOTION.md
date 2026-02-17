# Design Reference - Theme & Motion
> Theme Architecture, Icons, Animations, Gradients, Settings, Widgets

---

## 1. Theme Architecture

### Theme Function Signature
```kotlin
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    pureBlack: Boolean = false,           // AMOLED black
    themeColor: Color = DefaultThemeColor, // Seed color
    enableDynamicTheme: Boolean = true,    // Material You
    expressive: Boolean = false,           // Expressive M3
    content: @Composable () -> Unit,
)
```

### Dark Mode Options
| Mode | Behavior |
|------|----------|
| ON | Always dark |
| OFF | Always light |
| AUTO | Follow system setting |

### Pure Black Mode
When enabled, replaces:
- `surface` -> `Color.Black`
- `background` -> `Color.Black`
- Ideal for AMOLED displays to save battery

### Color Scheme Generation Priority
1. **System Dynamic Colors** (Android 12+, when default theme): Material You wallpaper colors
2. **MaterialKolor** (custom accent): Seed color with `TonalSpot` palette style
3. **Pure Black Override**: Modifies surface/background to `Color.Black`
4. **Static Fallback**: Hardcoded color scheme for older devices

---

## 2. Icon Strategy

### Icon Types
| Context | Style | Format |
|---------|-------|--------|
| Navigation (unselected) | Outlined | Vector drawable XML |
| Navigation (selected) | Filled | Vector drawable XML |
| Actions | Outlined | Vector drawable XML |
| Media controls | Filled | Vector drawable XML |

### Icon Naming Convention
```
home_outlined.xml           // Navigation unselected
home_filled.xml             // Navigation selected
play.xml                    // Media action
skip_next.xml               // Media action
skip_previous.xml           // Media action
pause.xml                   // Media action
library_music_outlined.xml  // Navigation
library_music_filled.xml    // Navigation
explore_outlined.xml        // Navigation
explore_filled.xml          // Navigation
```

### Icon Tinting
- Use `ColorFilter.tint(MaterialTheme.colorScheme.onSurface)` for dynamic theme support
- Never hardcode icon colors
- Media notification icons: defined in `res/values/drawables.xml`

### Launcher Icon
- **Adaptive icon**: Foreground + Background layers
- **Monochrome**: Single-tone variant for Android 13+
- **Background color**: `#2D184C` (dark purple)

---

## 3. Animations & Motion

### Spring Animations
| Animation | Stiffness | Use |
|-----------|-----------|-----|
| Navigation bar | `StiffnessMediumLow` | Nav show/hide |
| Bottom sheet | `StiffnessMediumLow` | Sheet open/close |
| Soft motion | `StiffnessLow` | Gentle transitions |
| Swipe return | `DampingRatioNoBouncy` | Snap back |

### Tween Animations
| Animation | Duration | Easing | Use |
|-----------|----------|--------|-----|
| Search expand | 300ms | Default | Search bar morph |
| Dropdown rotate | 400ms | Default | Icon rotation |
| Shimmer | 800ms | `LinearEasing` | Loading skeleton |
| Fade in/out | 500ms | Default | Playing indicator |

### Fade Effects
- **Background overlay**: Alpha 0.1f to 0.61f during 10%-61% progress
- **Disabled state**: Alpha 0.5f for disabled, 1.0f for enabled
- **Active overlay**: 0.6f alpha black for playing state

### Motion Best Practices
- Use `spring<Dp>()` for position animations (no fixed duration, physics-based)
- Use `animateFloatAsState` for alpha/scale transitions
- Velocity-based fling for draggable components (threshold: +/-250)
- Use `AnimatedVisibility` with `fadeIn`/`fadeOut` for show/hide
- Use `slideInVertically`/`slideOutVertically` for FAB visibility
- Use `AnimatedContent` for content transitions (e.g., chip changes)

---

## 4. Gradients & Dynamic Colors

### Player Background Gradient (3-color)
```
Color 1: Primary vibrant color (extracted from album art)
Color 2: Darker version (60% of primary RGB channels)
Color 3: Black (#000000)
```

### Color Extraction Pipeline
1. Extract dominant color from album art bitmap
2. Convert to HSV color space
3. Enhance saturation: multiply by 1.4x (clamp to 1.0)
4. Adjust brightness: multiply by 0.9x, clamp to 0.4-0.85 range
5. Convert back to RGB

### Player Background Modes
| Mode | Description |
|------|-------------|
| GRADIENT | 3-color gradient from album art |
| BLUR | Blurred album art (Android 12+ only) |
| DEFAULT | Solid theme color |
| APPLE_MUSIC | Apple Music-style layout |

### Player Button Styles
| Style | Description |
|-------|-------------|
| DEFAULT | Standard button coloring |
| SECONDARY | Secondary color variant |
| TERTIARY | Tertiary color variant |

---

## 5. Settings/Appearance Preferences

### Theme Preferences
| Preference | Key | Type | Values |
|------------|-----|------|--------|
| Dark Mode | `darkMode` | String | ON, OFF, AUTO |
| Pure Black | `pureBlack` | Boolean | true/false |
| Dynamic Theme | `dynamicTheme` | Boolean | true/false |
| Accent Color | `accentColor` | Int | Color int value |
| Expressive M3 | `material3_expressive` | Boolean | true/false |

### Player Preferences
| Preference | Key | Type | Values |
|------------|-----|------|--------|
| Player BG Style | `playerBackgroundStyle` | String | GRADIENT, DEFAULT, BLUR, APPLE_MUSIC |
| Player Buttons | `player_buttons_style` | String | DEFAULT, SECONDARY, TERTIARY |
| Slider Style | `sliderStyle` | String | DEFAULT, WAVY, SLIM |
| Mini Player Gradient | `mini_player_gradient` | Boolean | true/false |
| Rotating Thumbnail | `rotating_thumbnail` | Boolean | true/false |

### UI Preferences
| Preference | Key | Type | Values |
|------------|-----|------|--------|
| High Refresh Rate | `high_refresh_rate` | Boolean | true/false |
| Slim Nav Bar | `slimNavBar` | Boolean | true/false |
| Grid Item Size | `gridItemSize` | String | Size enum |
| Shape Color Tertiary | `settings_shape_color_tertiary` | Boolean | true/false |

### Lyrics Preferences
| Preference | Key | Type | Default |
|------------|-----|------|---------|
| Text Size | `lyricsTextSize` | Float | 28f |
| Line Spacing | `lyricsLineSpacing` | Float | 6f |
| Apple Music Blur | `apple_music_lyrics_blur` | Boolean | false |

---

## 6. Widget Design

### Widget Color System
- **Android 12+**: Uses `@android:color/system_accent1_*` system colors
- **Pre-Android 12**: Falls back to static color palette
- **Light/Dark variants**: via `values/` and `values-night/` resource qualifiers

### Widget Color Tokens
| Token | Light | Dark |
|-------|-------|------|
| Background | `#FFFFFF` | `#1F1F1F` |
| Primary Text | `#000000` | `#FFFFFF` |
| Secondary Text | `#757575` | 70% White |
| Accent | System or `#B8D45C` | System or `#B8D45C` |

### Expressive Widget Colors
| Token | Value | Purpose |
|-------|-------|---------|
| Tertiary Container | `#3E4A26` | Background |
| On Tertiary Container | `#E8E4D0` | Text |
| Play Button | `#B8D45C` | Play button bg |
| On Play Button | `#1C1E14` | Play button icon |
| Surface Variant | `#C2C9BD` | Dimmed elements |

### System Color References (Android 12+)
```xml
@android:color/system_accent1_100 through _700
@android:color/system_accent2_100 through _900
@android:color/system_neutral1_100, _400, _900
```

---

## 7. Key Source File Locations (in vivi-music)

### Core Theme Files
| File | Path |
|------|------|
| Theme.kt | `ui/theme/Theme.kt` |
| Type.kt | `ui/theme/Type.kt` |
| Shapes.kt | `ui/theme/Shapes.kt` |
| PlayerColorExtractor.kt | `ui/theme/PlayerColorExtractor.kt` |
| PlayerSliderColors.kt | `ui/theme/PlayerSliderColors.kt` |

### Component Files
| File | Path |
|------|------|
| Items.kt | `ui/component/Items.kt` (1566 lines) |
| IconButton.kt | `ui/component/IconButton.kt` |
| SearchBar.kt | `ui/component/SearchBar.kt` |
| Dialog.kt | `ui/component/Dialog.kt` |
| BottomSheet.kt | `ui/component/BottomSheet.kt` |
| BottomSheetMenu.kt | `ui/component/BottomSheetMenu.kt` |
| ChipsRow.kt | `ui/component/ChipsRow.kt` |
| MiniPlayer.kt | `ui/component/MiniPlayer.kt` |
| Preference.kt | `ui/component/Preference.kt` |
| SortHeader.kt | `ui/component/SortHeader.kt` |
| LyricsLine.kt | `ui/component/LyricsLine.kt` |

### Constants & Settings
| File | Path |
|------|------|
| Dimensions.kt | `constants/Dimensions.kt` |
| PreferenceKeys.kt | `constants/PreferenceKeys.kt` |
| AppearanceSettings.kt | `ui/screens/settings/AppearanceSettings.kt` |

### Resource Files
| File | Path |
|------|------|
| colors.xml (light) | `res/values/colors.xml` |
| colors.xml (dark) | `res/values-night/colors.xml` |
| widget_colors.xml | `res/values/widget_colors.xml` |
| widget_expressive_colors.xml | `res/values/widget_expressive_colors.xml` |
