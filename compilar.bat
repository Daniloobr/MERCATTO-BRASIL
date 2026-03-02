@echo off
echo Compilando projeto com JavaFX...
echo.

:: Compilar tudo de uma vez
javac --module-path "C:\Program Files\Java\javafx-sdk-17.0.18\lib" --add-modules javafx.controls,javafx.fxml -d bin src/br/com/marketplace/**/*.java

if errorlevel 1 (
    echo Erro na compilação!
    pause
    exit /b
)

echo.
echo ✅ Compilado com sucesso!
echo.
echo Agora execute: java --module-path "C:\Program Files\Java\javafx-sdk-17.0.18\lib" --add-modules javafx.controls,javafx.fxml -cp bin br.com.marketplace.view.MainFX
pause