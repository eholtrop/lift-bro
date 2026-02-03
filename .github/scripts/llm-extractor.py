#!/usr/bin/env python3
"""
LLM-Powered String Extraction for Kotlin Multiplatform Compose
Uses Google Gemini API for intelligent string extraction with semantic understanding.
"""

import json
import os
import re
import time
from pathlib import Path
from typing import List, Dict, Optional, Tuple, Union, Mapping
from collections import defaultdict
from dataclasses import dataclass

# Import Google Generative AI with error handling
try:
    from google import genai
    from google.genai import types
except ImportError:
    print("❌ Error: google-genai package not found. Install with: pip install google-genai")
    exit(1)

# Cost tracking (per 1K tokens)
GEMINI_COSTS = {
    'gemini-1.5-flash': {'input': 0.000075, 'output': 0.0003},  # $0.075/$0.30 per 1M tokens
    'gemini-1.5-flash-8b': {'input': 0.0000375, 'output': 0.00015},  # Cheaper variant
    'gemini-1.5-pro': {'input': 0.00125, 'output': 0.005},
}

@dataclass
class StringExtraction:
    text: str
    semantic_key: str
    context: str
    line_number: int
    file_path: str
    reasoning: str
    needs_import: bool = True

@dataclass
class FileAnalysis:
    file_path: str
    strings: List[StringExtraction]
    needs_resource_import: bool

class CostTracker:
    def __init__(self, monthly_budget: float = 15.0):
        self.monthly_budget = monthly_budget
        self.current_usage = 0.0
        self.api_calls = 0
        self.tokens_used = 0

    def track_usage(self, input_tokens: int, output_tokens: int, model: str):
        """Track API usage and cost"""
        cost_per_1k = GEMINI_COSTS.get(model, {'input': 0.0003, 'output': 0.0015})
        call_cost = (input_tokens * cost_per_1k['input'] / 1000) + (output_tokens * cost_per_1k['output'] / 1000)

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

class GeminiStringExtractor:
    def __init__(self, api_key: str, model: str = 'gemini-1.5-flash', budget_limit: float = 15.0):
        self.client = genai.Client(api_key=api_key)
        self.model = model
        self.cost_tracker = CostTracker(budget_limit)

    def read_file_content(self, file_path: str) -> str:
        """Read file content with error handling"""
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                return f.read()
        except Exception as e:
            print(f"Error reading {file_path}: {e}")
            return f"// Error reading file: {e}"

    def create_prompt(self, file_batch: List[str]) -> str:
        """Create prompt for string extraction"""

        # Read file contents
        file_contents = []
        for file_path in file_batch:
            content = self.read_file_content(file_path)
            file_name = Path(file_path).name
            file_contents.append(f"// File: {file_name}\n```kotlin\n{content}\n```")

        # Build prompt
        prompt = f"""
You are an expert Android/Kotlin localization specialist analyzing Compose Multiplatform UI code for string extraction.

TASK: Extract hardcoded strings from @Composable functions that should be localized, then generate semantic keys.

RULES:
1. EXTRACT only user-facing strings: buttons, labels, titles, descriptions, placeholders
2. IGNORE: debug strings, technical strings, URLs, empty strings, single characters, error messages
3. Generate semantic keys using: {{screen_name}}_{{component}}_{{purpose}}_{{type}} pattern
4. Use existing key patterns from project when similar
5. Replace conflicts by updating existing references (don't create duplicates)

KEY PATTERNS TO FOLLOW:
- Screen titles: {{screen}}_screen_title
- Content descriptions: {{screen}}_screen_content_description
- Button text: {{screen}}_screen_{{purpose}}_cta
- Dialog text: {{screen}}_dialog_{{purpose}}
- Placeholders: {{screen}}_{{component}}_placeholder
- General text: {{screen}}_{{component}}_text

SEMANTIC KEY EXAMPLES:
- "Edit Lift" in EditLiftScreen -> edit_lift_screen_title
- "Delete" button contentDescription -> edit_lift_screen_delete_content_description
- "Create Lift" button -> dashboard_screen_create_lift_cta
- "Enter lift name" placeholder -> edit_lift_screen_name_placeholder

SCREEN NAME EXTRACTION:
- Remove "Screen" suffix: EditLiftScreen -> edit_lift
- Convert to snake_case and add "_screen": edit_lift_screen
- For components: VariationTextField -> variation_field

FILES TO ANALYZE:
{chr(10).join(file_contents)}

RESPONSE FORMAT (valid JSON only):
{{
  "extracted_strings": [
    {{
      "text": "exact original string",
      "semantic_key": "generated_semantic_key",
      "context": "title|content_description|button|placeholder|dialog|text",
      "line_number": 123,
      "file_path": "file/package/path/file.kt",
      "reasoning": "brief explanation of key generation"
    }}
  ],
  "files_modified": ["file/package/path/file.kt"],
  "import_required": true,
  "total_strings_extracted": 1
}}
"""

        return prompt

    def extract_strings_batch(self, file_batch: List[str], debug: bool = True) -> Optional[Dict]:
        """Process batch of files with single Gemini API call"""

        # Check budget
        estimated_cost = 0.01  # Conservative estimate
        if not self.cost_tracker.can_proceed(estimated_cost):
            print(f"❌ Budget limit reached. Current usage: ${self.cost_tracker.current_usage:.4f}")
            return None

        # Create prompt
        prompt = self.create_prompt(file_batch)

        # Debug: show the prompt
        print("⚠️ DEBUG MODE: Showing prompt sent to Gemini")
        print("=" * 80)
        print(prompt)
        print("=" * 80)
        print(f"Prompt length: {len(prompt)} characters")
        print(f"Estimated tokens: ~{len(prompt) // 4}")
        print()

        try:
            # Make API call with structured output
            print("⏳ Making API call to Gemini...")

            response = self.client.models.generate_content(
                model=self.model,
                contents=prompt,
                config=types.GenerateContentConfig(
                    temperature=0.1,
                    max_output_tokens=4096,
                    response_mime_type="application/json"
                )
            )

            # Get response text
            response_text = response.text if hasattr(response, 'text') else str(response)

            print("✅ API call successful!")
            print("⚠️ DEBUG MODE: Showing Gemini response")
            print("=" * 80)
            print(response_text)
            print("=" * 80)
            print()

            # Estimate tokens (rough estimation)
            input_tokens = len(prompt) // 4  # Rough token estimation
            output_tokens = len(response_text) // 4

            # Track cost
            actual_cost = self.cost_tracker.track_usage(input_tokens, output_tokens, self.model)

            # Parse JSON response
            try:
                result = json.loads(response_text)
                result['cost'] = actual_cost
                result['input_tokens'] = input_tokens
                result['output_tokens'] = output_tokens

