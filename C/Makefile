CC = gcc
CFLAGS = -std=c99 -Wall -Wextra -pedantic -g
BIN = bin

all: $(BIN)/rotavital $(BIN)/teste_estoque $(BIN)/teste_fila $(BIN)/teste_pilha

$(BIN):
	mkdir -p $(BIN)

$(BIN)/rotavital: main.c estoque.c fila.c pilha.c comum.c estoque.h fila.h pilha.h comum.h | $(BIN)
	$(CC) $(CFLAGS) -o $@ main.c estoque.c fila.c pilha.c comum.c

$(BIN)/teste_estoque: teste_estoque.c estoque.c comum.c estoque.h comum.h teste.h | $(BIN)
	$(CC) $(CFLAGS) -o $@ teste_estoque.c estoque.c comum.c

$(BIN)/teste_fila: teste_fila.c fila.c comum.c fila.h comum.h teste.h | $(BIN)
	$(CC) $(CFLAGS) -o $@ teste_fila.c fila.c comum.c

$(BIN)/teste_pilha: teste_pilha.c pilha.c comum.c pilha.h comum.h teste.h | $(BIN)
	$(CC) $(CFLAGS) -o $@ teste_pilha.c pilha.c comum.c

teste: $(BIN)/teste_estoque $(BIN)/teste_fila $(BIN)/teste_pilha
	./$(BIN)/teste_estoque
	./$(BIN)/teste_fila
	./$(BIN)/teste_pilha

clean:
	rm -rf $(BIN)

.PHONY: all teste clean
