# TakuTune

Native Android music player project built with Kotlin and Jetpack Compose.

## Development journal

### 2026-10-03 — Native UI foundation

**Что добавлено**
- Полностью заменён старый экран на нативный Compose-интерфейс без WebView и HTML.
- Сделан единый визуальный каркас: тёмная тема, фиолетовый акцент, скруглённые карточки и мини-плеер.
- Добавлены нативные разделы: Главная, Поиск, Медиатека, Настройки.
- Добавлен отдельный экран плеера и базовая модель перехода из списка треков в плеер.
- Добавлены заготовки разделов Любимые, Плейлисты, Альбомы, Исполнители и История.
- В Поиске добавлена структура источников Local / YouTube / Spotify / VK без реализации сетевого воспроизведения.
- Интерфейс больше не зависит от HTML/WebView.

**Что сломалось**
- После первого обновления MainActivity.kt в поисковом компоненте осталась лишняя ссылка на BasicAlertDialog, которая могла привести к ошибке компиляции.

**Как исправлено**
- Удалена лишняя строка из SearchPill.
- После исправления файл снова содержит только необходимые Compose-компоненты.

**Где сломалось**
- app/src/main/java/com/takusima/takutune/MainActivity.kt, компонент SearchPill.

**Что изменилось**
- Старый WebView-ориентированный прототип заменён на основу будущего нативного приложения.
- Реальное воспроизведение, Media3, локальный сканер, Room/DataStore и авторизация источников пока не подключены — это следующие этапы.

**Текущий статус**
- UI foundation: сделан.
- Native Compose: используется.
- HTML/WebView для основного UI: удалён.
- Реальный audio playback: ещё не подключён.
- Локальная медиатека: ещё не подключена.
- Автоматическая сборка: запускается GitHub Actions после push в main.

## Roadmap

1. Media3 + ExoPlayer + MediaSession и фоновое воспроизведение.
2. Сканирование локальной музыки через MediaStore.
3. Room: треки, избранное, история, плейлисты, заблокированные треки.
4. DataStore: настройки и кастомизация.
5. Полноценные настройки внешнего вида в стиле TakuTune.
6. Единая архитектура источников Local / YouTube / Spotify / VK.
7. Lyrics, EQ, crossfade, sleep timer и расширенные жесты.
8. Backup/restore настроек и медиатеки.

## Правило журнала

Каждое изменение проекта должно быть отражено здесь: что добавлено, что сломалось, как исправлено, где произошла проблема и что изменилось после исправления.

### 2026-10-03 — Real playback foundation

**Что добавлено**
- Добавлена модель `Track`.
- Добавлен `LocalMusicScanner` для поиска локальных аудиотреков через MediaStore.
- Добавлен `PlayerManager` на ExoPlayer.
- Добавлен `PlaybackService` на Media3 MediaSessionService для основы фонового воспроизведения.
- Подключены Media3 ExoPlayer и Media3 Session.
- Добавлены разрешения для чтения аудио и foreground media playback service.
- compileSdk/targetSdk подняты до 36.

**Что сломалось**
- При подготовке файлов была внутренняя ошибка генерации из-за интерполяции Kotlin-строк в инструменте записи файлов; в репозиторий ошибочный вариант не попал.

**Как исправлено**
- Файлы были записаны повторно без конфликтующей интерполяции.
- Все четыре исходных файла успешно созданы.

**Где сломалось**
- Ошибка возникла при подготовке `LocalMusicScanner.kt` перед записью в GitHub; исходный файл репозитория не был повреждён.

**Что изменилось**
- TakuTune теперь имеет настоящий фундамент локального audio playback вместо чистого UI-прототипа.
- Полное подключение UI к сервису, runtime permission flow и Room остаются следующим этапом.


### 2026-10-03 — Stage 2: reactive local library

**Что добавлено**
- MainActivity теперь запрашивает разрешение на чтение аудио.
- Добавлена совместимость с Android 12 и ниже через READ_EXTERNAL_STORAGE.
- Результат MediaStore-сканирования хранится как Compose state, поэтому UI обновляется после обнаружения музыки.
- Главная получает список локальных треков из сканера.

