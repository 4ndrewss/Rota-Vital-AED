@echo off
rem Compila e roda os testes de todas as estruturas (Windows: cmd ou PowerShell).
rem Uso: testar.bat

setlocal
cd /d "%~dp0"

where gcc >nul 2>nul
if errorlevel 1 (
    echo gcc nao encontrado no PATH. Instale o MinGW/MSYS2 e tente novamente.
    exit /b 1
)

if not exist bin mkdir bin
set CFLAGS=-std=c99 -Wall -Wextra -pedantic

echo Compilando...
gcc %CFLAGS% -o bin\teste_estoque.exe teste_estoque.c estoque.c comum.c || goto erro_compilacao
gcc %CFLAGS% -o bin\teste_fila.exe teste_fila.c fila.c comum.c || goto erro_compilacao
gcc %CFLAGS% -o bin\teste_pilha.exe teste_pilha.c pilha.c comum.c || goto erro_compilacao

set FALHAS=0
bin\teste_estoque.exe || set FALHAS=1
bin\teste_fila.exe || set FALHAS=1
bin\teste_pilha.exe || set FALHAS=1

echo.
if %FALHAS%==0 (
    echo === TODOS OS TESTES PASSARAM ===
) else (
    echo === HOUVE FALHAS: veja os casos marcados com [FALHOU] ===
)
exit /b %FALHAS%

:erro_compilacao
echo.
echo Erro de compilacao.
exit /b 1
