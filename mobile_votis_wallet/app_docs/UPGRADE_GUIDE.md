# Project Upgrade Guide

This guide outlines the best practices for upgrading this Kotlin Multiplatform Mobile (KMM) project to the latest stable versions.

## Current Status (as of December 2024)

### ✅ Recently Applied Updates
- **Gradle**: `8.9` → `8.12` (Latest stable)
- **AndroidX Lifecycle**: `2.9.1` → `2.9.3` 
- **ktlint**: `1.3.1` → `1.7.1`
- **Detekt**: `1.23.7` → `1.23.8`
- **Gradle Versions Plugin**: Added `0.51.0` for dependency monitoring

### 🚧 Skipped Updates (Waiting for Stable)
- **Android Gradle Plugin**: `8.7.3` (9.0.0 is alpha)
- **Kotlin**: `2.2.0` (2.2.20 is RC)
- **Compose Multiplatform**: `1.8.2` (1.9.0 is RC)
- **AndroidX Activity**: `1.10.1` (1.12.0 is alpha)

## Automated Upgrade Strategy

### 1. Dependency Monitoring
The project now includes the **Gradle Versions Plugin** to automatically detect available updates:

```bash
./gradlew dependencyUpdates
```

This generates a report at `build/dependencyUpdates/report.txt` showing:
- ✅ Dependencies using latest stable versions
- ⬆️ Available stable updates
- ⚠️ Alpha/Beta/RC versions (usually avoided)

### 2. Dependabot Configuration
Added `.github/dependabot.yml` for automated pull requests:

- **Schedule**: Weekly on Mondays at 9 AM
- **Grouping**: Dependencies are grouped by ecosystem (Android, Kotlin, Code Quality)
- **Safety**: Ignores alpha/beta/RC versions automatically
- **Limits**: Max 10 PRs to avoid spam

### 3. Version Catalog Strategy
The project uses Gradle Version Catalogs (`libs.versions.toml`) for centralized version management:

```toml
[versions]
kotlin = "2.2.0"  # Comments explain upgrade decisions
agp = "8.7.3"     # Staying on stable; 9.0 is alpha
```

## Manual Upgrade Process

### Step 1: Check Available Updates
```bash
./gradlew dependencyUpdates
```

### Step 2: Update Version Catalog
Edit `gradle/libs.versions.toml` with stable versions only:

**Safe Updates** ✅:
- Patch versions (1.2.3 → 1.2.4)
- Minor versions within same ecosystem
- Well-tested stable releases

**Risky Updates** ⚠️:
- Major version bumps (breaking changes)
- Alpha/Beta/RC versions
- Cross-ecosystem conflicts (AGP vs Kotlin vs Compose)

### Step 3: Test the Build
```bash
./gradlew clean codeQuality  # Fast quality checks
./gradlew clean build        # Full build (longer)
```

### Step 4: Update Gradle Wrapper
```bash
./gradlew wrapper --gradle-version=LATEST_STABLE
```

## Compatibility Matrix

| Component | Current | Latest Stable | Notes |
|-----------|---------|---------------|--------|
| Kotlin | 2.2.0 | 2.2.20-RC | Wait for 2.2.20 stable |
| AGP | 8.7.3 | 9.0.0-alpha03 | Major version - needs testing |
| Compose MP | 1.8.2 | 1.9.0-rc01 | RC version - almost ready |
| Gradle | 8.12 | 8.12 | ✅ Up to date |

## Known Issues & Solutions

### Android Lint Compatibility
**Issue**: Kotlin 2.2.0 metadata incompatible with older Android Lint
**Solution**: Temporarily disable lint for unstable builds or wait for AGP 8.8+

### Lifecycle Version Conflicts
**Issue**: Multiple lifecycle library versions causing KLIB warnings
**Solution**: Use Bill of Materials (BOM) when available

## Future Automation

### 1. CI Integration
Add to your CI pipeline:
```yaml
- name: Check for outdated dependencies
  run: ./gradlew dependencyUpdates
- name: Upload dependency report
  uses: actions/upload-artifact@v4
  with:
    name: dependency-updates
    path: build/dependencyUpdates/report.txt
```

### 2. Renovate Alternative
For more advanced automation, consider [Renovate](https://renovatebot.com/) instead of Dependabot:
- Better KMM support
- Custom update rules
- Smarter conflict resolution

### 3. Version Alignment
Consider adding BOMs for better version alignment:
- `androidx.compose:compose-bom` for Compose dependencies
- `org.jetbrains.kotlin:kotlin-bom` for Kotlin ecosystem

## Upgrade Schedule Recommendation

- **Weekly**: Patch version updates (automated via Dependabot)
- **Monthly**: Minor version updates (review and test)
- **Quarterly**: Major version updates (plan and test thoroughly)
- **As-needed**: Security updates (immediate)

## Commands Reference

```bash
# Check for updates
./gradlew dependencyUpdates

# Run quality checks
./gradlew codeQuality

# Full clean build
./gradlew clean build

# Update Gradle wrapper
./gradlew wrapper --gradle-version=8.12

# Format code
./gradlew formatCode

# Build with no daemon (CI)
./gradlew clean build --no-daemon
```

---

**Last Updated**: December 2024  
**Next Review**: January 2025
