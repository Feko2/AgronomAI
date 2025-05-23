@echo off
echo Preparing clean environment...

REM Kill any running node processes
taskkill /F /IM node.exe > nul 2>&1
taskkill /F /IM java.exe > nul 2>&1
taskkill /F /IM npm.exe > nul 2>&1

REM Allow a moment for processes to close
timeout /t 3 > nul

REM Clean previous backend build (with error handling)
echo Cleaning backend...
if exist target (
  rmdir /s /q target 2>nul || echo Failed to remove target directory, continuing...
)

REM Clean previous frontend build and node_modules (with error handling)
echo Cleaning frontend...
if exist frontend\dist (
  rmdir /s /q frontend\dist 2>nul || echo Failed to remove dist directory, continuing...
)

REM Simple approach to cleanup frontend modules
echo Forcing cleanup of frontend folders...
if exist frontend\node_modules (
  echo Removing node_modules folder...
  rmdir /s /q frontend\node_modules 2>nul
)

REM Clean node_modules only if needed - this can be slow
if exist frontend\package-lock.json (
  echo Installing frontend dependencies...
  cd frontend
  call npm ci --no-fund --loglevel=error
  cd ..
) else (
  if exist frontend\node_modules (
    echo Frontend dependencies found, skipping reinstall...
  ) else (
    echo Installing frontend dependencies...
    cd frontend
    call npm install --no-fund --loglevel=error
    cd ..
  )
)

REM Check for .env file
if not exist .env (
  echo WARNING: .env file not found!
  echo Please copy env.example to .env and configure your database credentials.
  echo Example: copy env.example .env
  pause
  exit /b 1
)

REM Check which environment to use
set ENV=prod
if not "%1"=="" (
  if "%1"=="dev" (
    set ENV=dev
    echo Running in DEVELOPMENT mode with H2 Database
  ) else (
    echo Running in PRODUCTION mode with Oracle Cloud DB
  )
) else (
  echo Running in PRODUCTION mode with Oracle Cloud DB
  echo Use "run-all.bat dev" for H2 development mode
)

REM Start Spring Boot backend
echo Starting backend...
if "%ENV%"=="prod" (
  echo Using Oracle Cloud Database with .env configuration
  start cmd /k "cd /d %~dp0 && mvn clean spring-boot:run"
) else (
  echo Using H2 In-Memory Database
  start cmd /k "cd /d %~dp0 && mvn clean spring-boot:run -Dspring-boot.run.profiles=dev"
)

REM Wait a bit to let backend start
echo Waiting for backend to start...
timeout /t 25 > nul

REM Start React frontend
echo Starting frontend...
cd frontend
start cmd /k "npm run dev"
cd ..

echo All systems started! Open http://localhost:5173 in your browser.
echo.
if "%ENV%"=="prod" (
  echo Using Oracle Cloud Database (configured via .env file)
) else (
  echo Using H2 In-Memory Database
)
echo.
echo Backend: http://localhost:8080
echo Frontend: http://localhost:5173
echo Health Check: http://localhost:8080/actuator/health 