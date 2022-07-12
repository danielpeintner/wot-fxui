# wot-fxui

## Installation

JavaFXPorts: 
http://docs.gluonhq.com/javafxports/


### Issues & Resolutions

https://stackoverflow.com/questions/42143300/how-can-i-install-android-support-library-to-deploy-a-gluon-mobile-application/42143741#42143741


## Building & Running

### Run

```
mvn jfx:run
```

### Executable

```
mvn jfx:native
```

### Desktop

```
gradlew -b jfxmobile.gradle run
```

### Android



```
gradlew -b jfxmobile.gradle android /* generates an apk that is signed with a debug keystore and put it in the directorybuild/javafxports/android */
gradlew -b jfxmobile.gradle androidRelease /* generates an apk that is signed with the configured signingConfig and put it in the directorybuild/javafxports/android  */
gradlew -b jfxmobile.gradle androidInstall /* installs the generated debug apk onto a device that is connected to your desktop  */
```

### Web Browser

#### Start in foreground (development mode) ###

```
./gradlew -b jpro.gradle jproRun
```


#### Start in background (server mode) ###

```
./gradlew -b jpro.gradle jproRestart
```


#### Open app in Web Browser ###
```
http://localhost:8088/index.html
```

#### Show all jpro apps in Browser ####
```
http://localhost:8088/test/default
```

#### Open app in fullscreen ####
```
http://localhost:8088/test/fullscreen/[app-name]
```