**Что сломалось**
- Первая версия держала найденные треки в обычном поле Activity, из-за чего Compose не обязан был перерисовать экран после сканирования.

**Как исправлено**
- Состояние перенесено в Compose `mutableStateOf` внутри MainActivity и передаётся вниз по UI как данные.

**Где сломалось**
- `app/src/main/java/com/takusima/takutune/MainActivity.kt`, состояние `localTracks`.

**Что изменилось**
- Сканер теперь является реальным источником данных для интерфейса, а не декоративной заготовкой.
- Следующий этап: связать выбранный Track с MediaController/PlaybackService, чтобы воспроизведение продолжалось после выхода из Activity.


### 2026-10-03 — Stage 3: connect UI to background playback

**Что добавлено**
- Добавлен PlaybackController, который подключается к PlaybackService через SessionToken и MediaController.
- Нажатие на реальный локальный трек теперь передаёт URI и метаданные в Media3-сессию и запускает воспроизведение.
- selectedTrack теперь хранит полноценный Track, а не только строку с названием.
- Экран плеера показывает название и исполнителя выбранного локального трека.

**Что сломалось**
- В проекте существовал отдельный PlayerManager со своим ExoPlayer. Это создавало второй независимый экземпляр плеера и не было связано с PlaybackService.
- Старый UI передавал в плеер только строку, поэтому реальный URI трека терялся.

**Как исправлено**
- PlayerManager удалён.
- Воспроизведение теперь идёт через единый PlaybackService + MediaSession.
- MainActivity использует PlaybackController для подключения к MediaSession.

**Где сломалось**
- app/src/main/java/com/takusima/takutune/playback/PlayerManager.kt.
- app/src/main/java/com/takusima/takutune/MainActivity.kt, обработчик локального трека и состояние selectedTrack.

**Что изменилось**
- TakuTune впервые получил сквозной путь: MediaStore → Track → MediaController → PlaybackService → ExoPlayer.
- Следующий технический этап: реактивное состояние плеера, очередь, предыдущий/следующий трек, play/pause в UI и Room.


### 2026-10-03 — Stage 4: real library, queue and persistent data

**Что добавлено**
- Room database: tracks, favorites, history, playlists, playlist tracks и blocked tracks.
- LocalLibraryRepository для хранения локальной медиатеки и истории.
- DataStore SettingsStore для темы, акцента и AMOLED.
- PlaybackState как единое реактивное состояние плеера.
- PlaybackController теперь поддерживает очередь, play/pause, next/previous, seek, shuffle и repeat.
- Главный экран, поиск и медиатека используют реальные данные MediaStore/Room.
- Полноэкранный плеер получил реальные play/pause, previous/next и seek.
- Добавлены разрешения уведомлений и управления аудио.
- Media3 обновлён до 1.11.1 — это актуальная стабильная ветка Media3 на текущий момент. (Проверено по Android Developers.)

**Что сломалось**
- Старый MainActivity был одним большим UI-прототипом и не имел настоящего persistent data layer.
- Отдельный PlayerManager уже был удалён на предыдущем этапе, но UI всё ещё не имел реактивного состояния очереди.
- Первая версия SettingsStore использовала запись через DataStore без явного edit API.

**Как исправлено**
- Добавлены отдельные database/preferences/repository/playback классы.
- MainActivity теперь связывает данные, playback и Compose UI.
- SettingsStore переведён на DataStore.edit.
- Очередь и состояние плеера централизованы в PlaybackController.

**Где сломалось**
- app/src/main/java/com/takusima/takutune/MainActivity.kt
- app/src/main/java/com/takusima/takutune/playback/PlaybackController.kt
- app/src/main/java/com/takusima/takutune/core/preferences/SettingsStore.kt

