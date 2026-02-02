#!/usr/bin/env python3
"""
Translation service using LibreTranslate API for string localization.
Handles multiple languages with rate limiting and error recovery.
"""

import json
import os
import time
import xml.etree.ElementTree as ET
from typing import Dict, List, Set, Optional, Any
from pathlib import Path
import requests

# Language mapping for LibreTranslate
LANGUAGE_MAPPING = {
    'es': 'es',  # Spanish
    'pt': 'pt',  # Portuguese
    'fr': 'fr',  # French
    'de': 'de',  # German
    'ja': 'ja',  # Japanese
    'zh': 'zh',  # Chinese
    'ar': 'ar',  # Arabic
    'ru': 'ru',  # Russian
}

# RTL languages (Right-to-Left)
RTL_LANGUAGES = {'ar', 'he', 'fa', 'ur'}

class LibreTranslateClient:
    def __init__(self, api_url: str, api_key: Optional[str] = None, rate_limit_delay: float = 0.5):
        self.api_url = api_url
        self.api_key = api_key
        self.rate_limit_delay = rate_limit_delay
        self.session = requests.Session()
        
    def translate_text(self, text: str, target_lang: str, source_lang: str = 'en') -> str:
        """Translate a single text string with error handling."""
        if not text or not text.strip():
            return text
        
        data = {
            'q': text,
            'source': source_lang,
            'target': target_lang,
            'format': 'text'
        }
        
        if self.api_key:
            data['api_key'] = self.api_key or ''
        
        try:
            response = self.session.post(
                f"{self.api_url}/translate",
                json=data,
                headers={'Content-Type': 'application/json'},
                timeout=30
            )
            response.raise_for_status()
            
            result = response.json()
            return result.get('translatedText', text or '')
            
        except requests.exceptions.RequestException as e:
            print(f"⚠️ Translation failed for '{text[:50]}...': {e}")
            return text  # Return original text on failure
        except Exception as e:
            print(f"⚠️ Unexpected error translating '{text[:50]}...': {e}")
            return text
    
    def translate_multiple(self, texts: List[str], target_lang: str, source_lang: str = 'en') -> List[str]:
        """Translate multiple texts with rate limiting."""
        results = []
        
        for text in texts:
            translated = self.translate_text(text, target_lang, source_lang)
            results.append(translated)
            time.sleep(self.rate_limit_delay)  # Rate limiting
        
        return results

class StringTranslator:
    def __init__(self, api_url: str, api_key: str = None):
        self.client = LibreTranslateClient(api_url, api_key)
        
    def parse_strings_xml(self, xml_file: str) -> Dict[str, str]:
        """Parse strings XML file and return key-value pairs."""
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
    
    def create_strings_xml(self, strings: Dict[str, str], target_lang: str) -> ET.Element:
        """Create a strings XML element for target language."""
        root = ET.Element('resources')
        
        # Sort keys alphabetically for consistency
        for key in sorted(strings.keys()):
            string_elem = ET.SubElement(root, 'string')
            string_elem.set('name', key)
            string_elem.text = strings[key]
        
        return root
    
    def ensure_language_directory(self, output_dir: str, lang_code: str) -> str:
        """Ensure language-specific directory exists."""
        lang_dir = os.path.join(output_dir, f'values-{lang_code}')
        os.makedirs(lang_dir, exist_ok=True)
        return lang_dir
    
    def translate_strings_file(self, input_file: str, languages: List[str], output_dir: str) -> Dict[str, any]:
        """Translate strings XML to multiple languages."""
        
        # Validate languages exist in mapping
        valid_languages = [lang for lang in languages if lang in LANGUAGE_MAPPING]
        if not valid_languages:
            return {'error': 'No valid languages to translate to'}
        
        # Parse base strings
        strings = self.parse_strings_xml(input_file)
        if not strings:
            return {'error': 'No strings found in input file'}
        
        print(f"🌍 Translating {len(strings)} strings to {len(valid_languages)} languages")
        
        results = {
            'translated_languages': [],
            'total_strings': len(strings),
            'failed_translations': {}
        }
        
        # Process each language
        for lang in valid_languages:
            print(f"🔄 Translating to {lang}...")
            
            target_lang = LANGUAGE_MAPPING[lang]
            texts_to_translate = list(strings.values())
            
            # Translate all strings
            translated_texts = self.client.translate_multiple(texts_to_translate, target_lang)
            translated_strings = dict(zip(strings.keys(), translated_texts))
            
            # Create and write XML
            root = self.create_strings_xml(translated_strings, lang)
            lang_dir = self.ensure_language_directory(output_dir, lang)
            output_file = os.path.join(lang_dir, f'strings-{lang}.xml')
            
            try:
                tree = ET.ElementTree(root)
                tree.write(output_file, encoding='utf-8', xml_declaration=True)
                results['translated_languages'].append(lang)
                print(f"✅ Completed {lang} translation ({len(strings)} strings)")
            except Exception as e:
                print(f"❌ Failed to save {lang} translations: {e}")
                results['failed_translations'][lang] = str(e)
        
        return results

def main():
    import argparse
    
    parser = argparse.ArgumentParser(description='Translate strings using LibreTranslate')
    parser.add_argument('--input', required=True, help='Input strings XML file')
    parser.add_argument('--languages', required=True, help='Comma-separated list of language codes')
    parser.add_argument('--output', required=True, help='Output directory for translated files')
    parser.add_argument('--api-key', help='LibreTranslate API key (optional for free tier)')
    
    args = parser.parse_args()
    
    # Configuration
    api_url = os.environ.get('LIBRETRANSLATE_API_URL', 'https://libretranslate.com/translate')
    api_key = args.api_key or os.environ.get('LIBRETRANSLATE_API_KEY')
    
    # Initialize translator
    translator = StringTranslator(api_url, api_key)
    
    # Parse languages
    languages = [lang.strip() for lang in args.languages.split(',')]
    languages = [lang for lang in languages if lang]  # Remove empty strings
    
    # Ensure output directory
    os.makedirs(args.output, exist_ok=True)
    
    # Start translation
    start_time = time.time()
    result = translator.translate_strings_file(args.input, languages, args.output)
    total_time = time.time() - start_time
    
    # Handle errors
    if 'error' in result:
        print(f"❌ Translation failed: {result['error']}")
        exit(1)
    
    # Report results
    successful_count = len(result['translated_languages'])
    failed_count = len(result.get('failed_translations', {}))
    
    print(f"\n📊 Translation Summary:")
    print(f"  ✅ Successful: {successful_count} languages")
    print(f"  ❌ Failed: {failed_count} languages")
    print(f"  📝 Total strings: {result['total_strings']}")
    
    if result.get('failed_translations'):
        print(f"\n⚠️ Failed translations:")
        for lang, error in result['failed_translations'].items():
            print(f"  - {lang}: {error}")
    
    # Set GitHub Actions output
    if successful_count > 0:
        languages_joined = ','.join(result['translated_languages'])
        with open(os.environ['GITHUB_OUTPUT'], 'a') as f:
            f.write(f"languages-updated={languages_joined}\n")
    
    print(f"✅ Translation completed")

if __name__ == '__main__':
    main()