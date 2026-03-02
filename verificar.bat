@echo off
echo ========================================
echo VERIFICANDO SEU AMBIENTE
echo ========================================
echo.

echo [1] Verificando Java instalado:
echo.
dir "C:\Program Files\Java\" /b
echo.

echo [2] Verificando JavaFX:
echo.
if exist "C:\Program Files\Java\javafx-sdk-17.0.18" (
    echo ✅ JavaFX 17 encontrado
) else (
    echo ❌ JavaFX não encontrado
    echo.
    echo PROCURE POR: C:\Program Files\Java\javafx-sdk-*
    echo.
    dir "C:\Program Files\Java\javafx-*" 2>nul
)

echo.
echo [3] Verificando pastas do projeto:
echo.
if exist "src\br\com\marketplace" (
    echo ✅ Estrutura de pastas OK
) else (
    echo ❌ Pastas não encontradas
)

echo.
echo [4] JAVA_HOME atual:
echo %JAVA_HOME%

echo.
pause