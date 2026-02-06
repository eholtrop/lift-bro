#!/usr/bin/env python3
"""
Update Kotlin files with stringResource imports and references.
"""

import json
import os
import re
from typing import Dict, List, Set
from pathlib import Path

class KotlinFileUpdater:
    def __init__(self):
        self.string_pattern = re.compile(r'"([^"]*?)"')
        self.resource_import_pattern = re.compile(r'import\s+lift_bro\.core\.generated\.resources\.Res')
        self.string_resource_pattern = re.compile(r'import\s+lift_bro\.core\.generated\.resources\.\w+')

    def update_file(self, file_path: str, strings_to_update: List[Dict]) -> bool:
        """Update a single Kotlin file with string resources."""
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()

            original_content = content

            # Group strings by file for this specific file
            file_strings = [s for s in strings_to_update if s['file_path'] == file_path]

            if not file_strings:
                return False

            print(f"📝 Updating {file_path} with {len(file_strings)} strings")

            # Update each string reference
            for string_info in sorted(file_strings, key=lambda x: x['line_number'], reverse=True):
                old_string = string_info['original_string']
                new_key = string_info['semantic_key']

                # Replace string literal with resource reference
                old_pattern = re.escape(f'"{old_string}"')
                new_reference = f'stringResource(Res.string.{new_key})'

                print(f'replacing {old_pattern} with {new_reference}')
                # Use regex for precise replacement
                content = re.sub(
                    fr'"{re.escape(old_string)}"',
                    new_reference,
                    content,
                    count=1  # Only replace first occurrence to avoid duplicates
                )

            # Add necessary imports
            if any(s.get('needs_import', True) for s in file_strings):
                print('adding imports')
                content = self.add_required_imports(content, file_strings)

            # Write back if changed
            if content != original_content:
                with open(file_path, 'w', encoding='utf-8') as f:
                    f.write(content)
                return True

        except Exception as e:
            print(f"❌ Error updating {file_path}: {e}")

        return False

    def add_required_imports(self, content: str, strings: List[Dict]) -> str:
        """Add required import statements if not present."""
        lines = content.split('\n')

        # Check if imports are already present
        has_res_import = bool(self.resource_import_pattern.search(content))
        has_string_resource_import = 'import org.jetbrains.compose.resources.stringResource' in content

        # Collect required string imports
        required_string_imports = set()
        for string_info in strings:
            if string_info.get('needs_import', True):
                key = string_info['semantic_key']
                import_statement = f'import lift_bro.core.generated.resources.{key}'
                if import_statement not in content:
                    required_string_imports.add(import_statement)

        if has_res_import and has_string_resource_import and not required_string_imports:
            return content  # No imports needed

        # Find insertion point (after package declaration)
        insert_index = 0
        package_found = False

        for i, line in enumerate(lines):
            if line.strip().startswith('package '):
                package_found = True
                insert_index = i + 1
            elif package_found and (line.strip() == '' or line.startswith('import ')):
                insert_index = i + 1

        # Add imports
        imports_to_add = []

        if not has_res_import:
            imports_to_add.append('import lift_bro.core.generated.resources.Res')

        if not has_string_resource_import:
            imports_to_add.append('import org.jetbrains.compose.resources.stringResource')

        print(f"{required_string_imports}")
        # Add specific string imports
        imports_to_add.extend(sorted(required_string_imports))

        print(f"{imports_to_add}")
        # Insert imports
        for i, import_line in enumerate(imports_to_add):
            lines.insert(insert_index + i, import_line)

        # Add blank line if needed
        if insert_index < len(lines) and lines[insert_index + len(imports_to_add)].strip() != '':
            lines.insert(insert_index + len(imports_to_add), '')

        return '\n'.join(lines)

def main():
    import argparse

    parser = argparse.ArgumentParser(description='Update Kotlin files with string resources')
    parser.add_argument('--input', required=True, help='JSON file with processed strings')
    parser.add_argument('--base-path', required=True, help='Base path for Kotlin files')

    args = parser.parse_args()

    # Load processed strings
    with open(args.input, 'r', encoding='utf-8') as f:
        processed_strings = json.load(f)

    # Update files
    updater = KotlinFileUpdater()

    # Group strings by file
    files_to_update = {}
    for string_info in processed_strings:
        file_path = string_info['file_path']
        if file_path not in files_to_update:
            files_to_update[file_path] = []
        files_to_update[file_path].append(string_info)

    # Update each file
    updated_files = 0
    for file_path, file_strings in files_to_update.items():
        if updater.update_file(file_path, file_strings):
            updated_files += 1

    print(f"✅ Updated {updated_files} Kotlin files")

if __name__ == '__main__':
    main()
