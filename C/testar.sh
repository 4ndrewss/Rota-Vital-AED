#!/bin/sh
# Compila e roda os testes de todas as estruturas (Linux, macOS, WSL, Git Bash).
# Uso: ./testar.sh   (ou: sh testar.sh)

cd "$(dirname "$0")" || exit 1

if ! command -v gcc >/dev/null 2>&1; then
    echo "gcc nao encontrado no PATH."
    exit 1
fi

mkdir -p bin
CFLAGS="-std=c99 -Wall -Wextra -pedantic"

echo "Compilando..."
gcc $CFLAGS -o bin/teste_estoque teste_estoque.c estoque.c comum.c &&
gcc $CFLAGS -o bin/teste_fila teste_fila.c fila.c comum.c &&
gcc $CFLAGS -o bin/teste_pilha teste_pilha.c pilha.c comum.c || {
    echo
    echo "Erro de compilacao."
    exit 1
}

falhas=0
./bin/teste_estoque || falhas=1
./bin/teste_fila || falhas=1
./bin/teste_pilha || falhas=1

echo
if [ "$falhas" -eq 0 ]; then
    echo "=== TODOS OS TESTES PASSARAM ==="
else
    echo "=== HOUVE FALHAS: veja os casos marcados com [FALHOU] ==="
fi
exit "$falhas"
