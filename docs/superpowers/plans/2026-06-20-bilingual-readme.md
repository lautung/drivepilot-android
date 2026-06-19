# Bilingual README Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Publish equivalent English and Simplified Chinese GitHub README files with four real MuMu emulator screenshots.

**Architecture:** Keep English as the GitHub landing page and Chinese as a peer document, linked in both directions. Store stable PNG screenshots in `docs/screenshots/` and reference them through repository-relative paths.

**Tech Stack:** Markdown, GitHub HTML tables, ADB, MuMu Android 12, PowerShell, Git

---

### Task 1: Capture the primary navigation screens

**Files:**
- Create: `docs/screenshots/home.png`
- Create: `docs/screenshots/discover.png`
- Create: `docs/screenshots/services.png`
- Create: `docs/screenshots/profile.png`

- [x] **Step 1: Confirm the emulator and application process**

Run: `adb devices -l` and `adb -s 127.0.0.1:16384 shell pidof -s com.lautung.phonecar`

Expected: the MuMu device is `device` and the application has a process ID.

- [x] **Step 2: Dump the Android UI hierarchy and derive navigation coordinates**

Run: `adb -s 127.0.0.1:16384 exec-out uiautomator dump /dev/tty`

Expected: nodes labelled `首页`, `发现`, `服务`, and `我的` include bounds used to calculate tap centers.

- [x] **Step 3: Capture all four screens**

Tap each UI-hierarchy-derived center with `adb shell input tap`, wait for Compose to settle, and save a PNG with `adb exec-out screencap -p`.

- [x] **Step 4: Inspect all PNG files**

Open each image and confirm it shows the intended selected bottom navigation item, complete Chinese text, and no system dialog or transition state.

### Task 2: Write the English landing page

**Files:**
- Create: `README.md`

- [x] **Step 1: Add project presentation and language switch**

Use `DrivePilot Android` as the title and link `README.zh-CN.md` as `简体中文`.

- [x] **Step 2: Add screenshots and repository-accurate documentation**

Document the 16 screens, four feature domains, offline fake data, persisted interactions, Kotlin/Compose architecture, Android 7.0+ requirement, Gradle commands, APK path, and non-production vehicle-control disclaimer.

### Task 3: Write the Simplified Chinese version

**Files:**
- Create: `README.zh-CN.md`

- [x] **Step 1: Mirror the English structure and facts in Chinese**

Link back to `README.md` as `English` and preserve the same screenshot gallery, commands, requirements, architecture, and disclaimer.

### Task 4: Validate and publish

**Files:**
- Verify: `README.md`
- Verify: `README.zh-CN.md`
- Verify: `docs/screenshots/*.png`

- [x] **Step 1: Validate paths and content parity**

Run PowerShell `Test-Path` checks for both README files and all four PNG files, scan for unfinished-content markers and broken local Markdown targets, and run `git diff --check`.

Expected: every path exists, no placeholders are found, and `git diff --check` exits successfully.

- [x] **Step 2: Review the exact publish scope**

Run: `git status --short` and `git diff --stat`.

Expected: only the two README files, four screenshots, and this plan are new; `.idea` files remain outside the documentation commit.

- [x] **Step 3: Commit and push**

Commit only documentation and screenshots with `docs: add bilingual project README`, then run `git push origin master`.

Expected: `origin/master` resolves to the new documentation commit.
