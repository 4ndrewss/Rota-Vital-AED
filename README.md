# Rota Vital — AED (Unidade 1)

Projeto Integrador Rota Vital (CESAR School — ADS, 3º período): sistema de gestão e distribuição de hemocomponentes.

Esta entrega é referente à disciplina de Algoritmos e Estruturas de Dados (AED), Unidade 1: implementação das estruturas básicas do domínio (estoque, requisições e histórico) em C, com reimplementação equivalente em Java.

## Organização

- `/c` — implementação das estruturas em C (malloc/free, ponteiros explícitos).
- `/java` — reimplementação das mesmas estruturas em Java.

## Estruturas

- **Estoque** → lista encadeada
- **Requisições hospitalares** → fila
- **Histórico de operações** → pilha (opcional).

## Validação de Entrada (Entrega — Caio)

Especificação completa e implementação da camada de blindagem e validação dos documentos de requisição de hemocomponentes:
- Documentação técnica e especificação OpenAPI 3.0: [`VALIDACAO_ENTRADA.md`](./VALIDACAO_ENTRADA.md)
- Código-fonte em Java: [`Java/src/br/org/cesar/rotavital/validacao/`](./Java/src/br/org/cesar/rotavital/validacao/)
  - `DocumentoInvalidoException.java`: Exceção customizada detalhada por campo
  - `ValidadorEntradaDocumento.java`: Validação física (magic bytes) e semântica (data, prefixo, responsável, assinatura)
  - `TesteValidadorEntrada.java`: Bateria de testes cobrindo todos os cenários de erro e sucesso