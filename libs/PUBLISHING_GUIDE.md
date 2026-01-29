# Publishing Guide

All libraries are now configured with the Vanniktech Maven Publish plugin for easy publication to Maven Central or other Maven repositories.

## Configured Artifacts

### Logging
- **Group ID**: `tv.dpal`
- **Artifact ID**: `logging`
- **Version**: `0.1.0`

### Ext (3 modules)
- **Group ID**: `tv.dpal`
- **Artifact IDs**:
  - `ext-flow`
  - `ext-compose`
  - `ext-ktx-datetime`
- **Version**: `0.1.0`

### FlowVi (2 modules)
- **Group ID**: `tv.dpal`
- **Artifact IDs**:
  - `flowvi-core`
  - `flowvi-compose`
- **Version**: `0.1.0`

### SwipeNavHost
- **Group ID**: `tv.dpal`
- **Artifact ID**: `swipenavhost`
- **Version**: `0.1.0`

## Publishing Tasks

The Vanniktech plugin provides these Gradle tasks:

### Local Testing
```bash
# Publish to local Maven repository (~/.m2/repository)
./gradlew publishToMavenLocal
```

### Publishing to Maven Central

1. **Set up your credentials** in `~/.gradle/gradle.properties`:
```properties
mavenCentralUsername=your-username
mavenCentralPassword=your-token

signing.keyId=YOUR_KEY_ID
signing.password=YOUR_KEY_PASSWORD
signing.secretKeyRingFile=/path/to/secring.gpg
```

2. **Publish and release**:
```bash
# Publish to Maven Central Staging
./gradlew publishAndReleaseToMavenCentral --no-configuration-cache

# Or publish without auto-release (manual release via Sonatype UI)
./gradlew publishToMavenCentral --no-configuration-cache
```

### Publishing to GitHub Packages

Add to `gradle.properties`:
```properties
githubUsername=your-github-username
githubToken=your-github-token
githubRepository=your-org/your-repo
```

Then:
```bash
./gradlew publishAllPublicationsToGitHubPackagesRepository
```

## Before Publishing

### 1. Update Placeholder Information

Each `build.gradle.kts` contains placeholder values that need to be updated:

- Replace `yourusername` with your actual GitHub username
- Replace `Your Name` with your actual name
- Replace `your.email@example.com` with your email
- Update repository URLs to match your actual GitHub repositories

### 2. Update Version Numbers

When ready to publish, update the version in each module's `build.gradle.kts`:
```kotlin
mavenPublishing {
    coordinates(
        groupId = "tv.dpal",
        artifactId = "logging",
        version = "0.1.0" // Update this
    )
    // ...
}
```

### 3. Handle Cross-Library Dependencies

Some libraries depend on others. When all are published, uncomment the dependency TODOs in:
- `ext/flow/build.gradle.kts` (depends on logging)
- `flowvi/core/build.gradle.kts` (depends on logging)
- `flowvi/compose/build.gradle.kts` (depends on flowvi-core, logging)
- `swipenavhost/build.gradle.kts` (depends on flowvi-core, logging)

## Recommended Publishing Order

Due to dependencies, publish in this order:

1. **logging** (no dependencies)
2. **ext-flow**, **ext-compose**, **ext-ktx-datetime** (ext-flow depends on logging)
3. **flowvi-core** (depends on logging)
4. **flowvi-compose** (depends on flowvi-core and logging)
5. **swipenavhost** (depends on flowvi-core and logging)

## Maven Central Setup

Before you can publish to Maven Central, you need to:

1. **Create a Sonatype account**: https://issues.sonatype.org/
2. **Create a JIRA ticket** to claim your group ID (`tv.dpal`)
3. **Set up GPG signing keys** for artifact signing
4. **Configure gradle.properties** with your credentials

See the [Vanniktech Maven Publish plugin documentation](https://vanniktech.github.io/gradle-maven-publish-plugin/) for detailed setup instructions.

## Additional Notes

- The plugin automatically handles source and javadoc JARs
- All artifacts will be signed if signing is configured
- POM files are automatically generated with the configured metadata
- Each library has Apache 2.0 license specified in the POM
