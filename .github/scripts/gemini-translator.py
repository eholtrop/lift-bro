#!/usr/bin/env python3
"""
Gemini-Powered String Translation for Kotlin Multiplatform Compose
Uses Google Gemini API for intelligent string translation with cost tracking.
Supports 10 languages with language-specific optimizations.
"""

import json
import os
import re
import time
import xml.etree.ElementTree as ET
from pathlib import Path
from typing import Dict, List, Set, Optional, Any, Tuple
from dataclasses import dataclass
from collections import defaultdict

print("WHAT IS HAPPENING")

# Import Google Generative AI with error handling
try:
    from google import genai
    from google.genai import types
except ImportError:
    print("❌ Error: google-genai package not found. Install with: pip install google-genai")
    exit(1)

# Cost tracking (per 1K tokens) for Gemini 2.0 Flash
GEMINI_2_FLASH_COSTS = {
    'input': 0.000075,   # $0.075 per 1M input tokens
    'output': 0.00015,    # $0.15 per 1M output tokens
}

# Language mapping with proper directory names
LANGUAGE_MAPPING = {
    'es': 'Spanish',
    'fr': 'French',
    'pt': 'Portuguese (International)',
    'pt-BR': 'Portuguese (Brazil)',
    'uk': 'Ukrainian',
    'de': 'German',
    'ja': 'Japanese',
    'zh-CN': 'Chinese (Simplified)',
    'ar': 'Arabic',
    'ru': 'Russian'
}

# Directory mapping for file creation
DIRECTORY_MAPPING = {
    'es': 'values-es',
    'fr': 'values-fr',
    'pt': 'values-pt',
    'pt-BR': 'values-pt-rBR',
    'uk': 'values-uk',
    'de': 'values-de',
    'ja': 'values-ja',
    'zh-CN': 'values-zh-rCN',
    'ar': 'values-ar',
    'ru': 'values-ru'
}

# RTL languages
RTL_LANGUAGES = {'ar'}

# Language-specific translation prompts
TRANSLATION_PROMPTS = {
    'ar': """
    Translate to Arabic (RTL language):
    - Preserve ALL placeholders exactly: %s, %d, %1$s, %2$s
    - Keep strings concise for mobile UI
    - Use modern Standard Arabic
    - Maintain professional fitness app terminology
    - Return valid JSON only
    """,

    'ja': """
    Translate to Japanese:
    - Use appropriate politeness level (です/ます form)
    - Keep strings concise for mobile interfaces
    - Use common fitness terminology in Japanese
    - Preserve all placeholders exactly: %s, %d, %1$s
    - Return valid JSON only
    """,

    'pt-BR': """
    Translate to Brazilian Portuguese:
    - Use Brazilian Portuguese terminology and spelling
    - Use você form (not tu)
    - Apply Brazilian fitness/exercise terminology
    - Keep natural, informal but professional tone
    - Preserve all placeholders exactly: %s, %d, %1$s
    - Return valid JSON only
    """,

    'zh-CN': """
    Translate to Simplified Chinese:
    - Use simplified Chinese characters
    - Keep strings concise for mobile UI
    - Use common Chinese fitness terminology
    - Preserve all placeholders exactly: %s, %d, %1$s
    - Return valid JSON only
    """,

    'de': """
    Translate to German:
    - Use formal "Sie" form for fitness app
    - Keep strings concise but complete
    - Use German fitness terminology
    - Preserve all placeholders exactly: %s, %d, %1$s
    - Return valid JSON only
    """,

    'ru': """
    Translate to Russian:
    - Use formal Russian for fitness app
    - Keep strings professional but accessible
    - Use Russian fitness terminology
    - Preserve all placeholders exactly: %s, %d, %1$s
    - Return valid JSON only
    """,

    'uk': """
    Translate to Ukrainian:
    - Use Ukrainian characters properly (ї, є, ґ)
    - Keep strings professional and encouraging
    - Use Ukrainian fitness terminology
    - Preserve all placeholders exactly: %s, %d, %1$s
    - Return valid JSON only
    """,

    'es': """
    Translate to Spanish:
    - Use neutral Spanish for international audiences
    - Keep strings encouraging and motivational
    - Use Spanish fitness terminology
    - Preserve all placeholders exactly: %s, %d, %1$s
    - Return valid JSON only
    """,

    'fr': """
    Translate to French:
    - Use appropriate French for fitness app
    - Keep strings motivational and professional
    - Use French fitness terminology
    - Preserve all placeholders exactly: %s, %d, %1$s
    - Return valid JSON only
    """,

    'pt': """
    Translate to Portuguese (International):
    - Use neutral Portuguese for all regions
    - Keep strings encouraging and professional
    - Use Portuguese fitness terminology
    - Preserve all placeholders exactly: %s, %d, %1$s
    - Return valid JSON only
    """
}

