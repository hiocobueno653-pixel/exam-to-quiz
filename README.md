# 试卷转题库 App

Kotlin + Jetpack Compose 实现的 Android 应用。

## 功能

- 试卷拍照识别（CameraX + ML Kit）
- AI 生成答案（DeepSeek API）
- 题库管理（Room 数据库）
- 练习模式 & 测验模式

## 本地构建

用 Android Studio 打开 `android-app` 目录，然后 Build → Build APK。

## GitHub Actions 自动构建

推送代码到 GitHub 后，Actions 会自动编译 APK，可在 Actions 页面下载。

## 技术栈

- Kotlin + Jetpack Compose + Material3
- Room 数据库 + KSP
- Retrofit + OkHttp + Gson
- CameraX + ML Kit
- DeepSeek API