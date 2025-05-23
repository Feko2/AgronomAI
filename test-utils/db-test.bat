@echo off
echo === Simple Database Connection Test ===

REM Make sure dependencies are available
if not exist target\dependency mkdir target\dependency
if not exist target\dependency\ojdbc8.jar (
  echo Copying JDBC driver...
  call mvn dependency:copy-dependencies -DoutputDirectory=target/dependency -Dsilent=true
)

REM Compile the test class
echo Compiling SimpleDbTest.java...
javac -cp "./target/dependency/*;./target/classes;." SimpleDbTest.java

REM Run the test
echo Running test...
java -cp "./target/dependency/*;./target/classes;." -Djava.util.logging.config.file=src/main/resources/logging.properties SimpleDbTest

echo.
echo Press any key to exit...
pause > nul 