@dataclass
class TranslationResult:
    language: str
    success: bool
    translated_strings: Dict[str, str]
    cost: float
    error_message: Optional[str] = None
    input_tokens: int = 0
    output_tokens: int = 0

@dataclass
class TranslationBatch:
    total_strings: int
    results: List[TranslationResult]
    total_cost: float
    successful_languages: List[str]
    failed_languages: List[str]

class TranslationCostTracker:
    def __init__(self, monthly_budget: float = 30.0):
        self.monthly_budget = monthly_budget
        self.current_usage = 0.0
        self.api_calls = 0
        self.tokens_used = 0

    def track_usage(self, input_tokens: int, output_tokens: int) -> float:
        """Track translation usage and cost"""
        input_cost = (input_tokens * GEMINI_2_FLASH_COSTS['input']) / 1000
        output_cost = (output_tokens * GEMINI_2_FLASH_COSTS['output']) / 1000
        call_cost = input_cost + output_cost

        self.current_usage += call_cost
        self.api_calls += 1
        self.tokens_used += input_tokens + output_tokens

        return call_cost

    def can_proceed(self, estimated_cost: float) -> bool:
        """Check if operation fits within budget"""
        return self.current_usage + estimated_cost <= self.monthly_budget

    def get_summary(self) -> Dict:
        """Get current usage summary"""
        return {
            'current_cost': round(self.current_usage, 4),
            'remaining_budget': round(self.monthly_budget - self.current_usage, 2),
            'api_calls': self.api_calls,
            'tokens_used': self.tokens_used
        }