#                 if debug:
                print(f"✅ JSON parsed successfully")
                print(f"Extracted strings count: {len(result.get('extracted_strings', []))}")
                print()

                return result
            except json.JSONDecodeError as e:
                print(f"❌ JSON parsing error: {e}")
                print(f"Raw response: {response_text}")
                return None

        except Exception as e:
            print(f"❌ API call failed: {e}")
            import traceback
            print(f"Stack trace:")
            traceback.print_exc()
            return None

    def extract_string_objects(self, response_data: Dict, file_batch: List[str]) -> List[StringExtraction]:
        """Convert response data to StringExtraction objects"""
        strings = []

        print(f"Extracting Strings from Gemini response")
        for string_data in response_data.get('extracted_strings', []):
            extraction = StringExtraction(
                text=string_data['text'],
                semantic_key=string_data['semantic_key'],
                context=string_data['context'],
                line_number=string_data.get('line_number', 0),
                file_path=string_data['file_path'],
                reasoning=string_data.get('reasoning', ''),
                needs_import=response_data.get('import_required', True)
            )
            strings.append(extraction)

        print(f"Strings Extracted from Gemini: {len(strings)}")
        return strings

    def process_files(self, files: List[str], debug: bool = True) -> Tuple[List[FileAnalysis], Dict]:
        """Process all files for string extraction"""
        if not files:
            return [], {'error': 'No files to process'}

        print(f"📁 Processing {len(files)} files...")

        # Process all files in one batch (or split if needed)
        all_analyses = []
        total_strings = 0

        response = self.extract_strings_batch(files, debug=debug)

        if response:
            total_cost = response.get('cost', 0)
            total_strings = response.get('total_strings_extracted', 0)

            # Convert to analysis objects
            strings = self.extract_string_objects(response, files)

            # Create FileAnalysis for each file
            for file_path in files:
                print(f"extracting {file_path}")
                file_strings = [s for s in strings if file_path.endswith(s.file_path)]
                print(f"{file_strings}")
                if file_strings:
                    analysis = FileAnalysis(
                        file_path=file_path,
                        strings=file_strings,
                        needs_resource_import=True
                    )
                    print(f"analysis: {analysis}")
                    all_analyses.append(analysis)
                print(f"Analysis size: {len(all_analyses)}")
        else:
            print(f"❌ Failed to process files")
            total_cost = 0

        cost_summary = self.cost_tracker.get_summary()

        summary = {
            'files_processed': len(all_analyses),
            'total_strings': total_strings,
            'total_cost': total_cost,
            'cost_summary': cost_summary
        }

        return all_analyses, summary

