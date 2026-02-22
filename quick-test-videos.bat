@echo off
REM ============================================
REM VS Code Video Downloader - Quick Test
REM ============================================
REM This script tests the video download functionality
REM and shows whether videos are being detected

setlocal enabledelayedexpansion

echo.
echo ========================================
echo VS Code Video Downloader - Quick Test v1.0
echo ========================================
echo.

REM Check JAR file
if not exist "target\media-downloader-1.0.0.jar" (
    echo ERROR: JAR file not found
    echo.
    echo Please run: mvn clean package
    echo.
    pause
    exit /b 1
)

REM Create test directory
if exist ".\test-quick-videos" rmdir /s /q ".\test-quick-videos"
mkdir ".\test-quick-videos"

echo.
echo Step 1: Running DEBUG mode to detect videos...
echo (This shows all videos found WITHOUT downloading)
echo.
echo Command:
echo   java -cp target/media-downloader-1.0.0.jar com.media.VSCodeDocumentDownloader ^
echo     "https://code.visualstudio.com/docs" "./test-quick-videos" --videos --debug
echo.
echo Output:
echo ========================================
echo.

java -cp target/media-downloader-1.0.0.jar com.media.VSCodeDocumentDownloader ^
  "https://code.visualstudio.com/docs" "./test-quick-videos" --videos --debug

echo.
echo ========================================
echo.
echo Step 2: Checking if videos were found...
echo.

REM Count directories and files
for /f %%A in ('dir /s /b "test-quick-videos" 2^>nul ^| find /c /v ""') do set COUNT=%%A

if "%COUNT%"=="0" (
    echo RESULT: NO VIDEOS FOUND
    echo ========================
    echo.
    echo This means the tool couldn't find any video links on the VS Code docs pages.
    echo.
    echo Possible reasons:
    echo 1. Network issue - can't access code.visualstudio.com
    echo 2. Page structure changed - VS Code updated their website HTML
    echo 3. Videos are dynamically loaded by JavaScript
    echo.
    echo Troubleshooting:
    echo - Check your internet connection
    echo - Try: curl https://code.visualstudio.com/docs
    echo - Visit https://code.visualstudio.com/docs in your browser
    echo.
) else (
    echo RESULT: VIDEOS DETECTED!
    echo =======================
    echo.
    echo Total items found: %COUNT%
    echo.
    echo This indicates:
    echo - Video detection is working correctly
    echo - You can now proceed with actual download
    echo.
    echo Step 3: To download the videos (REMOVE --debug flag):
    echo.
    echo   java -cp target/media-downloader-1.0.0.jar com.media.VSCodeDocumentDownloader ^
    echo     "https://code.visualstudio.com/docs" "./vscode-videos" --videos
    echo.
    echo This will download all MP4 files to the ./vscode-videos directory
    echo.
)

echo.
pause
