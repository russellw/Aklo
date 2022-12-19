del aklo\*.class
javac -Xlint:unchecked -d . -g C:\aklo\src\main\java\aklo\*.java
if errorlevel 1 goto :eof

java -Xmx1g -ea aklo/Main %*
