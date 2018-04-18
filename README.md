# wot-fxui


## Building & Running

### Executable

```
mvn jfx:native
```

### Desktop

```
gradle run
```

### Android

```
gradle android /* generates an apk that is signed with a debug keystore and put it in the directorybuild/javafxports/android */
gradle androidRelease /* generates an apk that is signed with the configured signingConfig and put it in the directorybuild/javafxports/android  */
gradle androidInstall /* installs the generated debug apk onto a device that is connected to your desktop  */
```

### Web Browser

#### Start in foreground (development mode) ###

```
./gradlew jproRun
```


#### Start in background (server mode) ###

```
./gradlew jproRestart
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
