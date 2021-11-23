# 3S Scanner
####Project developed as part of the "Programming for IoT Applications" class

## Q: In what language was the project written?
#### A: The layouts were written in the descriptive language .XML, the logic was written in kotlin

## Q: What is kotlin?
#### A: Kotlin is a Object Oriented language that originated from Java, its recommended by Google as the "official" android language

## Q: How do I compile the code into an app?
#### A: To compile this app, you need to have the most recent Android Studio IDE installed, clone the project on a folder, then open that folder using that IDE. Once it's done, follow the instructions to "sync the gradle" (project dependencies), then click the "build" tab, select "build bundles" option and finally build APK

## Q: What are the "most important" files?
#### A: The logic can found on the following path: src/main/java/com/example/myapplication
The layouts can be found at: src/main/res/layout
All of them can be openned with any text editor

## Q: How do I install the app?
#### A: The app can be installed on any android device with version 4.2 or higher. You will need to disable the option to only install apps from the play store. Once that's done, use a computer to copy the generated APK to the root of your device. On your device's file manager click on it to install

## Q: Why use an app?
#### A: The app was an alternative to the use of barcode sensors, which would be present in "smart fridges and trashcans"

## Q: How does the app communicate with everything else?
#### A: The app uses a paho MQTT library for android

## Q: What MQTT setting can be changed within the app?
#### A: The user can choose, via the settings menu, the preferred broker and topics, the quality of service is set in the code. 

## Q: Is multithreading present on the app?
#### A: Yes, most of the functions, such as checking if a buttons is pressed, run on background tasks. The android system, together with the kotlin language's functions manage that automatically, as well as determine which tasks should be directed to the efficiency or performance cores/threads

## Q: Does the app comply with the best practices?
#### A: The project mostly complies with the general good practices on coding and implements security resource and battery management features required by Google. However, more complex software architecture patterns, such as modularization and use of MVVM, recommended by Google, weren't applied, given the relative simplicity of the project.

## Q: How can I know if the app is working properly?
#### A: The app uses "toasts" (messages in gray textboxes provided by the device's UI) to let the user know of the connection status, as well as if there is any missing information or if anything fails.