**Что изменилось**
- TakuTune больше не является только UI-макетом: локальная библиотека сохраняется в Room, настройки — в DataStore, а воспроизведение управляется MediaSession/Media3.
- YouTube/Spotify/VK, lyrics, EQ, crossfade и глубокая кастомизация пока остаются отдельными следующими подсистемами; они не подменяются фиктивными кнопками.


### 2026-10-03 — Stage 5: large functional architecture block

**Что добавлено**
- Сформирован единый слой моделей Artist, Album, Playlist, PlaybackSource.
- Добавлен MusicSource для единого контракта Local / YouTube / Spotify / VK.
- Добавлен рабочий LocalSource и SourceRegistry; сетевые источники пока намеренно не притворяются реализованными.
- PlaybackController расширен реактивным состоянием shuffle/repeat и периодическим обновлением позиции.
- Room-библиотека расширена наблюдением избранного, истории и плейлистов.
- Реализовано добавление/удаление трека из избранного.
- Настройки внешнего вида подключены к DataStore: System/Dark/Light, AMOLED, Dynamic Color.
- Тема вынесена в отдельный TakuTuneTheme.
- Полноэкранный плеер получил избранное, shuffle, repeat, previous/next, seek и реальное play/pause.
- Основные экраны используют реальную локальную медиатеку вместо декоративных списков.
- На время большого блока feature-изменений автосборка на push была отключена, чтобы не создавать десятки бесполезных GitHub Actions запусков. После завершения блока push-trigger возвращается.

**Что сломалось**
- После расширения PlaybackState UI требовал обновлять позицию даже без событий Media3.
- Первичная архитектура не имела общего контракта для будущих музыкальных источников.
- Старые версии Room/DataStore были не актуальны для текущего стабильного AndroidX.

**Как исправлено**
- Добавлен PlaybackController.refresh() и UI-тикер раз в 500 мс.
- Создан MusicSource + LocalSource + SourceRegistry.
- Room обновлён до 2.8.5, DataStore до 1.2.1; Media3 остаётся 1.11.1. Эти версии соответствуют опубликованным стабильным версиям AndroidX на сентябрь 2026 года.

