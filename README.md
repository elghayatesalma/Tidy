# Tidy Documentation

## User documentation
### Description
Tidy is a shared household management app that is targeted at college students and working young adults who share a household together. Typically, college students/young adults can be put into awkward situations when attempting to confront a roommate about completing their chores so this is where Tidy would help and act as a third-party that keeps everyone on track. It helps in establishing household standards/guidelines and maintaining accountability among housemates by providing an organizational structure for tracking their tasks that lends itself to meeting these goals. A lack of organization leads to a plethora of problems and here a few that we are trying to solve, chores are divided agreeably but not equally, agreeing upon standards and guidelines for clean living is difficult to keep consistent, and combinations of tools not meant for managing a household are lacking.
This file will help you go through the project whether you are a user or developer.

### Installation
Install the app from the Google Play Store (not yet published).

#### Beta Releases:
Download and install the APK file from our Github Releases page: [Releases](https://github.com/elghayatesalma/Tidy/releases)

#### Usage
If you received an invite link from another Tidy user, click on this link to open the app and automatically be added to the user’s shared household.

Otherwise, open the app from your home screen to set up your own new household.

#### Sign-In
You will be greeted by the Tidy sign-in screen with a Google Sign-In button at the bottom
Press the Google Sign-In button and select a Google Account you want to use for the app.
If sign-in was successful, you will see a “Continue” button - press it.

#### Tidy Household
You will now see a list of `All Chores` in your household.
Press on the “Add Chore” button to create a new chore.
- Fill out the Title, Description, and Priority fields
- The Priority field determines how important a task is by decreasing priority. i.e. priority = 1 is the most important task
Delete a task by long-pressing on it and confirming (not implemented yet).

Swiping left will take you to the `My Chores` pane where you can view which chores have been assigned to you by the app. (not implemented yet)

#### User Profile
Pressing the `User Profile` button in the top right corner will take you to the User Profile.

Here you can view and edit your profile information.
There is a list of re-orderable chore preferences on this screen. Preferences can be re-ordered by dragging items up and down the list. Chores will automatically be assigned to users based on their preferences.

##### Inviting Users to Household
In the User Profile screen, there is a Share button in the top right corner.
Press it to get an invitation to your household which can be shared with others.

The invitation is in the form of a URL - if the app is not installed it will take you to the Google Play Store page. If the app is installed, it will open the app and add the user to your household rather than creating a new one. It will currently only work on Android devices.

### Bug report
Bug reporting for Tidy follows these guidelines below.
[How to write a solid bug report for Tidy](https://developer.mozilla.org/en-US/docs/Mozilla/QA/Bug_writing_guidelines)
### To view current and resolved bugs
We are using Github issues to manage bugs. 
To view current and past bugs, follow this [URL] (https://github.com/elghayatesalma/Tidy/issues)
You need to join the project first to create, modify and resolve bugs. 

## Developer documentation

### Required Tools
These are the tools that you will need, click the links to download and install.
- For Java development:[Java 7 JDK or better](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
- For Android development: [Android Studio](https://developer.android.com/sdk/index.html)
- Setup Android API 29 in Android Studio.
- Setup an Android device or Android emulator (API 29) in Android Studio’s AVD if you intend to run the integration tests.
- For running Firebase integration tests: [Firebase CLI](https://firebase.google.com/docs/cli#setup_update_cli)

### Get the Code
Clone this repository to a location where you'd like the code to live:
- `git clone https://github.com/elghayatesalma/Tidy`
- This can also be done directly through Android Studio, using version control 
- Import the Android project to Android Studio (to make sure the project is imported, check the source files in the project explorer).

### Directory Structure
- Tidy/app: Folder for the actual app.
	-Tidy/app/src/main: Folder for the main source
		-Tidy/app/src/main/AndroidManifest.xml: Manifest file
		-Tidy/app/src/main/java/cse403/sp2020/tidy/: Folder for main java files
-- README.md -File containing useful instructions for how to get started on the project.
-- gradlew - gradle wrapper script for Linux and macOS systems
-- gradlew.bat - gradle wrapper for Windows systems

### Configuring Firebase
You will need to configure the project in order for your builds to communicate with Firebase servers. You will need the google-services.json file from your Firebase project - this contains the private keys used to communicate with Firebase servers. You will also need to add the signing certificate’s SHA1 that you use for building the app to the Firebase project. This is used to verify that the app was built by authorized developers. 

#### Use our Firebase project
We can add SHA1’s and provide google-services JSON file from our project to developers per request. Please email one of the Tidy developers so we can add you to our Firebase project.

Alternatively, you can also create and use your own Firebase project by following these instructions:

#### Create a Firebase project
- Go to [https://console.firebase.google.com/](https://console.firebase.google.com/) and click “Add Project” to start creating a new project.
- Set the package name in the Firebase project console to the Android package name, `cse403.sp2020.tidy`, found in AndroidManifest.xml and the app module’s build.gradle.

#### Add google-services.json to Tidy source tree
- Go to the Firebase console website for your project and navigate to the settings page.
- Download the google-services.json file from the “Download the latest config file section” under the General pane. (or download the one we provide per developer request)
- Place the downloaded google-services.json file into the 'app/' directory.

#### Add SHA1 to Firebase
- Get your signing certificate’s SHA1. This can be retrieved by running the `signingReport` Gradle task. This can be done from the CLI with `./gradlew signingReport` or by navigating to the gradle menu in Android Studio and running the signingReport task there.
- There are separate certificates for debug and release builds - you will need to add the SHA1’s for whichever type of build you want to run.
- In the Firebase console General Settings pane under the JSON file download section, find the SHA certificate fingerprints section and click 'Add fingerprint'. 
- Add your SHA1 key there (or email it to the Tidy developers so we can add it to our project) 

### Testing
Android studio provides gradle wrappers `gradlew` and `gradlew.bat` for Linux/macOS and Windows systems. These wrap the same gradle build tasks and allow the project to be built from multiple platforms. Substitute `gradlew` for the version appropriate to your system.

Our Unit tests and Integration tests are separate targets in our gradle build system. The unit tests run as part of the build whereas integration tests require an Android device be available to deploy and run the tests on.

#### Unit tests
##### CLI
- `./gradlew`, the default task, builds the project and runs the unit tests
- `./gradlew check` will only run the unit tests (it is a subtask run by `gradlew`)

#### Integration Tests
For the backend/ModelInterface tests to work, the Firebase CLI needs to be installed (see above) in order to run the Firestore Emulator.
- Run the emulator with ‘firebase emulators:start --only firestore’
- Integration tests should now work on the emulator, restart the Firestore emulator between tests to clear data (tests will fail otherwise)

An Android device on API 29 or higher needs to be available via ADB as well. Configure a physical device or Android emulator and make sure that it is running/available over ADB in Android Studio.

##### CLI
- `./gradlew connectedCheck` runs the integration tests on a connected Android device

#### Android Studio Testing
Run all tests from Android Studio (ensure Android device and Firebase emulators are available):
- Run -> Run all tests

### Building
#### Debug Builds
##### CLI
Compile
 - ./gradlew

Generate installable APK:  
- ./gradlew assembleDebug

#### Android Studio
Compile
- Build -> make project

Generate installable APK:  
- Build -> Build Bundle(s) / APK(s) -> Build APK(s)

#### Release Builds
You will first need to configure a release signing certificate to build a release version of the app. We will be using Android Studio’s Google Play Store integration tool to manage publishing to Google Play and signing release builds. This option will auto-generate a release certificate when we publish our app to Google Play. The SHA1 of the release certificate will then need to be added to the Firebase project. The SHA1 can be found on the Google Play Console page for the app. Another option is to manually generate and manage the release certificate, in which case the SHA1 can be retrieved by using the `gradlew signingReport`. For more information about either option, see the Android developer documentation on [app signing](https://developer.android.com/studio/publish/app-signing).

##### Release Versioning
Update the version number in the versionName field in app/build.gradle

##### CLI
Generate release APK:  
- ./gradlew assembleRelease

#### Android Studio
Generate release APK
- Build -> Generate Signed Bundle / APK -> APK

Generate app bundle for publishing to Google Play
- Build -> Generate Signed Bundle / APK -> Android App Bundle

All build APK files will be located in `<project_repo>/app/build/outputs/apk/<release_type>/<release_type>.apk`