class GeminiTranslator:
    def __init__(self, api_key: str, budget_limit: float = 30.0, model: str = 'gemini-2.0-flash'):
        self.client = genai.Client(api_key=api_key)
        self.model = model
        self.cost_tracker = TranslationCostTracker(budget_limit)

    def load_strings_xml(self, xml_file: str) -> Dict[str, str]:
        """Load strings from XML file with error handling"""
        try:
            tree = ET.parse(xml_file)
            root = tree.getroot()

            strings = {}
            for string_elem in root.findall('string'):
                key = string_elem.get('name')
                text = string_elem.text or ''
                strings[key] = text

            print(f"📖 Loaded {len(strings)} strings from {Path(xml_file).name}")
            return strings

        except Exception as e:
            print(f"❌ Error parsing {xml_file}: {e}")
            return {}

    def detect_new_strings(self, base_strings_file: str) -> Dict[str, str]:
        """Detect new or changed strings that need translation"""
        base_strings = self.load_strings_xml(base_strings_file)
        if not base_strings:
            return {}

        # For now, translate all strings. In the future, we can compare with existing translations
        print(f"🔍 Detected {len(base_strings)} strings for translation")
        return base_strings

    def create_translation_prompt(self, strings: Dict[str, str], target_language: str) -> str:
        """Create translation prompt for specific language"""

        # Create JSON input
        strings_json = json.dumps(strings, ensure_ascii=False, indent=2)

        # Get language-specific prompt
        language_prompt = TRANSLATION_PROMPTS.get(target_language, TRANSLATION_PROMPTS['es'])

        prompt = f"""
You are a professional Android app localizer specializing in fitness applications.

Translate these UI strings to {LANGUAGE_MAPPING[target_language]}.

{language_prompt}

STRINGS TO TRANSLATE:
```json
{strings_json}
```

Return valid JSON only with the same structure and keys:
```json
{{
  "key1": "translated_text1",
  "key2": "translated_text2"
}}
```
"""
        return prompt

    def translate_strings_for_language(self, strings: Dict[str, str], target_language: str) -> TranslationResult:
        """Translate strings to a specific language"""

        # Check budget
        estimated_cost = 0.02  # Conservative estimate per language
        if not self.cost_tracker.can_proceed(estimated_cost):
            return TranslationResult(
                language=target_language,
                success=False,
                translated_strings={},
                cost=0,
                error_message="Budget limit reached"
            )

        prompt = self.create_translation_prompt(strings, target_language)

        try:
            print(f"🌍 Translating to {LANGUAGE_MAPPING[target_language]}...")

            # Make API call
            response = self.client.models.generate_content(
                model=self.model,
                contents=prompt,
                config=types.GenerateContentConfig(
                    temperature=0.2,
                    max_output_tokens=8192,
                    response_mime_type="application/json"
                )
            )

            # Get response text
            response_text = response.text if hasattr(response, 'text') else str(response)

            # Estimate tokens
            input_tokens = len(prompt) // 4  # Rough estimation
            output_tokens = len(response_text) // 4

            # Track cost
            actual_cost = self.cost_tracker.track_usage(input_tokens, output_tokens)

            # Parse JSON response
            try:
                translated_strings = json.loads(response_text)

                # Validate structure
                if not isinstance(translated_strings, dict):
                    raise ValueError("Response is not a valid JSON object")

                print(f"✅ Successfully translated {len(translated_strings)} strings")

                return TranslationResult(
                    language=target_language,
                    success=True,
                    translated_strings=translated_strings,
                    cost=actual_cost,
                    input_tokens=input_tokens,
                    output_tokens=output_tokens
                )

            except json.JSONDecodeError as e:
                print(f"❌ JSON parsing error for {target_language}: {e}")
                return TranslationResult(
                    language=target_language,
                    success=False,
                    translated_strings={},
                    cost=actual_cost,
                    error_message=f"JSON parsing error: {e}",
                    input_tokens=input_tokens,
                    output_tokens=output_tokens
                )

        except Exception as e:
            print(f"❌ Translation failed for {target_language}: {e}")
            return TranslationResult(
                language=target_language,
                success=False,
                translated_strings={},
                cost=0,
                error_message=str(e)
            )

    def create_strings_xml(self, strings: Dict[str, str], language: str) -> ET.Element:
        """Create a strings XML element for target language"""
        root = ET.Element('resources')

        # Sort keys alphabetically for consistency
        for key in sorted(strings.keys()):
            string_elem = ET.SubElement(root, 'string')
            string_elem.set('name', key)
            string_elem.text = strings[key]

        return root

    def update_language_file(self, strings: Dict[str, str], language: str, base_path: str) -> bool:
        """Update language-specific strings file"""

        # Get directory for language
        directory_name = DIRECTORY_MAPPING[language]
        language_dir = os.path.join(base_path, directory_name)

        # Ensure directory exists
        os.makedirs(language_dir, exist_ok=True)

        # Create XML
        xml_file = os.path.join(language_dir, 'strings.xml')
        root = self.create_strings_xml(strings, language)

        try:
            tree = ET.ElementTree(root)
            # Add proper formatting
            ET.indent(tree, space='    ', level=0)
            tree.write(xml_file, encoding='utf-8', xml_declaration=True)
            print(f"💾 Updated {xml_file}")
            return True
        except Exception as e:
            print(f"❌ Failed to save {language} translations: {e}")
            return False

    def translate_all_languages(self, languages: List[str], strings: Dict[str, str], base_path: str) -> TranslationBatch:
        """Translate strings to all supported languages"""

        print(f"🌍 Translating {len(strings)} strings to {len(languages)} languages")

        results = []
        successful_languages = []
        failed_languages = []

        # Translate to each language
        for language in languages:
            result = self.translate_strings_for_language(strings, language)
            results.append(result)

            if result.success:
                successful_languages.append(language)
                # Update the language file
                self.update_language_file(result.translated_strings, language, base_path)
            else:
                failed_languages.append({
                    'language': language,
                    'error': result.error_message
                })

        # Calculate total cost
        total_cost = sum(r.cost for r in results)

        return TranslationBatch(
            total_strings=len(strings),
            results=results,
            total_cost=total_cost,
            successful_languages=successful_languages,
            failed_languages=failed_languages
        )

