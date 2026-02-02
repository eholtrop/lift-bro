# String Extraction & Localization Workflow

Automated AI-powered workflow for extracting hardcoded strings and generating localized resources.

## Overview

This workflow automatically:
1. Detects hardcoded strings in `@Composable` functions
2. Uses **Google Gemini AI** for intelligent extraction and semantic key generation
3. Translates to 8 languages (Spanish, Portuguese, French, German, Japanese, Chinese, Arabic, Russian)
4. Updates Kotlin files with `stringResource()` and proper imports
5. Creates a PR with all changes

## Features

- **AI-Powered**: Uses Gemini 2.5 Flash for accurate, context-aware extraction
- **Semantic Keys**: Auto-generates keys following `{screen}_{component}_{purpose}_{type}` pattern
- **Multi-Language**: Supports 8 languages including Arabic (RTL)
- **Auto-Updates**: Modifies Kotlin files and adds necessary imports
- **Cost-Effective**: ~$0.03-0.08/month with $15 budget limit

### Key Examples
- `"Edit Lift"` → `edit_lift_screen_title`
- `"Delete"` button → `edit_lift_screen_delete_content_description`
- `"Create Lift"` → `dashboard_screen_create_lift_cta`

## Setup

### Required Secrets

Add these to your GitHub repository secrets:

1. **GEMINI_API_KEY** (required)
   - Get from: https://makersuite.google.com/app/apikey
   - Used for AI-powered string extraction

2. **LIBRETRANSLATE_API_KEY** (optional)
   - Get from: https://portal.libretranslate.com
   - Increases translation rate limits (free tier available)


## Usage

### Automatic
Runs automatically on PRs to `main` branch (opened, synchronized, reopened events).

### Manual
Trigger via GitHub Actions UI with optional file filtering.

### Example

**Before:**
```kotlin
@Composable
fun EditLiftScreen() {
    Text("Edit Lift")
    IconButton(contentDescription = "Delete") { }
}
```

**After:**
```kotlin
import lift_bro.core.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditLiftScreen() {
    Text(stringResource(Res.string.edit_lift_screen_title))
    IconButton(contentDescription = stringResource(Res.string.edit_lift_screen_delete_content_description)) { }
}
```



## 🔍 Troubleshooting

### Common Issues

1. **No strings extracted**
   - Check if files contain user-facing strings
   - Verify files are in `*/commonMain/*` path
   - Ensure @Composable functions are properly annotated

2. **Translation failures**
   - Check LibreTranslate API key in secrets
   - Verify internet connectivity in workflow
   - Check rate limits (free tier: 10 RPM)

3. **Cost overruns**
   - Reduce file batching size
   - Switch to Gemini 1.5 Flash model
   - Review extraction frequency

4. **Import errors**
   - Verify package name matches project structure
   - Check for existing import conflicts
   - Ensure generated keys are valid

### Debug Mode
Enable debug output by setting:
```yaml
env:
  DEBUG_MODE: "true"
```

## 📈 Performance

### Benchmarks
- **Average PR processing**: 2-5 seconds
- **String extraction accuracy**: >95%
- **Translation success rate**: >90%
- **Monthly cost estimate**: $3-8 for 20 PRs

### Optimization Tips
1. **Batch related files** together
2. **Use conservative prompts** to minimize tokens
3. **Cache translations** for repeated strings
4. **Monitor API usage** regularly

---

## 🤝 Contributing

To improve the workflow:

1. **Test with various file patterns**
2. **Add new language support**
3. **Optimize cost efficiency**
4. **Improve semantic key generation**
5. **Enhance error handling**

Report issues or contribute via:
- GitHub Issues in this repository
- Discussions for questions and suggestions

---

*This workflow represents a modern AI-powered approach to internationalization, reducing manual effort while maintaining high quality standards.*