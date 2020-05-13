# Tidy

Building:

Generate installable APK:
./gradlew assembleDebug

The APK is located

Build From Android Studio:
Import the project into Android Studio through VCS
Build -> make project

To run from Android studio, first configure an Android device to use for running - either a physical device or an emulator.
Then, in Android Studio:
Run -> app


Testing:
Firebase CLI should be running first for backend test cases (see below)
To run the test cases from the CLI:
./gradlew check

Testing from Android Studio:
Run -> Run all tests

Backend Testing Requirements:
In order for the backend/ModelInterface tests to work, the Firebase/Firestore emulator needs to be running. Install and run it with the following:
Get the Firebase CLI from here: https://firebase.google.com/docs/cli#setup_update_cli
Run the emulator with ‘firebase emulators:start --only firestore’
Restart the emulator between tests to clear data
