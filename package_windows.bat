mvn clean package

jlink --add-modules java.base,java.desktop,java.sql ^
      --output runtime ^
      --strip-debug ^
      --compress=2 ^
      --no-header-files ^
      --no-man-pages

jpackage ^
  --name AmazingGame ^
  --input Client/target ^
  --main-jar Client-1.0-SNAPSHOT.jar ^
  --runtime-image runtime ^
  --type app-image

pause