def main():
    import argparse

    parser = argparse.ArgumentParser(description='LLM-Powered String Extraction')
    parser.add_argument('--files', required=True, help='Comma-separated list of files to analyze')
    parser.add_argument('--output-dir', required=True, help='Output directory for results')
    parser.add_argument('--pr-number', help='PR number for tracking')
    parser.add_argument('--budget-limit', type=float, default=15.0, help='Monthly budget limit')
    parser.add_argument('--debug', action='store_true', help='Show prompt sent to Gemini')

    args = parser.parse_args()

    # Setup
    os.makedirs(args.output_dir, exist_ok=True)

    api_key = os.environ.get('GEMINI_API_KEY')
    model = os.environ.get('GEMINI_MODEL', 'gemini-1.5-flash')

    print(f"🔧 Configuration:")
    print(f"   Model: {model}")
    print(f"   Output dir: {args.output_dir}")
    print(f"   Budget limit: ${args.budget_limit}")
    print(f"   Debug mode: {args.debug}")
    print()

    if not api_key:
        print("❌ Error: GEMINI_API_KEY environment variable not set")
        print("💡 This should be set in GitHub Secrets")
        exit(1)

    # Initialize extractor
    extractor = GeminiStringExtractor(api_key, model, args.budget_limit)

    # Process files
    print(f"📂 Processing files...")
    all_files = [f.strip() for f in args.files.split(' ') if f.strip()]
    print(f"   Total files from input: {len(all_files)}")

    # Check which files exist
    files = []
    for f in all_files:
        if os.path.exists(f):
            files.append(f)
            print(f"   ✅ {f}")
        else:
            print(f"   ❌ {f} (not found)")

    if not files:
        print("❌ No valid files found to process")
        with open(os.environ.get('GITHUB_OUTPUT', '/dev/null'), 'a') as f:
            f.write("strings-found=false\n")
        exit(1)

    print(f"\n🚀 Starting extraction on {len(files)} file(s)...\n")

    start_time = time.time()
    analyses, summary = extractor.process_files(files, debug=args.debug)
    processing_time = time.time() - start_time

    if not analyses:
        print("❌ No strings extracted or processing failed")
        print(f"   Files processed: {summary.get('files_processed', 0)}")
        print(f"   Strings found: {summary.get('total_strings', 0)}")
        with open(os.environ.get('GITHUB_OUTPUT', '/dev/null'), 'a') as f:
            f.write("strings-found=false\n")
        return

    # Save results for other scripts
    processed_file = os.path.join(args.output_dir, 'processed-strings.json')
    all_strings = []
    for analysis in analyses:
        all_strings.extend([
            {
                'original_string': s.text,
                'semantic_key': s.semantic_key,
                'file_path': s.file_path,
                'context': s.context,
                'line_number': s.line_number,
                'needs_import': s.needs_import
            }
            for s in analysis.strings
        ])

    with open(processed_file, 'w', encoding='utf-8') as f:
        json.dump(all_strings, f, indent=2, ensure_ascii=False)

    # Set GitHub outputs
    strings_count = len(all_strings)
    cost_summary = summary['cost_summary']

    with open(os.environ['GITHUB_OUTPUT'], 'a') as f:
        f.write(f"strings-found=true\n")
        f.write(f"strings-count={strings_count}\n")
        f.write(f"estimated-cost=${summary['total_cost']:.4f}\n")
        f.write(f"processing-time={processing_time:.2f}\n")
        f.write(f"api-calls={cost_summary['api_calls']}\n")
        f.write(f"tokens-used={cost_summary['tokens_used']}\n")
        f.write(f"cost-tracking=Budget: ${args.budget_limit:.2f}, Used: ${cost_summary['current_cost']:.4f}, Remaining: ${cost_summary['remaining_budget']:.2f}\n")
        f.write(f"remaining-budget={cost_summary['remaining_budget']:.2f}\n")

    print(f"✅ Successfully processed {strings_count} strings from {len(analyses)} files")
    print(f"💰 Total cost: ${summary['total_cost']:.4f}")
    print(f"⏱️ Processing time: {processing_time:.2f}s")

if __name__ == '__main__':
    main()
