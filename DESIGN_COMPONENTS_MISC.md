# Design Reference - Misc Components
> Preferences, Sort, Navigation, Loading States, Text, Lyrics, Checkbox

---

## 1. Preferences & Settings

### PreferenceEntry
```kotlin
@Composable
fun PreferenceEntry(
    title: @Composable () -> Unit,
    description: String? = null,
    icon: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    isEnabled: Boolean = true
)
```
| Property | Value |
|----------|-------|
| Padding | 16dp horizontal, 16dp vertical |
| Icon spacing | 12dp after icon |
| Title style | `titleMedium` |
| Description style | `titleSmall`, secondary color |
| Trailing spacing | 12dp before trailing |
| Enabled alpha | 1.0f |
| Disabled alpha | 0.5f |

### SwitchPreference
- Switch with check/close icons in thumb
- Clickable entire row to toggle

### Other Preference Types
| Type | Interaction |
|------|------------|
| ListPreference | Dialog with radio buttons |
| EnumListPreference | Dialog with enum options |
| EditTextPreference | Dialog with text input |
| SliderPreference | Inline slider adjustment |

---

## 2. Sort Header

```kotlin
@Composable
fun <T : Enum<T>> SortHeader(
    sortType: T,
    sortDescending: Boolean,
    onSortTypeChange: (T) -> Unit,
    onSortDescendingChange: (Boolean) -> Unit,
    sortTypeText: (T) -> Int
)
```
| Property | Value |
|----------|-------|
| Layout | `SplitButtonLayout` (leading + trailing) |
| Background | `primaryContainer` |
| Text color | `onPrimaryContainer` |
| Leading min width | 120dp |
| Dropdown | Menu with radio selection |
| Sort icon rotation | 180deg animation (asc/desc toggle) |

---

## 3. Navigation

### NavigationTile
| Property | Value |
|----------|-------|
| Button size | 56dp circle |
| Button shape | `CircleShape` |
| Background | `surfaceContainer` |
| Label style | `labelMedium` |
| Spacing | 4dp between icon and label |
| Padding | 6dp around tile |

### GridMenu
| Property | Value |
|----------|-------|
| Layout | `LazyVerticalGrid`, adaptive columns |
| Min column width | 120dp |
| Item height | 108dp |
| Item shape | `ShapeDefaults.Large` |
| Item padding | 12dp |
| Label style | `labelLarge`, 2-line max |

---

## 4. Loading & Empty States

### ShimmerHost (Skeleton Loading)
```kotlin
@Composable
fun ShimmerHost(
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit
)
```
| Property | Value |
|----------|-------|
| Animation | 800ms `LinearEasing` tween |
| Delay | 250ms |
| Blend mode | `BlendMode.DstIn` gradient overlay |
| Gradient | Alpha fade: black to transparent |
| Shader colors | 0.25f, 0.50f, 0.25f alpha |

**Usage**: Apply `.shimmer()` modifier to placeholder shapes.

### EmptyPlaceholder
```kotlin
@Composable
fun EmptyPlaceholder(
    @DrawableRes icon: Int,
    text: String,
    trailingContent: @Composable (() -> Unit)? = null
)
```
| Property | Value |
|----------|-------|
| Layout | Column, centered, full size |
| Padding | 12dp |
| Icon size | 64dp |
| Icon tint | `onBackground` |
| Text style | `bodyLarge` |
| Spacing | 12dp between icon and text |

### InstrumentalDots (Breathing Animation)
```kotlin
@Composable
fun InstrumentalDots(
    dotColor: Color = Color.White,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center
)
```
| Property | Value |
|----------|-------|
| Dot count | 3 |
| Dot size | 10dp |
| Dot spacing | 8dp |
| Animation cycle | 1200ms |
| Stagger delay | 200ms per dot |
| Scale range | 0.8 to 1.2 |
| Alpha range | 0.3 to 1.0 |
| Pattern | Sine wave breathing |

---

## 5. Text Components

### AutoResizeText
```kotlin
@Composable
fun AutoResizeText(
    text: String,
    fontSizeRange: FontSizeRange,
    maxLines: Int = Int.MAX_VALUE,
    style: TextStyle = LocalTextStyle.current
)
```
| Property | Value |
|----------|-------|
| Algorithm | Binary search for optimal font size |
| Step | Decrements by `step` until text fits |
| Rendering | `drawWithContent` for conditional display |

```kotlin
data class FontSizeRange(
    val min: TextUnit,
    val max: TextUnit,
    val step: TextUnit = 1.sp
)
```

---

## 6. Lyrics Display

### LyricsLine
```kotlin
@Composable
fun LyricsLine(
    entry: LyricsEntry,
    currentTime: Long,
    isActive: Boolean,
    distanceFromCurrent: Int,
    lyricsTextPosition: LyricsPosition,
    textColor: Color,
    textSize: Float,
    lineSpacing: Float,
    isWordForWord: Boolean,
    isScrolling: Boolean
)
```

### Visual Hierarchy by Distance
| Distance | Alpha | Scale |
|----------|-------|-------|
| Active (0) | 1.0f | 1.0f |
| Next (1) | 0.5f | 0.9f |
| 2 away | 0.25f | 0.8f |
| Far (3+) | 0.15f | 0.8f |

### Styling
| Property | Value |
|----------|-------|
| Shape | `RoundedCornerShape(16.dp)` |
| Selected bg | `primary` @ 0.2f alpha |
| Padding | 24dp horizontal, lineSpacing+4 vertical |
| Blur (inactive) | 6dp (Apple Music style) |
| Default text size | 28sp (range: 16-48sp) |
| Default line spacing | 6sp |

### Sync Modes
| Mode | Description |
|------|-------------|
| Word-for-Word | FlowRow with gradient fill karaoke effect |
| Letter-by-Letter | Character-level animation |
| Sentence | Simple line highlighting |

### Karaoke Gradient
- Hard-edged gradient from active color to inactive (0.3f alpha)
- Color stops calculated per-word progress

### Lyrics Settings
| Setting | Default | Range |
|---------|---------|-------|
| Text Size | 28sp | 16sp - 48sp |
| Line Spacing | 6sp | Customizable float |
| Position | LEFT | LEFT, CENTER, RIGHT |
| Vertical Position | CENTER | TOP, CENTER |
| Word-for-Word sync | false | Boolean toggle |
| Letter animation | false | Boolean toggle |

---

## 7. Checkbox

### RoundedCheckbox
```kotlin
@Composable
fun RoundedCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?
)
```
| Property | Value |
|----------|-------|
| Style | Custom stroke rendering |
| Stroke cap | `StrokeCap.Round` |
| Stroke join | `StrokeJoin.Round` |
| Width | `CheckboxDefaults.StrokeWidth` |
