@echo off
REM Clean Video Downloader with full debugging

if "%1"=="" (
    echo.
    echo VS Code MP4 Video Downloader v1.0
    echo ===================================
    echo.
    echo Usage:
    echo   download-videos-clean.bat ^<URL^> [output-dir]
    echo.
    echo Examples:
    echo   download-videos-clean.bat "https://code.visualstudio.com/docs" "./videos"
    echo.
    echo Debug Mode (see what videos are found before downloading):
    echo   download-videos-clean.bat "https://code.visualstudio.com/docs" "./videos" --debug
    echo.
    goto :end
)

setlocal enabledelayedexpansion

set "URL=%1"
set "OUTPUT_DIR=%2"
if "!OUTPUT_DIR!"=="" set "OUTPUT_DIR=.\vscode-videos"

echo.
echo Checking JAR file...
if not exist "target\media-downloader-1.0.0.jar" (
    echo ERROR: JAR file not found at target\media-downloader-1.0.0.jar
    echo Please run: mvn clean package
    goto :end
)

echo JAR found: target\media-downloader-1.0.0.jar
echo.

echo Starting Video Download:
echo  URL: !URL!
echo  Output: !OUTPUT_DIR!
echo.

java -cp target/media-downloader-1.0.0.jar com.media.VSCodeDocumentDownloader "!URL!" "!OUTPUT_DIR!" --videos %3 %4

:end
echo.
pause
