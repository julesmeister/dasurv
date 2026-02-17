# Design Reference - Surface Components
> Dialogs, Bottom Sheets, Chips, Mini Player, Sliders, Swipe Actions

## 1. Dialogs

### DefaultDialog
```kotlin
@Composable
fun DefaultDialog(
    onDismiss: () -> Unit,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    buttons: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
)
```
| Property | Value |
|----------|-------|
| Outer padding | 24dp |
| Inner padding | 24dp |
| Background | `AlertDialogDefaults.containerColor` |
| Shape | `AlertDialogDefaults.shape` |
| Elevation | `AlertDialogDefaults.TonalElevation` |
| Title style | `headlineSmall` |
| Button style | `labelLarge`, FlowRow at end |
| Icon spacing | 16dp below icon |

### TextFieldDialog
| Property | Value |
|----------|-------|
| Auto-focus delay | 300ms |
| Single-line action | `ImeAction.Done` |
| Multi-line max | 10 lines |
| Validation | `isInputValid` callback |
| Supports | Single or multiple text fields |

### ListDialog
| Property | Value |
|----------|-------|
| Content | `LazyColumn` |
| Vertical padding | 24dp |
| Alignment | `CenterHorizontally` |

### ActionPromptDialog
| Property | Value |
|----------|-------|
| Body padding | 12dp |
| Buttons | Reset, Cancel, Confirm (Row, End aligned) |

---

## 2. Bottom Sheets

### Custom BottomSheet (State-driven)
```kotlin
@Composable
fun BottomSheet(
    state: BottomSheetState,
    background: @Composable (BoxScope.() -> Unit) = {},
    onDismiss: (() -> Unit)? = null,
    collapsedContent: @Composable BoxScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit
)
```
| Property | Value |
|----------|-------|
| Collapsed shape | `RoundedCornerShape(16.dp)` |
| Background alpha | `(1.4f * (progress - 0.1f)^0.5).coerceIn(0f, 1f)` |
| Content alpha | `((progress - 0.25f) * 4).coerceIn(0f, 1f)` |
| Collapsed content alpha | `1f - (progress * 4).coerceAtMost(1f)` |
| Fling velocity threshold | +/-250 |
| Anchors | dismissed(0), collapsed(1), expanded(2) |

### BottomSheetState
```kotlin
@Stable
class BottomSheetState(
    draggableState: DraggableState,
    collapsedBound: Dp
) : DraggableState by draggableState
```
**Properties**: `dismissedBound`, `expandedBound`, `collapsedBound`, `progress` (0-1f)
**States**: `isDismissed`, `isCollapsed`, `isExpanded`
**Methods**: `dismiss()`, `collapse()`, `expand()`

### BottomSheetMenu (Modal)
```kotlin
@Composable
fun BottomSheetMenu(
    state: MenuState,
    background: Color = MaterialTheme.colorScheme.surface
)
```
| Property | Value |
|----------|-------|
| Drag handle | 40dp width x 4dp height |
| Handle corners | 2dp |
| Handle padding | 12dp vertical |
| Content padding | 20dp horizontal |
| Background | `surface` color |

---

## 3. Chips & Filters

### Dasurv Standard: FilledTonalButton Filter Row
> This is the standard filter pattern used across Dasurv screens (pigment catalogue, equipment, etc.)

```kotlin
Row(
    modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
) {
    options.forEach { (value, label) ->
        val isSelected = currentFilter == value
        FilledTonalButton(
            onClick = { onFilterChange(value) },
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = if (isSelected)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(text = label, maxLines = 1)
        }
    }
}
// Count label below filters
Text(
    "${filteredItems.size} items",
    modifier = Modifier.padding(horizontal = 16.dp),
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant
)
```

| Property | Value |
|----------|-------|
| Component | `FilledTonalButton` |
| Scroll | `horizontalScroll` enabled |
| Row padding | 16dp horizontal, 8dp vertical |
| Button gap | 8dp (`Arrangement.spacedBy`) |
| Content padding | 16dp horizontal, 8dp vertical |
| Selected bg | `primaryContainer` |
| Selected text | `onPrimaryContainer` |
| Unselected bg | `surfaceContainerHigh` |
| Unselected text | `onSurfaceVariant` |
| Count label | `bodySmall`, `onSurfaceVariant`, 16dp horizontal padding |
| Text | `maxLines = 1` |

