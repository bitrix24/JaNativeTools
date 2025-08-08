# JaNativeTools

JaNativeTools is a plugin for JetBrains IDEs that provides advanced tools for managing dependencies, extensions, and color themes in JS.

## Features

- **Dependency Management**: Detect, sort, modify, and validate project dependencies.
- **Inspections & Quick Fixes**: Automatic detection of extension and dependency issues with quick-fix suggestions.
- **Color Previews**: Inline color previews in the editor.
- **Template Generation**: Create extension files and class/function templates.

## Getting Started

1. **Build the plugin:**
   ```sh
   ./gradlew build
   ```
2. **Install:**
    - Locate the built `.jar` or `.zip` in `build/libs`.
    - Install via JetBrains IDE: `Settings → Plugins → Install plugin from disk`.
3. **Usage:**
    - Access plugin features via file context menus, code inspections, and IDE settings.

## Requirements

- JDK 11+
- Compatible JetBrains IDE (e.g., PhpStorm, IntelliJ IDEA)

## License

Apache License 2.0