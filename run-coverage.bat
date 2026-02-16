@echo off
REM Script to run tests and generate JaCoCo coverage report
REM Then open the HTML report in the default browser

echo ============================================
echo Running Tests with JaCoCo Coverage
echo ============================================
echo.

call .\mvnw.cmd clean test

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ============================================
    echo Tests Failed! Please check the errors above.
    echo ============================================
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo ============================================
echo Tests Completed Successfully!
echo ============================================
echo.

REM Check if the coverage report was generated
if exist "target\site\jacoco\index.html" (
    echo ============================================
    echo Opening Coverage Report in Browser...
    echo ============================================
    echo.
    echo Report Location: target\site\jacoco\index.html
    echo.
    start "" "target\site\jacoco\index.html"
    echo.
    echo Coverage report opened in your default browser.
) else (
    echo ============================================
    echo Warning: Coverage report not found!
    echo ============================================
    echo Expected location: target\site\jacoco\index.html
)

echo.
echo ============================================
echo Coverage Summary Location:
echo   HTML: target\site\jacoco\index.html
echo   XML:  target\site\jacoco\jacoco.xml
echo   CSV:  target\site\jacoco\jacoco.csv
echo ============================================
echo.

pause