def main():
    import argparse

    print("Parsing args")

    parser = argparse.ArgumentParser(description='Gemini-Powered String Translation')
    parser.add_argument('--input', required=True, help='Input strings XML file')
    parser.add_argument('--base-path', required=True, help='Base path for composeResources')
    parser.add_argument('--budget-limit', type=float, default=30.0, help='Monthly budget limit')
    parser.add_argument('--debug', action='store_true', help='Show debug output')

    args = parser.parse_args()

    print("Args Parsed")

    # Validate inputs
    if not os.path.exists(args.input):
        print(f"❌ Input file not found: {args.input}")
        exit(1)

    # Setup
    api_key = os.environ.get('GEMINI_API_KEY')
    if not api_key:
        print("❌ Error: GEMINI_API_KEY environment variable not set")
        print("💡 This should be set in GitHub Secrets")
        exit(1)

    # Initialize translator
    translator = GeminiTranslator(api_key, args.budget_limit)

    # Detect new strings
    strings_to_translate = translator.detect_new_strings(args.input)
    if not strings_to_translate:
        print("ℹ️ No strings found to translate")
        exit(0)

    # TODO: Filter languages if specified
    languages_to_translate = LANGUAGE_MAPPING.keys()
    print(f"🎯 Translating to specified languages: {languages_to_translate}")

    # Start translation
    start_time = time.time()
    results = translator.translate_all_languages(LANGUAGE_MAPPING.keys(), strings_to_translate, args.base_path)
    processing_time = time.time() - start_time

    # Report results
    print(f"\n📊 Translation Summary:")
    print(f"  ✅ Successful: {len(results.successful_languages)} languages")
    print(f"  ❌ Failed: {len(results.failed_languages)} languages")
    print(f"  📝 Total strings: {results.total_strings}")
    print(f"  💰 Total cost: ${results.total_cost:.4f}")
    print(f"  ⏱️ Processing time: {processing_time:.2f}s")

    if results.failed_languages:
        print(f"\n⚠️ Failed translations:")
        for failure in results.failed_languages:
            print(f"  - {failure['language']}: {failure['error']}")

    # Get cost summary
    cost_summary = translator.cost_tracker.get_summary()
    print(f"\n💰 Cost Tracking:")
    print(f"  Current cost: ${cost_summary['current_cost']}")
    print(f"  Remaining budget: ${cost_summary['remaining_budget']}")
    print(f"  API calls: {cost_summary['api_calls']}")
    print(f"  Tokens used: {cost_summary['tokens_used']}")

    # Set GitHub Actions output
    success_count = len(results.successful_languages)
    if success_count > 0:
        languages_joined = ','.join(results.successful_languages)
        with open(os.environ['GITHUB_OUTPUT'], 'a') as f:
            f.write(f"languages-updated={languages_joined}\n")
            f.write(f"translation-cost=${results.total_cost:.4f}\n")
            f.write(f"strings-translated={results.total_strings}\n")
            f.write(f"processing-time={processing_time:.2f}\n")
            f.write(f"api-calls={cost_summary['api_calls']}\n")
            f.write(f"tokens-used={cost_summary['tokens_used']}\n")
            f.write(f"remaining-budget={cost_summary['remaining_budget']:.2f}\n")
            f.write(f"translation-success=true\n")

        print(f"✅ Translation completed successfully")
        exit(0)
    else:
        print(f"❌ All translations failed")
        with open(os.environ.get('GITHUB_OUTPUT', '/dev/null'), 'a') as f:
            f.write(f"translation-success=false\n")
        exit(1)

if __name__ == '__main__':
    main()
