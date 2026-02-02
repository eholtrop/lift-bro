#!/usr/bin/env python3
"""
Validate changes made by string extraction and localization workflow.
"""

import json
import os
import xml.etree.ElementTree as ET
from pathlib import Path
from typing import Dict, List

class ChangeValidator:
    def __init__(self, base_path: str):
        self.base_path = base_path
        self.errors = []
        self.warnings = []
        
    def validate_string_resources(self, strings_file: str) -> bool:
        """Validate strings.xml file for consistency."""
        try:
            tree = ET.parse(strings_file)
            root = tree.getroot()
            
            strings = root.findall('string')
            keys = set()
            
            for string_elem in strings:
                key = string_elem.get('name')
                text = string_elem.text or ''
                
                # Check for duplicates
                if key in keys:
                    self.errors.append(f"Duplicate key: {key}")
                else:
                    keys.add(key)
                
                # Validate key is not empty
                if not key or not key.strip():
                    self.errors.append("Empty or invalid key found")
                
                # Warn about empty strings
                if not text.strip():
                    self.warnings.append(f"Empty string for key: {key}")
            
            print(f"🔍 Validated {len(strings)} strings in {Path(strings_file).name}")
            return len(self.errors) == 0
            
        except Exception as e:
            self.errors.append(f"Failed to parse {strings_file}: {e}")
            return False
    
    def get_validation_report(self) -> Dict:
        """Get validation report."""
        return {
            'errors': self.errors,
            'warnings': self.warnings,
            'error_count': len(self.errors),
            'warning_count': len(self.warnings),
            'is_valid': len(self.errors) == 0
        }

def main():
    import argparse
    
    parser = argparse.ArgumentParser(description='Validate string extraction changes')
    parser.add_argument('--base-path', required=True, help='Base path for composeResources')
    parser.add_argument('--extracted-strings', help='Extracted strings JSON file (unused, for compatibility)')
    
    args = parser.parse_args()
    
    validator = ChangeValidator(args.base_path)
    
    # Validate base strings file
    strings_file = os.path.join(args.base_path, 'values', 'strings.xml')
    validator.validate_string_resources(strings_file)
    
    # Generate report
    report = validator.get_validation_report()
    
    print(f"\n🔍 Validation Report:")
    print(f"  ✅ Valid: {report['is_valid']}")
    print(f"  ❌ Errors: {report['error_count']}")
    print(f"  ⚠️ Warnings: {report['warning_count']}")
    
    if report['errors']:
        print(f"\n❌ Validation Errors:")
        for error in report['errors']:
            print(f"  - {error}")
        exit(1)
    
    if report['warnings']:
        print(f"\n⚠️ Validation Warnings:")
        for warning in report['warnings']:
            print(f"  - {warning}")
    
    print(f"✅ Validation completed successfully")

if __name__ == '__main__':
    main()