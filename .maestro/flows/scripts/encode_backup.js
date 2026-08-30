// .maestro/flows/scripts/encode_backup.js
// Reads the generated backup JSON and builds a deep link URL for Maestro.
// Emits output.restoreUrl = "liftbro://restore?data=<base64>".
const fs = require('fs');
const path = require('path');

const root = env.PROJECT_ROOT || process.cwd();
const backupPath = path.resolve(root, 'build/generated/test-data/screenshot_test_backup.json');
const backup = fs.readFileSync(backupPath, 'utf8');
const encoded = Buffer.from(backup, 'utf8').toString('base64');
output.restoreUrl = 'liftbro://restore?data=' + encoded;
