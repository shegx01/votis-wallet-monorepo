# Code Quality Tools

This project is set up with code formatting and quality analysis tools to maintain consistent code style and catch potential issues early.

## Tools Used

### Ktlint
- **Purpose**: Kotlin code formatting and style checking
- **Version**: 1.3.1
- **Configuration**: `.editorconfig` file in project root

### Detekt  
- **Purpose**: Static code analysis for Kotlin
- **Version**: 1.23.7
- **Configuration**: `config/detekt/detekt.yml`

## Available Commands

### Formatting
```bash
# Format all Kotlin code using ktlint
./gradlew formatCode

# Format specific module only
./gradlew :composeApp:ktlintFormat
```

### Code Quality Checks
```bash
# Run all code quality checks (ktlint + detekt)
./gradlew codeQuality

# Check code style only (no auto-fix)
./gradlew checkCodeStyle

# Run ktlint check only
./gradlew :composeApp:ktlintCheck

# Run detekt analysis only
./gradlew :composeApp:detekt
```

### Combined Commands
```bash
# Format code and run all quality checks
./gradlew formatAndCheck
```

## Configuration Details

### Ktlint Configuration (.editorconfig)
The project is configured to:
- Allow Compose functions to start with capital letters (e.g., `@Composable fun Button()`)
- Allow wildcard imports for common packages like `androidx.compose.*`
- Allow flexible file naming for UI/Compose components
- Set maximum line length to 120 characters

### Detekt Configuration
- Uses comprehensive rule set with sensible defaults
- Configured for Compose/Android development
- Includes rules for complexity, style, potential bugs, and more
- Reports available in HTML, XML, TXT, SARIF, and Markdown formats

## IDE Integration

### IntelliJ IDEA / Android Studio
1. Install the ktlint and detekt plugins
2. Configure ktlint to use project's `.editorconfig`
3. Set up detekt to use `config/detekt/detekt.yml`

### VS Code
1. Install Kotlin extension
2. Configure ktlint integration in settings

## CI/CD Integration

Add these commands to your CI/CD pipeline:
```yaml
# Example GitHub Actions
- name: Check code style
  run: ./gradlew checkCodeStyle

- name: Run code quality checks  
  run: ./gradlew codeQuality
```

## Rules and Exceptions

The configuration has been tailored for Jetpack Compose and Android development:

- **Function naming**: Compose functions can start with uppercase letters
- **Wildcard imports**: Allowed for common Compose/Android packages
- **File naming**: Flexible naming allowed for UI components
- **Line length**: Set to 120 characters for better readability

## Troubleshooting

### Common Issues

1. **"Function name should start with a lowercase letter"**
   - This is disabled for `@Composable` functions in our configuration
   - If you see this error, the function might be missing the `@Composable` annotation

2. **"Wildcard import" warnings**  
   - These are disabled for common packages
   - For other packages, prefer explicit imports

3. **Detekt baseline file**
   - Located at `config/detekt/baseline.xml`
   - Regenerate with: `./gradlew :composeApp:detektBaseline`

### Updating Configurations

- **Ktlint rules**: Edit `.editorconfig` file
- **Detekt rules**: Edit `config/detekt/detekt.yml`
- **Tool versions**: Update `gradle/libs.versions.toml`

## Reports Location

After running quality checks, find reports at:
- **Ktlint**: `composeApp/build/reports/ktlint/`
- **Detekt**: `composeApp/build/reports/detekt/`
