# Design Reference - Core Components
> List Items, Grid Items, Thumbnails, Buttons, Search Bar

---

## 1. List Items

### Generic ListItem
```kotlin
@Composable
fun ListItem(
    title: String,
    subtitle: (@Composable RowScope.() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    thumbnailContent: @Composable () -> Unit = {},
    trailingContent: @Composable RowScope.() -> Unit = {},
    isActive: Boolean = false,
    drawHighlight: Boolean = true
)
```
| Property | Value |
|----------|-------|
| Height | 64dp (`ListItemHeight`) |
| Horizontal padding | 8dp |
| Active background | `RoundedCornerShape(8.dp)` + `secondaryContainer` color |
| Title | 14sp, Bold |
| Subtitle | Below title, secondary color |

### SongListItem
- Inherits ListItem layout
- **Swipe gesture**: Left = add to queue (primary), Right = play next (secondary)
- **Badges**: Liked icon, Explicit tag, In Library, Download status
- **Selection mode**: RoundedCheckbox overlay on thumbnail
- **Active state**: Playing indicator overlay

### ArtistListItem
- **Thumbnail shape**: `CircleShape` (circular avatar)
- **Subtitle**: Pluralized song count
- **Thumbnail size**: 48dp (`ListThumbnailSize`)

### AlbumListItem
- **Thumbnail shape**: `RoundedCornerShape` (square with corners)
- **Subtitle**: Artists + song count + year
- **Play button**: Overlay on hover/focus

### PlaylistListItem
- **Thumbnail**: Composite layout (0, 1, or 4 images in 2x2 grid)
- **Placeholder**: Centered icon when no images
- **Subtitle**: Song count

---

## 2. Grid Items

### Generic GridItem
```kotlin
@Composable
fun GridItem(
    title: @Composable () -> Unit,
    subtitle: @Composable () -> Unit,
    badges: @Composable RowScope.() -> Unit = {},
    thumbnailContent: @Composable BoxWithConstraintsScope.() -> Unit,
    thumbnailRatio: Float = 1f,
    fillMaxWidth: Boolean = false
)
```
| Property | Value |
|----------|-------|
| Padding | 12dp |
| Width | `GridThumbnailHeight * thumbnailRatio` (128dp default) |
| Title style | `bodyLarge`, Bold, marquee scroll |
| Subtitle style | `bodyMedium`, secondary color |

### SongGridItem
- **Title**: Marquee animation with `basicMarquee()`
- **Subtitle**: 2 lines - artist name + duration
- **Overlay**: Play button when not active

### ArtistGridItem
- **Thumbnail**: `CircleShape`
- **Subtitle**: Subscriber/song count

---

## 3. Thumbnails

### ItemThumbnail (Remote images)
```kotlin
@Composable
fun ItemThumbnail(
    thumbnailUrl: String?,
    isActive: Boolean,
    isPlaying: Boolean,
    shape: Shape,
    albumIndex: Int? = null,
    isSelected: Boolean = false,
    thumbnailRatio: Float = 1f
)
```
| Property | Value |
|----------|-------|
| Corner radius | 6dp (`ThumbnailCornerRadius`) |
| Active overlay alpha | 0.6f (`ActiveBoxAlpha`) dark overlay |
| Album index | Centered text overlay |
| Image loading | AsyncImage with cache |

### PlaylistThumbnail (Composite)
| Image Count | Layout |
|-------------|--------|
| 0 | Centered placeholder icon |
| 1 | Single full-size image |
| 4+ | 2x2 grid of images |

### Overlay Buttons on Thumbnails
| Button | Size | Position | Background |
|--------|------|----------|------------|
| OverlayPlayButton | 36dp circle | Center | Black @ 0.6f alpha |
| AlbumPlayButton | 36dp circle | BottomEnd | Black @ 0.6f alpha |
| OverlayEditButton | 36dp circle | Center (configurable) | Black @ 0.6f alpha |
| Icon size inside | 20dp | - | White |

---

## 4. Buttons