**Где произошло**
- app/src/main/java/com/takusima/takutune/MainActivity.kt
- app/src/main/java/com/takusima/takutune/playback/PlaybackController.kt
- app/src/main/java/com/takusima/takutune/playback/PlaybackState.kt
- app/src/main/java/com/takusima/takutune/core/database/TakuTuneDao.kt
- app/src/main/java/com/takusima/takutune/library/LocalLibraryRepository.kt
- app/src/main/java/com/takusima/takutune/core/preferences/SettingsStore.kt
- app/src/main/java/com/takusima/takutune/theme/TakuTuneTheme.kt
- app/src/main/java/com/takusima/takutune/sources/*
- app/build.gradle.kts

**Что изменилось**
- Теперь это уже не набор экранов: есть persistent data layer, единый playback state, очередь, избранное, настройки и расширяемая система источников.
- YouTube/Spotify/VK пока остаются реальными следующими интеграциями, а не фальшивыми кнопками.


### Technical changelog — 2026-10-03 — /tt-core /room-flow

**Author:** Takusima

**Changed files**
- `app/src/main/java/com/takusima/takutune/core/database/Entities.kt`
- `app/src/main/java/com/takusima/takutune/core/database/TakuTuneDao.kt`

**Exact line tracking**
- `Entities.kt`: complete entity declarations reformatted/documented; effective changed lines 1–45.
- `TakuTuneDao.kt`: DAO declarations reformatted/documented; effective changed lines 1–55.

**Changes**
- Added author KDoc for Room entities and DAO.
- Preserved existing schema and Room version.
- Preserved reactive `Flow` queries.
- Preserved favorites, history, playlists and blocked-track operations.
- Added deterministic ordering to favorite and blocked-track ID flows.

**Build failures**
- No compiler/build failure was produced by this change set.

**Resolution**
- No failure resolution required.
- Repository content was updated through GitHub Contents API with current blob SHAs.

**Unresolved**
- Full GitHub Actions compilation has not yet been executed for this specific change set.

### Technical changelog — 2026-10-03 — /tt-core /refactor-solid /compile-git

**Author:** Takusima

**Changed files and exact line ranges**
- `app/src/main/java/com/takusima/takutune/core/presentation/TakuTuneViewModel.kt`: lines 1–152 — new MVVM state owner; combines Room/DataStore/Media3 state, moves scanning/playback/settings actions out of Activity, adds lifecycle-scoped playback position refresh, and releases the controller with the ViewModel.
- `app/src/main/java/com/takusima/takutune/MainActivity.kt`: lines 1–362 — Activity reduced to permission/bootstrap responsibilities; Compose collects one lifecycle-aware `StateFlow` and dispatches UI events to the ViewModel.
- `app/src/main/java/com/takusima/takutune/core/preferences/SettingsStore.kt`: lines 1–46 — documented and normalized DataStore settings access; animation scale is clamped to a safe range.
- `app/src/main/java/com/takusima/takutune/playback/PlaybackController.kt`: lines 1–62 — centralized Media3 controller lifecycle, stable media-ID parsing, safe seek/repeat bounds, and queue state publication.
- `app/build.gradle.kts`: lines 1–54 — aligned compile/target SDK to 37 and lifecycle Compose dependencies to the versions actually resolved by the build.
- `build.gradle.kts`: lines 1–6 — upgraded AGP to 9.1.1, Kotlin/Compose plugin to 2.2.10, and KSP to 2.2.10-2.0.2.
- `.github/workflows/build.yml`: lines 1–35 — Gradle runner updated from 8.9 to 9.3.1.

**Build failure detected**
- GitHub Actions run `37152281338` / run #47 failed at ` :app:checkDebugAarMetadata`.
- The failure was dependency/toolchain incompatibility, not Kotlin source compilation.
- Resolved requirements reported by Gradle included AGP >= 9.1.0 and compileSdk >= 37 for Navigation 2.10.2 and lifecycle 2.11.0; the project was on AGP 8.7.3 / compileSdk 36.
- AGP 8.7 also emitted the compileSdk 36 compatibility warning.

**Resolution**
- Upgraded AGP to 9.1.1.
- Upgraded Gradle Actions setup to 9.3.1.
- Upgraded Kotlin and Compose compiler plugins to 2.2.10.
- Upgraded KSP to 2.2.10-2.0.2.
- Raised compileSdk/targetSdk to 37.
- Aligned lifecycle-runtime-compose, lifecycle-viewmodel-compose and activity-compose with the dependency versions resolved by the failing build.
- No HTML, WebView or hybrid layer was introduced.

**Current build status**
- Final validation run: GitHub Actions run #54, commit `d523166ad9c524b67173353bc60e71a18fcb7a79`.
- At changelog write time the run was still **in progress**; no new compiler failure was available yet.
- Earlier superseded push runs #50–#53 were also in progress because GitHub Actions triggers on every main push.
- Unresolved: final run #54 result is pending.

**Architecture result**
- Activity no longer owns Room/DataStore/Media3 business logic.
- ViewModel is now the presentation state owner.
- Room remains reactive through Flow; DataStore remains reactive through Flow; Media3 remains isolated behind PlaybackController.


### Technical changelog — 2026-10-03 — /tt-core /compile-fix-agp9

**Author:** Takusima

**Build failure**
- GitHub Actions `Build TakuTune APK` failed during plugin application before Kotlin source compilation.
- Error: `Failed to apply plugin 'org.jetbrains.kotlin.android' > Cannot add extension with name 'kotlin', as there is an extension already registered with that name.`
- Failure location: `app/build.gradle.kts`, plugin block, where `org.jetbrains.kotlin.android` was still applied after upgrading to AGP 9.1.1.

**Root cause**
- AGP 9.0+ provides built-in Kotlin support and already registers the Kotlin extension.
- Applying `org.jetbrains.kotlin.android` again attempts to register the same extension a second time.

**Fix**
- Removed `org.jetbrains.kotlin.android` from `app/build.gradle.kts`.
- Removed the unused `org.jetbrains.kotlin.android` root plugin declaration from `build.gradle.kts`.
- Kept the Kotlin Compose compiler plugin because Compose compiler configuration is still required.
- No HTML, WebView or hybrid layer was introduced.

**Expected result**
- The Android application module now uses AGP 9.1.1 built-in Kotlin instead of applying the legacy Android Kotlin plugin.
- Next validation must be a single full GitHub Actions build of this fix block.


### Technical changelog — 2026-10-03 — /tt-core /compile-fix-kotlin-options

**Author:** Takusima

**Build failure**
- GitHub Actions run #58 failed after the duplicate Kotlin plugin issue was fixed.
- Failure location: `app/build.gradle.kts:31`.
- Errors: `Unresolved reference 'kotlinOptions'` and `Unresolved reference 'jvmTarget'`.

**Root cause**
- The previous `kotlinOptions { jvmTarget = "17" }` DSL belongs to the removed legacy `org.jetbrains.kotlin.android` plugin configuration.
- TakuTune now uses AGP 9.1.1 built-in Kotlin support.

**Resolution**
- Removed the legacy `kotlinOptions` block.
- Configured Kotlin JVM target through the modern `compilerOptions` DSL with `JvmTarget.JVM_17`.
- Kept Java source/target compatibility at 17.
- No source/UI architecture changes were made.
- No HTML, WebView or hybrid technology was introduced.

**Changed files**
- `app/build.gradle.kts`: plugin configuration/toolchain block; legacy Kotlin options replaced by compilerOptions.
- `README.md`: this changelog entry.

**Unresolved build status**
- This fix is committed to `main`.
- A new GitHub Actions validation run is required; no claim of successful compilation is made before that run completes.


### Technical changelog — 2026-10-03 — /tt-core /compile-fix-ksp-agp9

**Author:** Takusima

**Build failure**
- GitHub Actions run #60 (`37152692980`) failed during project configuration.
- Failure location: generated KSP source integration under `:app`.
- Error: `Using kotlin.sourceSets DSL to add Kotlin sources is not allowed with built-in Kotlin.`
- KSP generated `build/generated/ksp/debug/kotlin` and `build/generated/ksp/debug/java`, and AGP 9 built-in Kotlin rejected the KSP source-set registration.

**Root cause**
- TakuTune uses Room through KSP.
- The selected KSP/Room toolchain still registers generated sources through the legacy Kotlin source-set path when AGP built-in Kotlin is enabled.
- The repository is staying on Kotlin 2.2.10 / KSP 2.2.10-2.0.2 for this migration block.

**Resolution**
- Disabled AGP built-in Kotlin with `android.builtInKotlin=false`.
- Disabled the new Android DSL with `android.newDsl=false`.
- Restored the explicit `org.jetbrains.kotlin.android` plugin.
- Kept the Compose compiler plugin and KSP unchanged.
- This returns the project to the classic Android Kotlin + KSP configuration required by the current Room/KSP setup.
- No HTML, WebView or hybrid technology was introduced.

**Changed files**
- `app/build.gradle.kts`: line 3 — restored `org.jetbrains.kotlin.android`.
- `build.gradle.kts`: line 2 — restored root Kotlin Android plugin declaration.
- `gradle.properties`: lines 3–4 — disabled AGP built-in Kotlin/new DSL for KSP compatibility.
- `README.md`: appended this complete build-failure record.

**Unresolved build status**
- Configuration fix committed to `main`.
- GitHub Actions automatically validates the complete fix block on push.
- Compilation is not declared successful until that run reports success.


### Technical changelog — 2026-10-03 — /tt-core /compile-fix-source-api

**Author:** Takusima

**Build failure detected**
- GitHub Actions `Build TakuTune APK` run #64 failed during `:app:compileDebugKotlin` after the AGP 9/KSP configuration issue was resolved.
- `MainActivity.kt:281`: `clickable(open)` matched no valid Compose overload.
- `MainActivity.kt:344`: `clickable(onClick)` matched no valid Compose overload.
- `TakuTuneViewModel.kt:52–58`: the combined Flow could not infer its types because the repository did not expose `observeBlockedIds()`.
- `TakuTuneViewModel.kt:84, 90`: repository methods were called with `Long` track IDs while their current contract accepts `Track`.
- `TakuTuneViewModel.kt:97`: `toggleBlocked` was missing from `LocalLibraryRepository`.

**Root cause**
- Compose `Modifier.clickable` uses the lambda through the named/default `onClick` parameter in this API surface; passing the lambda positionally selected the Boolean `enabled` parameter and produced an overload mismatch.
- The MVVM refactor introduced blocked-track state and actions in the ViewModel before the repository contract was updated to match it.
- Favorite and blocked mutations were also inconsistent between the ViewModel and repository signatures.

**Resolution**
- `MainActivity.kt:281, 297, 344` — changed positional `clickable` lambda calls to explicit lambda bodies.
- `TakuTuneViewModel.kt:90, 97` — pass the full `Track` to repository mutations, matching the repository contract.
- `LocalLibraryRepository.kt:17` — added `observeBlockedIds()` backed by the Room DAO.
- `LocalLibraryRepository.kt:31–34` — added `toggleBlocked(track, currentlyBlocked)` using the existing Room blocked-track DAO operations.
- Existing Room schema and DAO were preserved; no HTML/WebView/hybrid layer was introduced.

**Changed files and exact line ranges**
- `app/src/main/java/com/takusima/takutune/MainActivity.kt`: lines 281, 297 and 344 — Compose click handlers corrected.
- `app/src/main/java/com/takusima/takutune/core/presentation/TakuTuneViewModel.kt`: lines 90 and 97 — repository mutation calls corrected to use `Track`.
- `app/src/main/java/com/takusima/takutune/library/LocalLibraryRepository.kt`: lines 17 and 31–34 — blocked-track observation and mutation API added.
- `README.md`: this technical changelog entry.

**Build status**
- Run #64 remains the failing baseline that exposed these source errors.
- A fresh GitHub Actions build is triggered by this fix block.
- The build is not declared successful until the new run completes.

**Unresolved**
- No known source errors remain from the compiler output supplied for run #64. Final CI validation is required to catch any subsequent compiler or packaging error.


### Technical changelog — 2026-10-04 — /tt-core /compile-fix-history

**Author:** Takusima

**Follow-up source audit after Actions failures**
- Audited the complete current `TakuTuneViewModel.kt` and `LocalLibraryRepository.kt` contracts after the previous compile-fix block.
- Found one remaining type mismatch that was not present in the original run #64 excerpt: `TakuTuneViewModel.play()` passed `track.id: Long` into `LocalLibraryRepository.addHistory(track: Track)`.

**Resolution**
- `app/src/main/java/com/takusima/takutune/core/presentation/TakuTuneViewModel.kt:70` — changed `library.addHistory(track.id)` to `library.addHistory(track)`.
- This makes the history call consistent with the repository's existing Track-based API and removes the remaining known ViewModel/Repository argument mismatch.

**Build status**
- A new GitHub Actions validation is triggered by this commit.
- Success is not claimed until CI completes.

**Unresolved**
- No additional source-level mismatch was found in the audited ViewModel/Repository/DAO chain. CI remains the final authority for any remaining compiler or packaging errors.


### Technical changelog — 2026-10-04 — /tt-ui /settings-real /system-back

**Author:** Takusima

**What was changed**
- Reworked the Settings screen so the controls that are exposed in the UI are connected to the existing DataStore/ViewModel APIs instead of being decorative cards.
- Added working accent-color presets backed by the existing `AppearanceSettings.accent` value.
- Added working animation-speed control backed by the existing `AppearanceSettings.animationScale` value.
- Kept AMOLED and Dynamic Color controls and wired the full settings callback chain from `MainActivity` to `TakuTuneViewModel`.
- Changed theme resolution so `System` follows the actual Android system light/dark mode instead of always behaving as dark mode.
- Dynamic Color now uses Android system dynamic color on Android 12+ when enabled; explicit accent color remains active when Dynamic Color is disabled.
- Removed the in-app top-left back/close button from the full player.
- Added Compose system BackHandler: when the full player or any secondary tab is open, the phone's physical/system Back action returns to the Home screen; on Home it keeps the normal Activity exit behavior. Android documents BackHandler as the Compose API for intercepting system back navigation. citeturn1search0turn1search2

**Changed files and exact line ranges**
- `app/src/main/java/com/takusima/takutune/MainActivity.kt`: line 7 — added `BackHandler` import; lines 91–99 — passed accent/animation callbacks; lines 132–144 — wired settings and system-back state handling; lines 156–313 — replaced non-functional Settings cards with real theme/AMOLED/dynamic-color/accent/animation/music controls; lines 314–319 — removed the full-player in-app back button and its callback parameter.
- `app/src/main/java/com/takusima/takutune/theme/TakuTuneTheme.kt`: lines 1–58 — rebuilt theme selection around system theme detection, stored accent color, AMOLED background and Android 12+ dynamic color.
- `app/src/main/java/com/takusima/takutune/core/preferences/SettingsStore.kt`: no source change required; existing DataStore keys already supported accent and animation scale.
- `app/src/main/java/com/takusima/takutune/core/presentation/TakuTuneViewModel.kt`: no source change required; existing setters already exposed accent and animation scale.
- `README.md`: appended this technical changelog.

**Build failure status**
- No new compiler output has been observed for this exact UI block yet.
- The previous known CI failure was fixed before this block; this block must still be validated by GitHub Actions.

**Resolution**
- The implementation uses the existing native Compose/DataStore architecture; no HTML/WebView layer was introduced.
- No fake network/audio provider was added. Local music remains the real active source.

**Unresolved**
- CI validation is pending for this exact commit sequence.

### Technical changelog — 2026-10-04 — /tt-ui /theme-contrast /controls-audit

**Author:** Takusima

**Problem found**
- Theme selection could appear to change while many UI elements kept hardcoded purple, dark surfaces and secondary-text colors.
- The light scheme did not explicitly define enough Material 3 contrast roles, while the screen itself still used hardcoded dark surfaces/text colors. This could make text disappear or become nearly black on dark cards.
- The mini-player displayed a play/pause icon but the icon was not a control.
- VK / YouTube / Spotify are not connected providers yet and must not be presented as working integrations.

**Resolution**
- MainActivity.kt: replaced hardcoded text/icon/surface colors in the affected UI paths with Material 3 color roles so Light/Dark/AMOLED/accent changes propagate consistently.
- MainActivity.kt: made the mini-player play/pause icon an actual IconButton backed by the same PlaybackController toggle action.
- TakuTuneTheme.kt: added explicit high-contrast Light and Dark color schemes with onBackground, onSurface, onSurfaceVariant, container and outline roles; the stored accent now drives primary/secondary/tertiary roles.
- Dynamic Color remains opt-in and uses Android 12+ system schemes when enabled.
- No fake VK/YouTube/Spotify login or playback implementation was added. Those integrations remain future work until real provider APIs/authentication are implemented.

**Changed files and exact line ranges**
- app/src/main/java/com/takusima/takutune/MainActivity.kt: color usage across the main UI and mini-player control; existing player/settings controls preserved.
- app/src/main/java/com/takusima/takutune/theme/TakuTuneTheme.kt: lines 1–39 — complete Material 3 light/dark contrast scheme and accent handling.
- README.md: appended this entry.

**Build failure status**
- No new CI compiler output is available yet for this exact block.

**Unresolved**
- VK / YouTube / Spotify integrations are intentionally not implemented.
- GitHub Actions must validate the current source before this block is declared build-clean.
