@echo off
echo ========================================
echo        Oracle Connection Test Tool
echo ========================================

REM Verificar que exista el driver de Oracle
if not exist target\dependency mkdir target\dependency
if not exist target\dependency\ojdbc8.jar (
  echo Copiando driver JDBC...
  if not exist target\classes mkdir target\classes
  call mvn dependency:copy-dependencies -DoutputDirectory=target/dependency -Dsilent=true
  if errorlevel 1 (
    echo ERROR: No se pudo copiar las dependencias.
    echo Ejecutando mvn clean install para resolver dependencias...
    call mvn clean install -DskipTests
    call mvn dependency:copy-dependencies -DoutputDirectory=target/dependency -Dsilent=true
  )
)

REM Compilar la clase de prueba
echo.
echo Compilando TestOracleConnection.java...
javac -cp "./target/dependency/*;./target/classes;." TestOracleConnection.java
if errorlevel 1 (
  echo ERROR: No se pudo compilar TestOracleConnection.java
  exit /b 1
)

REM Diferentes passwords a probar
echo.
echo Probando diferentes passwords para el wallet...
set PASSWORDS=welcome1 Welcome1 welcome Welcome Micontrasenasecreta1 password Password oracle Oracle admin Admin ADMIN

for %%p in (%PASSWORDS%) do (
  echo Intentando con password: %%p
  set WALLET_PASSWORD=%%p
  java -cp "./target/dependency/*;./target/classes;." -DTNS_ADMIN=src/main/resources/wallet -DWALLET_PASSWORD=%%p TestOracleConnection
  if not errorlevel 1 goto :success
)

echo.
echo No se pudo conectar con ninguna password conocida.
echo Revise la password que utilizó al descargar el wallet de Oracle Cloud.
goto :end

:success
echo.
echo Conexión exitosa!

:end
echo.
echo Presiona cualquier tecla para salir...
pause > nul 