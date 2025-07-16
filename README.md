# lift-bro
Lift Bro

Lift Bro is a Lift tracking app created as a side project to help me replace my notebook at the Gym.

It is built using Kotlin Multiplatform and Jetpack Compose Multiplatform to allow for shared logic between Android and iOS


# UI Testing

ui tests are run using maestro, to install follow this link

https://docs.maestro.dev/getting-started/installing-maestro

To run the ui tests you can then run it will target whatever currently running/connected device you have
```bash
maestro test .maestro
```

you can also target a specific test instead of you would like
```bash
maestro test .maestry/your_test.yaml
```

