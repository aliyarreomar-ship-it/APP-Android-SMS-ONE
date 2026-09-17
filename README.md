# SMS Reader Pro (Xisaabiye Pro V2)

Private personal web application to parse, track, and manage Somali mobile-money SMS transactions (EVC Plus, E-Dahab, Jeeb Premier, ZAAD, Sahal).

Ported from Android (Kotlin/Room/Jetpack Compose) to modern Web (React, TypeScript, Vite, Tailwind CSS).

## Features

- **Multi-Provider SMS Engine**: Regex & heuristic parsing tailored for Somali mobile-money networks:
  - Hormuud EVC Plus (192)
  - Telesom ZAAD
  - Golis Sahal
  - Somtel E-Dahab (898)
  - Premier Bank Jeeb
  - Generic fallback parser
- **Automatic Transaction Categorization**: Default categories (Bajaaj, Cunto, Dukaan, Kiro, Koronto, Biyo, Gas, TV, Nadaafad, Internet, Shaah) plus custom tags.
- **Smart Verification & Needs Review (Dib-u-eegis)**: Review newly captured or ambiguous transactions before committing to financial records.
- **Visual Analytics & Breakdowns**: Visual charts for income vs. expenses, category proportions, and provider volume with Recharts.
- **Consolidated Monthly Reports**: Filter records by month, view net savings, and export clean CSV ledgers.
- **Live SMS Scanner & Simulator**: Paste raw SMS text (single or batch) with immediate parsing preview and 1-click import.
- **Privacy & Local Persistence**: Data persists securely within client-side storage (`localStorage`) without sending financial records to external servers.
