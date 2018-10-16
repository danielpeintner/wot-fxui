# wot-fxui


## Building & Running

### Executable

```
mvn jfx:native
```

### Desktop

```
gradle -b jfxmobile.gradle run
```

### Android



```
gradle -b jfxmobile.gradle android /* generates an apk that is signed with a debug keystore and put it in the directorybuild/javafxports/android */
gradle -b jfxmobile.gradle androidRelease /* generates an apk that is signed with the configured signingConfig and put it in the directorybuild/javafxports/android  */
gradle -b jfxmobile.gradle androidInstall /* installs the generated debug apk onto a device that is connected to your desktop  */
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
