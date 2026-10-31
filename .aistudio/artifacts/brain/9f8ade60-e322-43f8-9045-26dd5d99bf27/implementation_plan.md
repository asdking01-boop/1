# NOVA IPTV — Android Streaming Application

A complete, high-performance Android IPTV streaming client inspired by the Netflix user experience, built with Kotlin, Jetpack Compose, AndroidX Media3 ExoPlayer, and Room Database. Designed with native dual-language support (Arabic RTL default and English LTR), dark cinematic aesthetics, and real M3U / Xtream Codes API stream integration.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural decisions were aligned based on your preferences:

- **First-Time Launch Experience**: Prompt immediately with a high-fidelity IPTV Setup Wizard modal (M3U URL or Xtream Codes input) while offering an instant "Explore Demo Showcase" option featuring verified legal open-access HLS/VOD test streams (Big Buck Bunny, Tears of Steel, Sintel, and public test channels).
- **Live TV Viewing Mode**: Direct full-screen cinema player with a swipeable translucent channel guide overlay, real-time quality detection (SD / HD / FHD / 4K badges), and fast channel switching.
- **Home Screen Presentation**: Modern compact grid view with quick category chips, featured wide cinematic hero billboard with action buttons (Play Now, Details), "Continue Watching" progress rail, and trending carousels.
- **Bilingual & Layout Mirroring**: Arabic as the first-launch default with complete Right-to-Left (RTL) layout switching and localized strings; seamless one-tap toggle to English Left-to-Right (LTR) without app restart.

---

## 1. Overview & Core Concept

**NOVA IPTV** delivers a Netflix-grade streaming interface for live channels, movies, and episodic series on modern Android devices (optimized for flagship displays like Samsung Galaxy S26 Ultra). Users can connect their own IPTV subscriptions via standard M3U/M3U8 playlists or Xtream Codes API credentials, automatically organizing thousands of streams into categorized channels, movies with metadata, and structured multi-season series.

### Target Persona
Android users seeking an intuitive, ad-free, high-performance IPTV player with smart playback resumption, multi-track audio and subtitle selection, customizable aspect ratios, and full Arabic-first accessibility.

### Key Value Pillars
- **Zero Mock Fallbacks**: Real M3U parser (handling `#EXTINF`, tvg-id, tvg-logo, group-title) and real Xtream Codes client (`player_api.php` categories, live streams, VOD, and series info).
- **Pro Media3 Player**: Hardware-accelerated ExoPlayer with HLS adaptive bitrate, real track/resolution inspection, PiP (Picture-in-Picture), aspect ratio switching (Fit, Fill, Stretch, Zoom, 16:9), and playback speed control.
- **Persistent Offline Store**: Local Room SQLite database managing multiple IPTV accounts, favorites, watch history with exact millisecond positions, and cached category listings.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Initial Onboarding & Connection**:
   - First launch displays the **Setup Wizard** sheet: Choice of **Xtream Codes** (Server URL, Username, Password) or **M3U Playlist** (URL or M3U8 file), alongside a prominent **"Try Demo Showcase"** button.
   - Sync progress indicator showing channel and VOD parsing with category indexing.
2. **Browsing Content**:
   - **Home Screen**: Featured hero banner with dynamic backdrop, category chips (Trending, Action, Drama, News, Sports), "Continue Watching" rail with red progress bars, and compact 3-column media cards.
   - **Live TV**: Categorized channel browser with channel logos, live program badges, quality tags (FHD, 4K, 720p), and fast search filter.
   - **Movies & Series**: Grid catalog with duration, rating, year, genre filters, and deep-dive detail sheets (cast, synopsis, seasons/episodes picker, auto-next episode toggle).
   - **Favorites & History**: Dedicated quick-access screens with single-tap removal and resume capabilities.
3. **Advanced Playback**:
   - Tap any title or channel to enter the **Cinema Player**. Tap screen to reveal translucent OSD (On-Screen Display): Title, stream resolution badge, scrub bar, audio/subtitle selectors, quality switcher (Auto, 1080p, 720p, etc.), aspect ratio toggle, lock controls, and Live TV channel drawer.

### Visual Identity & Theme
- **Color Tokens**:
  - `Background`: Deep Charcoal / Pure Obsidian (`#0A0A0C` and `#121216`)
  - `Surface / Cards`: Translucent elevation cards (`#1B1B22` with subtle 1dp borders `#2A2A36`)
  - `Primary Accent`: Netflix Crimson Red (`#E50914`) with glowing focus highlights
  - `Secondary Accent`: Warm Amber (`#FFB800`) for ratings and live badges
  - `Text Hierarchy`: Crisp White (`#FFFFFF`) titles, Muted Smoke (`#A0A0AB`) metadata
- **Typography & Rhythm**:
  - Prominent bold display typography for hero banners and category headers.
  - Generous 12dp–16dp rounded corners for cards and modal sheets.
  - Strict 48dp touch targets for all interactive player controls.
