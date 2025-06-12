const {remote} = require('webdriverio');

const capabilities = {
  platformName: 'Android',
  'appium:automationName': 'UiAutomator2',
  'appium:deviceName': 'Android',
  'appium:appPackage': 'com.lift.bro',
  'appium:appActivity': '.android.MainActivity',
};

const wdOpts = {
  hostname: process.env.APPIUM_HOST || 'localhost',
  port: parseInt(process.env.APPIUM_PORT, 10) || 4723,
  logLevel: 'info',
  capabilities,
};

async function runTest() {
  const driver = await remote(wdOpts);
    const batteryItem = await driver.$('//*[@text="Leo"]');
    await batteryItem.click();
    const broItem = await driver.$('//*[@text="Continue"]');
    await broItem.click();
    const coachItem = await driver.$('#SKIP_ONBOARDING');
    await coachItem.waitForDisplayed({ timeout: 5000 })
    await coachItem.click();
}

runTest().catch(console.error);