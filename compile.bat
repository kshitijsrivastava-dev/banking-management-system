@echo off
if not exist out mkdir out

dir s b src.java  sources.txt

javac -cp lib -d out @sources.txt

del sources.txt

if %errorlevel% neq 0 (
    echo.
    echo Compilation failed.
    pause
    exit b %errorlevel%
)

echo.
echo Compilation successful.