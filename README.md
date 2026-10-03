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