### ChipsRow (vivi-music reference)
| Property | Value |
|----------|-------|
| Chip shape | `RoundedCornerShape(16.dp)` |
| Border | None |
| Scroll | Horizontal scroll enabled |
| Start padding | 12dp |
| Chip gap | 8dp |
| Background | `surfaceContainer` |

### ChoiceChipsRow (vivi-music reference - Dropdown + Filter)
| Property | Value |
|----------|-------|
| Lead chip | `AssistChip` with dropdown icon |
| Icon rotation | 180deg, 400ms tween |
| Dropdown | `DropdownMenu` with radio selection |
| Secondary chips | `FilterChip` row |
| Transition | `AnimatedContent` for chip changes |

---

## 4. Mini Player

### NewMiniPlayer (Current Design)
| Property | Value |
|----------|-------|
| Height | 64dp (`MiniPlayerHeight`) |
| Horizontal padding | 12dp |
| Shape | `RoundedCornerShape(32.dp)` |
| Background | `surfaceContainer` |

**Layout (left to right):**
| Component | Size | Details |
|-----------|------|---------|
| Play/Pause | 48dp box | CircularProgressIndicator (3dp stroke) around 40dp circle with 1dp border |
| Song info | weight=1f | Title: 14sp Medium + marquee, Artists: 12sp 0.7f alpha + marquee |
| Audio device | 40dp | CircleShape, 1dp border, headphones/speaker icon |
| Favorite | 40dp | Gold border when liked, 20dp star icon |

**Swipe gesture:**
| Direction | Action | Background Color |
|-----------|--------|-----------------|
| Left | Next song | Green |
| Right | Previous song | Red |
| Animation | Spring (DampingRatioNoBouncy) | - |
| Threshold | Sigmoid function based on sensitivity | - |

### LegacyMiniPlayer
- LinearProgressIndicator at bottom (2dp height)
- Skip controls in a row

---

## 5. Sliders & Seek Bars

### Standard Slider Styles
| Style | Description |
|-------|-------------|
| DEFAULT | Standard Material3 slider |
| WAVY | Squiggly/wavy track line |
| SLIM | Thin custom track variant |

**Inactive track colors**:
- Default bg: `outline` (dark) / `onSurface` at 40% alpha (light)
- Gradient/Blur bg: White at 40% alpha

### BigSeekBar
| Property | Value |
|----------|-------|
| Height | 48dp |
| Shape | `RoundedCornerShape(16.dp)` |
| Background | `surfaceTint` @ 0.13f alpha |
| Active color | `primary` |
| Drag sensitivity | 1.2f horizontal |
| Rendering | Canvas-based |

### VolumeSlider
| Property | Value |
|----------|-------|
| Track height | 40dp |
| Handle height | 52dp |
| Handle width | 4dp |
| Track corners | 12dp |
| Icon size | 24dp |
| Icon padding | 10dp |
| Thumb gap | 6dp |
| Stop indicator | 4dp radius circles |
| Icons | Dynamic: off / mute / down / up |

### WavySlider
| Property | Value |
|----------|-------|
| Stroke width | 4dp |
| Thumb radius | 8dp |
| Wavelength | M3 default |
| Speed | Matches wavelength |
| Amplitude | Animated based on playback state |

---

## 6. Swipe Actions

### SwipeToSongBox
```kotlin
@Composable
fun SwipeToSongBox(
    mediaItem: MediaItem,
    content: @Composable BoxScope.() -> Unit
)
```
| Property | Value |
|----------|-------|
| Drag threshold | 300f |
| Left swipe | Add to queue (`primary` color bg) |
| Right swipe | Play next (`secondary` color bg) |
| Reset animation | 300ms |
| Feedback | Toast notification on complete |
| Icon visibility | Based on offset direction |

---

## 7. Playing Indicator (Audio Visualizer)

```kotlin
@Composable
fun PlayingIndicator(
    color: Color,
    bars: Int = 3,
    barWidth: Dp = 4.dp,
    cornerRadius: Dp = 6.dp
)
```
| Property | Value |
|----------|-------|
| Bar count | 3 |
| Bar width | 4dp |
| Bar spacing | 6dp |
| Bar height range | 0.1f to Random(0.1-1.0) |
| Animation interval | 50ms updates after 300ms initial delay |
| Corner style | RoundRect |
