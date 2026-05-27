# Stitch Exports - MinLish Vocabulary App Design

Project:

- Title: MinLish Vocabulary App Design
- ID: `2325080910252905506`
- Export source: Stitch MCP `get_project`, `list_design_systems`, `get_screen`

## Exported Screenshots

Screenshots are stored in `stitch_exports/screens/`:

- `onboarding_screen.png`
- `login_screen.png`
- `splash_screen.png`
- `register_screen.png`
- `home_dashboard.png`
- `deck_list.png`
- `deck_detail.png`
- `create_deck.png`
- `add_word_form.png`
- `flashcard_learning.png`
- `review_results.png`
- `progress_analytics.png`
- `profile_settings.png`

## Exported HTML Code

Stitch HTML exports are stored in `stitch_exports/code/`:

- `onboarding_screen.html`
- `login_screen.html`
- `splash_screen.html`
- `register_screen.html`
- `home_dashboard.html`
- `deck_list.html`
- `deck_detail.html`
- `create_deck.html`
- `add_word_form.html`
- `flashcard_learning.html`
- `review_results.html`
- `progress_analytics.html`
- `profile_settings.html`
- `design_system_tokens.md`

## Design System Note

The Design System item is a Stitch design-system asset, not a normal screen. Calling `get_screen` with the provided design-system ID returned `Requested entity was not found`.

Design tokens were successfully retrieved from `get_project` and `list_design_systems`, then summarized in `stitch_exports/code/design_system_tokens.md`.

## Implementation Note

The HTML is a visual/reference export only. The Android app should implement these screens as native Jetpack Compose UI, not WebView.