### ResizableIconButton
```kotlin
@Composable
fun ResizableIconButton(
    @DrawableRes icon: Int,
    color: Color = MaterialTheme.colorScheme.onSurface,
    enabled: Boolean = true,
    indication: Indication? = null,
    onClick: () -> Unit = {}
)
```
| Property | Value |
|----------|-------|
| Color | `ColorFilter.tint(color)` |
| Ripple | Unbounded |
| Enabled alpha | 1.0f |
| Disabled alpha | 0.5f |

### IconButton (with long press)
```kotlin
@Composable
fun IconButton(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors()
)
```
| Property | Value |
|----------|-------|
| Min size | 48dp x 48dp |
| Shape | `CircleShape` |
| Ripple radius | 24dp |
| Supports | Click + Long click combined |

### FAB (Floating Action Button)

> Dasurv Standard: Squarish with rounded corners, low elevation. NOT circular.

#### Standard FAB (icon only)
```kotlin
FloatingActionButton(
    onClick = { /* action */ },
    shape = RoundedCornerShape(16.dp),
    elevation = FloatingActionButtonDefaults.elevation(
        defaultElevation = 2.dp,
        pressedElevation = 4.dp
    )
) {
    Icon(Icons.Default.Add, "Add")
}
```

#### Extended FAB (icon + text)
```kotlin
ExtendedFloatingActionButton(
    onClick = { /* action */ },
    shape = RoundedCornerShape(16.dp),
    elevation = FloatingActionButtonDefaults.elevation(
        defaultElevation = 2.dp,
        pressedElevation = 4.dp
    )
) {
    Text("Label")
}
```

| Property | Value |
|----------|-------|
| Shape | `RoundedCornerShape(16.dp)` (NOT circle) |
| Default elevation | 2dp (low shadow) |
| Pressed elevation | 4dp |
| Position | `Alignment.BottomEnd` |
| Padding | 16dp from edges |
| Scroll behavior | Hide on scroll down, show on scroll up |
| Show animation | `slideInVertically` (from full height) |
| Hide animation | `slideOutVertically` (to full height) |

---

## 5. Search Bar

```kotlin
@Composable
fun TopSearch(
    query: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit,
    onSearch: (String) -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    shape: Shape = SearchBarDefaults.inputFieldShape,
    colors: SearchBarColors = SearchBarDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    )
)
```
| Property | Value |
|----------|-------|
| Input height | 48dp |
| Corner radius | 24dp (half of height) |
| Vertical padding | 8dp |
| Horizontal padding | 12dp |
| Icon offset | 4dp |
| Expand animation | 300ms tween |
| Background | `surfaceContainerLow` |
| Active state | Shape morphs to full-screen, divider appears with alpha fade |

---

## 6. Styling Patterns Summary

### Color Usage
- Primary container actions: `primaryContainer` + `onPrimaryContainer`
- Secondary actions: `secondary` + `onSecondary`
- Surfaces: `surface`, `surfaceContainer`, `surfaceVariant`
- Text: `onSurface` (primary), `onSurfaceVariant` (secondary)
- Errors: `error` color

### Corner Conventions
- List item active state: `RoundedCornerShape(8.dp)`
- Larger elements: `RoundedCornerShape(16.dp)`
- Buttons/pills: `CircleShape` or `RoundedCornerShape(16.dp)`
- Thumbnails: `RoundedCornerShape(6.dp)` or `CircleShape` (artists)

### Spacing Conventions
- Horizontal padding: 8dp (list items), 12dp (cards/grid), 16dp (preferences)
- Vertical padding: 6-8dp (density), 16dp (settings)
- Icon spacing: 4-12dp gaps
- Chip spacing: 8dp between

### Typography Hierarchy
- Titles: `titleMedium` (16sp), `titleSmall` (14sp)
- Body: `bodyLarge` (16sp), `bodyMedium` (14sp)
- Labels: `labelLarge` (14sp), `labelMedium` (12sp)
- Small: `bodySmall` (12sp)
