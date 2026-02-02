#!/usr/bin/env python3
"""
Update string resource files with new strings extracted by LLM.
"""

import json
import os
import xml.etree.ElementTree as ET
from typing import Dict, List
from pathlib import Path

class ResourceUpdater:
    def __init__(self, base_path: str):
        self.base_path = base_path
        self.strings_file = os.path.join(base_path, 'values', 'strings.xml')
        self.existing_strings = {}
        self.load_existing_strings()
        
    def load_existing_strings(self):
        """Load existing strings to avoid duplicates."""
        try:
            tree = ET.parse(self.strings_file)
            root = tree.getroot()
            
            for string_elem in root.findall('string'):
                key = string_elem.get('name')
                text = string_elem.text or ''
                self.existing_strings[key] = text
                
            print(f"📖 Loaded {len(self.existing_strings)} existing strings")
        except Exception as e:
            print(f"⚠️ Could not load existing strings: {e}")
    
    def update_base_strings(self, new_strings: List[Dict]) -> bool:
        """Update main strings.xml file with new strings."""
        if not new_strings:
            print("ℹ️ No new strings to add")
            return False
        
        # Load existing XML
        try:
            tree = ET.parse(self.strings_file)
            root = tree.getroot()
        except:
            root = ET.Element('resources')
        
        # Track added/updated strings
        added_count = 0
        updated_count = 0
        
        for string_info in new_strings:
            key = string_info['semantic_key']
            text = string_info['original_string']
            
            # Skip if already exists with same text
            if key in self.existing_strings:
                if self.existing_strings[key] == text:
                    continue  # Exact match, no update needed
                else:
                    # Update for conflict resolution
                    existing_elem = root.find(f".//string[@name='{key}']")
                    if existing_elem is not None:
                        existing_elem.text = text
                        updated_count += 1
            else:
                # Add new string
                string_elem = ET.SubElement(root, 'string')
                string_elem.set('name', key)
                string_elem.text = text
                added_count += 1
            
            # Track in existing
            self.existing_strings[key] = text
        
        # Sort strings alphabetically by key for consistency
        strings = root.findall('string')
        strings.sort(key=lambda x: x.get('name', ''))
        
        # Create new root with sorted strings
        new_root = ET.Element('resources')
        for string_elem in strings:
            new_root.append(string_elem)
        
        # Save updated XML
        os.makedirs(os.path.dirname(self.strings_file), exist_ok=True)
        tree = ET.ElementTree(new_root)
        tree.write(self.strings_file, encoding='utf-8', xml_declaration=True)
        
        print(f"✅ Added {added_count} new strings, updated {updated_count} existing strings")
        return added_count > 0 or updated_count > 0

def main():
    import argparse
    
    parser = argparse.ArgumentParser(description='Update string resource files')
    parser.add_argument('--input', required=True, help='JSON file with processed strings')
    parser.add_argument('--base-path', required=True, help='Base path for composeResources')
    parser.add_argument('--output', help='Output directory (default: base-path)')
    
    args = parser.parse_args()
    
    # Configuration
    output_dir = args.output or args.base_path
    
    # Load processed strings
    with open(args.input, 'r', encoding='utf-8') as f:
        processed_strings = json.load(f)
    
    # Update resources
    updater = ResourceUpdater(args.base_path)
    modified = updater.update_base_strings(processed_strings)
    
    # Set GitHub Actions output
    files_modified = 1 if modified else 0
    with open(os.environ['GITHUB_OUTPUT'], 'a') as f:
        f.write(f"files-modified={files_modified}\n")
    
    print(f"✅ Resource update completed. Files modified: {files_modified}")

if __name__ == '__main__':
    main()