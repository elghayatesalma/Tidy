# Tidy Documentation

## User documentation
What does the system do and why would a user want to use it.
### Installation
These are the tools that you will need, click the links to download and install.
- For Java development:[Java 7 JDK or better](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
- For Android development: [Android Studio](https://developer.android.com/sdk/index.html)
### Steps 
Clone this repository to a location where you'd like the code to live:
`git clone https://github.com/elghayatesalma/Tidy`  
Import the Android project to Android Studio

### Bug report

## Developer documentation

### Directory Structure

### How to Test the Software

### How to build a release of the software




Setup
-----
You will need the get the google-services.json file from the Firebase console, then add your SHA1 (You will need to be authorized to access the console):
- Go to https://console.firebase.google.com/u/0/project/tidy-71f32/settings/general/android:cse403.sp2020.tidy and click the download button for google-services.json. Put this file under the 'app/' directory.
- Click 'Add fingerprint', then add you SHA1 key (you can run './gradlew signingReport' to get it, or go through the Android Studio gradle window)


Building
--------
Generate installable APK:  
- ./gradlew assembleDebug


Build From Android Studio:  
- Import the project into Android Studio through VCS
- Build -> make project


To run from Android studio, first configure an Android device to use for running - either a physical device or an emulator.
Then, in Android Studio:
- Run -> app


Testing
-----------
Firebase CLI should be running first for backend test cases (see below)

To run the test cases from the CLI:
- ./gradlew check

Testing from Android Studio:
- Run -> Run all tests

In order for the backend/ModelInterface tests to work, the Firebase/Firestore emulator needs to be running. Install and run it with the following:
- Get the Firebase CLI from here: https://firebase.google.com/docs/cli#setup_update_cli
- Run the emulator with ‘firebase emulators:start --only firestore’
- Tests should now work on the emulator, restart the emulator between tests to clear data (tests will fail otherwise)
