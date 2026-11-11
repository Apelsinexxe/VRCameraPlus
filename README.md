# VRCamera Plus

**VRCamera Plus** — аддон для мода [VRCamera](https://github.com/Deelter/VRCamera), добавляющий новые возможности вывода камеры и просмотр камер других игроков.

## Возможности

- **Вывод картинки камеры в угол окна игры** (как миникарта), чтобы OBS захватывал всё одним окном.
- **Вертикальный формат 9:16** (как в ТикТоке) для угла и отдельного окна.
- **Перетаскивание картинки мышкой** в настройках — поставь угол в любое место.
- **Просмотр камер других игроков** (view only, без управления).
- **Скрытие своей камеры** в мире, пока смотришь чужую.
- **Настройки в Mod Menu** и в меню паузы.
- **Live preview** в настройках: угол показывается прямо в окне настроек.

## Установка

1. Установи [Fabric Loader](https://fabricmc.net/use/) для Minecraft.
2. Скачай и положи в папку `mods`:
   - [Fabric API](https://modrinth.com/mod/fabric-api)
   - [VRCamera](https://modrinth.com/mod/vrcamera) (основной мод)
   - [VRCamera Plus](https://github.com/Apelsinexxe/VRCameraPlus/releases) (этот аддон)
   - (опционально) [Mod Menu](https://modrinth.com/mod/modmenu) для кнопки настроек
3. Запусти игру.

## Использование

### Настройки
- **Mod Menu**: кнопка «VRCamera Plus» в списке модов.
- **Меню паузы**: кнопка «Настройки VRCamera Plus» внизу слева.
- **Горячая клавиша**: настраивается в Controls → Key Binds → VRCamera Plus.

### В настройках:
- **Камера**: своя или выбери игрока из списка.
- **Вывод**: угол экрана или отдельное окно.
- **Формат**: обычный (по форме окна игры) или вертикальный 9:16.
- **Позиция**: один из четырёх углов или **перетащи картинку мышкой** в настройках.
- **Размер**: от 10% до 60% ширины окна.

### Команды
```
/vrcamcorner status             — текущее состояние
/vrcamcorner on|off             — включить/выключить
/vrcamcorner config             — открыть настройки
/vrcamcorner position <угол>    — top_left | top_right | bottom_left | bottom_right
/vrcamcorner size <0.1-0.6>     — размер в долях ширины
```

## Требования

- **Minecraft**: 26.2 (Neo)
- **Fabric Loader**: 0.19.5+
- **VRCamera**: 2.2.0+
- **Fabric API**
- Java 25

## Для разработчиков

Проект использует:
- **Fabric Loom** 1.18.2
- **Gradle** 9.7.0
- **Mixins** для интеграции с VRCamera

### Сборка
```bash
./gradlew build
```
JAR появится в `build/libs/`.

## Лицензия

GPL-3.0 — можешь использовать, изменять и распространять мод, но производные работы тоже должны быть GPL-3.0.

## Ссылки

- [VRCamera (основной мод)](https://github.com/Deelter/VRCamera)
- [VRCamera на Modrinth](https://modrinth.com/mod/vrcamera)

---

## English

**VRCamera Plus** is an addon for [VRCamera](https://github.com/Deelter/VRCamera) mod that adds new camera output options and the ability to view other players' cameras.

### Features
- **Camera picture in the game window corner** (like a minimap) for single-window OBS capture.
- **Vertical 9:16 format** (TikTok-style) for corner and separate window.
- **Drag the corner with the mouse** in settings.
- **View other players' cameras** (view only, no control).
- **Hide your own camera model** while watching another player's camera.
- **Settings in Mod Menu** and pause menu.
- **Live preview** in settings screen.

### Installation
1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft.
2. Download and place in `mods` folder:
   - [Fabric API](https://modrinth.com/mod/fabric-api)
   - [VRCamera](https://modrinth.com/mod/vrcamera)
   - [VRCamera Plus](https://github.com/Apelsinexxe/VRCameraPlus/releases)
   - (optional) [Mod Menu](https://modrinth.com/mod/modmenu)
3. Launch the game.

### Requirements
- **Minecraft**: 26.2 (Neo)
- **Fabric Loader**: 0.19.5+
- **VRCamera**: 2.2.0+
- **Fabric API**
- Java 25
