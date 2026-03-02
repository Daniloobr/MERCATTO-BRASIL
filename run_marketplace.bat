@echo off
echo Compilando Mercatto Brasil...
echo.

javac -d bin src/br/com/marketplace/model/Produto.java
javac -d bin -cp bin src/br/com/marketplace/service/Deposito.java
javac --module-path "C:\Program Files\Java\javafx-sdk-17.0.18\lib" --add-modules javafx.controls,javafx.fxml -d bin -cp bin src/br/com/marketplace/view/MainFX.java

if errorlevel 1 (
    echo.
    echo ❌ Erro na compilação!
    echo.
    pause
    exit /b
)

echo.
echo ✅ Compilado com sucesso!
echo.
echo Executando...
echo.

java --module-path "C:\Program Files\Java\javafx-sdk-17.0.18\lib" --add-modules javafx.controls,javafx.fxml -cp bin br.com.marketplace.view.MainFX

pause