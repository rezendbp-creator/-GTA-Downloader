# GTA Downloader

Исходный Android-проект на Kotlin. Скачивает файл по прямой HTTPS-ссылке через системный Android DownloadManager и сохраняет его в публичную папку Downloads.

## Сборка APK
1. Установи Android Studio и Android SDK Platform 35.
2. Открой эту папку в Android Studio.
3. Дождись Gradle Sync.
4. Выбери **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
5. APK будет в `app/build/outputs/apk/debug/app-debug.apk`.

## Примечания
- ZIP содержит исходный проект, а не готовый APK.
- Вставляй только HTTPS-ссылки на файлы, которые имеешь право скачивать.
- Загрузка выполняется Android DownloadManager и может продолжаться в фоне.
- В этой базовой версии нет распаковки ZIP и ручного управления паузой.