- **RTL & LTR Localization**:
  - Full bidirectional mirroring using `CompositionLocalProvider(LocalLayoutDirection provides layoutDirection)`.
  - Comprehensive `strings.xml` for Arabic (`values-ar/strings.xml` & programmatic language switcher) and English (`values/strings.xml`).

---

## 3. Key Product Decisions & Trade-Offs

| Decision | Chosen Approach | Rationale & Trade-Offs |
| :--- | :--- | :--- |
| **Video Playback Engine** | AndroidX Media3 ExoPlayer (`1.5.1` / `1.4.1`) with HLS extension | Industry-standard Android streaming engine; hardware decoding, low latency HLS, subtitle track switching, and true resolution detection via `Format.width/height`. |
| **Data Persistence** | Android Room Database (Local SQLite) | Instant local load times, zero-latency favorite toggling, offline playlist cache, and secure credential storage on-device without remote auth dependencies. |
| **Playlist Parsing** | Streaming line-by-line M3U parser + Moshi-backed Xtream Codes HTTP client | Low-memory footprint when handling large playlists (10,000+ entries) without locking the UI thread. |
| **Language & RTL Switching** | In-app dynamic locale manager + Compose `LocalLayoutDirection` | Allows instant switching between Arabic and English from Settings without restarting the application or requiring activity re-creation glitches. |
| **Demo Content** | Curated legal HLS/MP4 streams (Blender Studio open films, public HLS test channels) | Guarantees an immediate working experience out of the box when the user has not yet entered private IPTV credentials. |

---

## 4. Technical Architecture & Data Strategy

### System Component Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                          NOVA IPTV Application                         │
├────────────────────────────────────────────────────────────────────────┤
│                               UI LAYER                                 │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐  │
│  │ Home Screen  │ │ Live TV View │ │ Movies/Series│ │ Player Screen│  │
│  │ (Hero/Grid)  │ │ (Channel OSD)│ │ (Detail/Ep)  │ │ (Media3 Exo) │  │
│  └──────┬───────┘ └──────┬───────┘ └──────┬───────┘ └──────┬───────┘  │
│         │                │                │                │          │
│         └────────────────┼────────────────┼────────────────┘          │
│                          ▼                ▼                           │
│               ┌──────────────────────────────────────┐                │
│               │             IptvViewModel            │                │
│               │ (StateFlow: channels, movies, state) │                │
│               └──────────────────┬───────────────────┘                │
├──────────────────────────────────┼─────────────────────────────────────┤
│                           DOMAIN & DATA                                │
│                                  ▼                                     │
│               ┌──────────────────────────────────────┐                │
│               │            IptvRepository            │                │
│               └───────┬──────────────────────┬───────┘                │
│                       ▼                      ▼                        │
│         ┌──────────────────────────┐   ┌──────────────────────────┐   │
│         │      Room Database       │   │   Network / Parsers      │   │
│         │ - IptvAccountEntity      │   │ - M3uPlaylistParser      │   │
│         │ - PlaylistItemEntity     │   │ - XtreamCodesApiClient   │   │
│         │ - WatchHistoryEntity     │   │ - OkHttp Client (Media)  │   │
│         │ - FavoriteEntity         │   │                          │   │
│         └──────────────────────────┘   └──────────────────────────┘   │
└────────────────────────────────────────────────────────────────────────┘
```

### Core Entities & State

1. **`IptvAccount`**: ID, account name, type (`M3U` or `XTREAM`), server URL, username, password, last sync timestamp, isActive.
2. **`MediaItem`**: ID, stream ID/URL, title, logo/poster URL, category, type (`LIVE`, `MOVIE`, `SERIES`), resolution (`SD`, `HD`, `FHD`, `4K`), stream format (`hls`, `ts`, `mp4`), metadata (duration, release year, overview, rating).
3. **`SeriesEpisode`**: Episode ID, series ID, season number, episode number, title, stream URL, duration, thumbnail.
4. **`WatchHistory`**: Stream ID, title, poster URL, playback position (ms), total duration (ms), last watched timestamp.
5. **`UserPreference`**: App language (`ar` / `en`), default video quality, auto-play next episode, preferred aspect ratio (`FIT`, `FILL`, `ZOOM`, `STRETCH`, `ORIGINAL`).

### Interactive Component & State Mapping

- **IPTV Setup Wizard**: Validates inputs, downloads playlist asynchronously with a progress percentage bar, saves account to Room, and updates the active catalog.
- **Channel OSD Drawer**: Gestural horizontal swipe from the right (Arabic) or left (English) inside the full-screen player to switch channels with instant buffer preview without leaving the player.
- **Aspect Ratio Switcher**: Cycles through `AspectRatioFrameLayout.RESIZE_MODE_FIT`, `RESIZE_MODE_ZOOM`, `RESIZE_MODE_FILL`, and `RESIZE_MODE_FIXED_WIDTH`.
- **Quality Track Selector**: Queries `ExoPlayer.currentTracks`, extracts available video tracks, renders human-readable labels (`1080p`, `720p`, `480p`, `Auto`), and sets `TrackSelectionParameters`.
