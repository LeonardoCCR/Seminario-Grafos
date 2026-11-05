# Sistema de Grafos — Trabalho de Teoria dos Grafos

Projeto desenvolvido ao longo do semestre na disciplina de Teoria dos Grafos
(IF Sudeste MG — Juiz de Fora), com o professor Daves Martins.

O sistema representa grafos por **matriz de adjacências** e funciona por um menu
de terminal. Trata grafos orientados e não orientados, ponderados e não ponderados.

**Grupo 16:** Leonardo César e Lucas Ferreira.

## Funcionalidades

| Entrega | O que foi implementado |
|---|---|
| 01 | Criação do grafo, inclusão de vértices e arestas |
| 02 | Remoção de vértice e aresta, informações e classificação do grafo, fechos transitivos |
| 03 | Caminho mínimo com Dijkstra |
| 04 | Árvore geradora mínima com Kruskal e com Prim |
| 05 | Exportação para DOT, geração de imagem e componentes fortemente conexas com Malgrange |
| 06 | Busca em largura (BFS), busca em profundidade (DFS) e leitura de grafo a partir de arquivo DOT |

## Como compilar e executar

O projeto usa **Maven** e precisa do **Java 17 ou superior**.

A geração de imagens depende da biblioteca `br.com.davesmartins:api`, que fica
num repositório do GitLab já declarado no `pom.xml`. Por isso a primeira
compilação precisa de internet para baixar as dependências.

### Compilar

```bash
mvn clean compile
```

### Executar o sistema

```bash
mvn dependency:copy-dependencies
java -cp "target/classes;target/dependency/*" Application
```

No Linux ou no Mac, troque o `;` por `:` no classpath:

```bash
java -cp "target/classes:target/dependency/*" Application
```

### Executar a rotina de testes

```bash
java -cp "target/classes;target/dependency/*" GraphTestRoutine
```

A rotina executa os 40 testes em sequência e, se todos passarem, termina com
`Todos os testes passaram.` Se algum falhar, ela para na hora e mostra o que
era esperado.

### Executar os testes unitários

```bash
mvn test
```

## Como navegar pelo menu

### Menu inicial

```
1 - Criar Grafo          cria um grafo vazio; pergunta se é orientado e se é ponderado
2 - Abrir Grafo (.dot)   carrega um grafo de um arquivo .dot do disco
0 - Sair
```

Os arquivos de exemplo ficam em `DotArchives/Read/`. Para abrir um deles, informe
o caminho quando o sistema pedir:

```
DotArchives/Read/direcionado.dot
DotArchives/Read/nao_direcionado.dot
```

### Menu do grafo

Depois de criar ou abrir um grafo, o sistema mostra:

```
1.1 - Adicionar Vértice            informe um ID e um valor
1.2 - Adicionar Aresta             informe origem e destino; se for ponderado, o peso
1.3 - Remover Vértice
1.4 - Remover Aresta
1.5 - Imprimir Grafo               mostra a matriz de adjacências
1.6 - Informações do Grafo         ordem, tamanho, graus e as classificações 1.6.1 a 1.6.8
1.7 - Algoritmos
1.8 - Salvar Grafo em Disco (.dot)
1.9 - Gerar Imagem do Grafo
1.10 - Sair
```

### Algoritmos (opção 1.7)

```
1.7.1 - Dijkstra       caminho mínimo; pede origem e destino
1.7.2 - Kruskal        árvore geradora mínima
1.7.3 - Prim           árvore geradora mínima
1.7.4 - Malgrange      componentes fortemente conexas e grafo reduzido
1.7.5 - BFS            busca em largura; pede o vértice inicial
1.7.6 - DFS            busca em profundidade; pede o vértice inicial
```

Alguns algoritmos só valem para um tipo de grafo. Quando não se aplicam, o
sistema informa o motivo e volta ao menu, sem encerrar:

| Algoritmo | Exige |
|---|---|
| Dijkstra | grafo ponderado e sem pesos negativos |
| Kruskal e Prim | grafo não orientado e ponderado |
| Malgrange e fechos transitivos | grafo orientado |

### Imagens (opção 1.9)

```
1.9.1 - Imagem do grafo
1.9.2 - Imagens das componentes fortemente conexas e do grafo reduzido
1.9.3 - Imagem da busca (BFS ou DFS)
```

As imagens saem em `Generated-Graph-Images/`. Se o caminho for deixado em branco,
o sistema usa um nome padrão. A imagem é gerada de novo a cada chamada, sempre a
partir do estado atual do grafo.

Na imagem da busca, os vértices aparecem numerados na ordem de visita e as
arestas percorridas saem em vermelho. As demais arestas continuam no desenho, em
cinza, e os vértices não alcançados ficam cinza com a observação.

## Exemplo rápido de uso

Para ver o sistema funcionando sobre um grafo lido do disco:

1. No menu inicial, escolha `2` e informe `DotArchives/Read/direcionado.dot`
2. Escolha `1.6` para ver as informações e a classificação do grafo
3. Escolha `1.7` e depois `1.7.1` para o caminho mínimo de `A` até `E`
4. Escolha `1.7.4` para as componentes fortemente conexas
5. Escolha `1.9` e depois `1.9.3` para gerar a imagem da busca

## Organização do projeto

```
src/main/java/
  Application.java              menu do sistema
  GraphTestRoutine.java         rotina com os 40 testes
  grafos/model/
    AdjMatrixGraph.java         o grafo e todos os algoritmos
    Node.java                   o vértice
src/test/java/                  testes unitários (JUnit)
DotArchives/Read/               arquivos .dot de exemplo
DotArchives/Write/              arquivos .dot salvos pelo sistema
Generated-Graph-Images/         imagens geradas
README-images/                  evidências dos testes usadas neste documento
```

## Sobre a rotina de testes

A classe `GraphTestRoutine` cria os grafos de cada cenário, executa as operações,
compara o resultado com o esperado e interrompe com `AssertionError` se alguma
verificação falhar.

Os testes não cobrem só os casos que dão certo. Também verificam entrada
inválida, algoritmo aplicado no tipo errado de grafo, grafo desconexo, ciclo,
laço, vértice isolado e caminho inexistente. Quando faz sentido, conferem também
que o algoritmo não alterou o grafo original.

Comparações de peso usam uma pequena tolerância, porque números decimais têm
representação binária aproximada.

## Os 40 testes

| Nº | Cenário | O que é verificado |
|---:|---|---|
| 01 | Triângulo não orientado | Grafo simples, regular, completo e não bipartido; graus dos vértices. |
| 02 | Caminho A-B-C e vértice isolado D | Folhas, vértice isolado, grau, regularidade, completude e bipartição. |
| 03 | Grafo ponderado com laço | Detecção de laço, grau do vértice, peso zero, simplicidade e rejeição de aresta paralela. |
| 04 | Ciclo de quatro vértices | Ciclo par, regularidade e propriedade de ser bipartido. |
| 05 | Dígrafo A→B→C→D e isolado E | Orientação, fontes, sumidouros, antecessores, sucessores e fechos transitivos. |
| 06 | Dígrafo completo com três vértices | Completude, regularidade, graus de entrada e saída e fechos com ciclo. |
| 07 | Remoção de arco e vértice | Remoção correta, preservação de arcos independentes e atualização da ordem/tamanho. |
| 08 | Dijkstra em grafo não orientado | Menor caminho e custo total em grafo ponderado. |
| 09 | Dijkstra em grafo orientado | Respeito à direção dos arcos e identificação de destinos inalcançáveis. |
| 10 | Prim em grafo conexo | Árvore geradora mínima com peso total conhecido. |
| 11 | Prim com pesos empatados | Quantidade de arestas e peso mínimo mesmo quando há mais de uma solução possível. |
| 12 | Prim com laço e aresta paralela | Laço ignorado na AGM e multiaresta recusada pela matriz. |
| 13 | Prim em grafo desconexo | Floresta geradora mínima, componentes e vértice isolado. |
| 14 | Prim em grafo orientado | Rejeição de algoritmo que não se aplica a dígrafos. |
| 15 | Prim com um vértice | Caso limite: zero arestas e peso total zero. |
| 16 | Kruskal em grafo conexo | Seleção das arestas de menor peso e custo total conhecido. |
| 17 | Kruskal com pesos empatados | Resultado mínimo válido em cenário com empate. |
| 18 | Kruskal com laço e aresta paralela | Laço não selecionado e tentativa de multiaresta rejeitada. |
| 19 | Kruskal em grafo desconexo | Floresta mínima com componentes separados e vértice isolado. |
| 20 | Kruskal em grafo orientado | Rejeição de aplicação em grafo orientado. |
| 21 | Kruskal com um vértice | Caso limite: nenhum arco/aresta selecionado e custo zero. |
| 22 | Malgrange com componentes de tamanhos diferentes | Identificação de várias componentes fortemente conexas. |
| 23 | Malgrange em dígrafo fortemente conexo | Identificação de uma única componente contendo todos os vértices. |
| 24 | Malgrange em dígrafo sem ciclos | Cada vértice permanece em uma componente própria. |
| 25 | Malgrange com fonte e sumidouro | Separação correta da fonte, componente cíclica e sumidouro. |
| 26 | Malgrange com laço e arco paralelo | Laço tratado e tentativa de arco paralelo rejeitada. |
| 27 | Grafo reduzido acíclico | Construção do grafo de componentes e verificação das ligações entre elas. |
| 28 | Malgrange em grafo não orientado | Rejeição do algoritmo em tipo de grafo incompatível. |
| 29 | Geração visual das componentes | Exportação de PNG do grafo original colorido por componente e do grafo reduzido. |
| 30 | BFS e DFS em grafo não orientado | Ordens de visita e árvores de busca distintas, respeitando a estratégia de cada busca. |
| 31 | Busca em grafo desconexo | Identificação dos vértices alcançados e não alcançados a partir da origem. |
| 32 | Busca em dígrafo | Respeito à orientação durante BFS/DFS. |
| 33 | Busca em dígrafo com ciclo e laço | Cada vértice visitado uma única vez; ausência de repetição infinita. |
| 34 | Busca com um vértice e vértice inexistente | Caso limite e rejeição de origem que não pertence ao grafo. |
| 35 | Leitura e escrita DOT não direcionado | Importação, conferência dos dados, exportação e reimportação preservando pesos e estrutura. |
| 36 | Leitura e escrita DOT direcionado | Importação, preservação da orientação e dos pesos após exportar e importar novamente. |
| 37 | Validações negativas de entrada | ID vazio/duplicado, vértice inexistente, peso incompatível, infinito e NaN. |
| 38 | Dijkstra com entradas inválidas | Rejeição de grafo não ponderado, pesos negativos e origem/destino inexistentes. |
| 39 | Prim e Kruskal sem pesos | Rejeição dos dois algoritmos quando o grafo não é ponderado. |
| 40 | DOT com arquivo inexistente | Falha controlada de leitura por `IOException`, sem criar arquivo automaticamente. |

## Evidências dos testes

As imagens foram organizadas por número de teste. Cada uma apresenta o grafo ou cenário utilizado à esquerda e o resultado correspondente da execução à direita.

<details>
<summary><strong>Testes 01 a 10 — propriedades, estrutura e Dijkstra</strong></summary>


### Teste 01

![Evidência do teste 01](README-images/teste_01.png)

### Teste 02

![Evidência do teste 02](README-images/teste_02.png)

### Teste 03

![Evidência do teste 03](README-images/teste_03.png)

### Teste 04

![Evidência do teste 04](README-images/teste_04.png)

### Teste 05

![Evidência do teste 05](README-images/teste_05.png)

### Teste 06

![Evidência do teste 06](README-images/teste_06.png)

### Teste 07

![Evidência do teste 07](README-images/teste_07.png)

### Teste 08

![Evidência do teste 08](README-images/teste_08.png)

### Teste 09

![Evidência do teste 09](README-images/teste_09.png)

### Teste 10

![Evidência do teste 10](README-images/teste_10.png)

</details>

<details>
<summary><strong>Testes 11 a 21 — Prim e Kruskal</strong></summary>

### Teste 11

![Evidência do teste 11](README-images/teste_11.png)

### Teste 12

![Evidência do teste 12](README-images/teste_12.png)

### Teste 13

![Evidência do teste 13](README-images/teste_13.png)

### Teste 14

![Evidência do teste 14](README-images/teste_14.png)

### Teste 15

![Evidência do teste 15](README-images/teste_15.png)

### Teste 16

![Evidência do teste 16](README-images/teste_16.png)

### Teste 17

![Evidência do teste 17](README-images/teste_17.png)

### Teste 18

![Evidência do teste 18](README-images/teste_18.png)

### Teste 19

![Evidência do teste 19](README-images/teste_19.png)

### Teste 20

![Evidência do teste 20](README-images/teste_20.png)

### Teste 21

![Evidência do teste 21](README-images/teste_21.png)

</details>

<details>
<summary><strong>Testes 22 a 29 — componentes fortemente conexas e visualização</strong></summary>

### Teste 22

![Evidência do teste 22](README-images/teste_22.png)

### Teste 23

![Evidência do teste 23](README-images/teste_23.png)

### Teste 24

![Evidência do teste 24](README-images/teste_24.png)

### Teste 25

![Evidência do teste 25](README-images/teste_25.png)

### Teste 26

![Evidência do teste 26](README-images/teste_26.png)

### Teste 27

![Evidência do teste 27](README-images/teste_27.png)

### Teste 28

![Evidência do teste 28](README-images/teste_28.png)

### Teste 29

![Evidência do teste 29](README-images/teste_29.png)

Imagens específicas produzidas pelo teste 29:

![Grafo original com componentes fortemente conexas coloridas](README-images/grafo_original_cfcs.png)

![Grafo reduzido](README-images/grafo_reduzido.png)

</details>

<details>
<summary><strong>Testes 30 a 40 — BFS, DFS, DOT e validações negativas</strong></summary>

### Teste 30

![Evidência do teste 30](README-images/teste_30.png)

### Teste 31

![Evidência do teste 31](README-images/teste_31.png)

### Teste 32

![Evidência do teste 32](README-images/teste_32.png)

### Teste 33

![Evidência do teste 33](README-images/teste_33.png)

### Teste 34

![Evidência do teste 34](README-images/teste_34.png)

### Teste 35

![Evidência do teste 35](README-images/teste_35.png)

### Teste 36

![Evidência do teste 36](README-images/teste_36.png)

### Teste 37

![Evidência do teste 37](README-images/teste_37.png)

### Teste 38

![Evidência do teste 38](README-images/teste_38.png)

### Teste 39

![Evidência do teste 39](README-images/teste_39.png)

### Teste 40

![Evidência do teste 40](README-images/teste_40.png)

</details>

## Arquivos DOT usados nos testes

- `DotArchives/Read/nao_direcionado.dot` — grafo não orientado e ponderado, 5 vértices e 7 arestas
- `DotArchives/Read/direcionado.dot` — grafo orientado e ponderado, 5 vértices e 8 arcos

Os testes 35 e 36 leem esses arquivos, exportam o grafo de volta para DOT, leem
de novo e conferem que nada mudou. Os arquivos temporários criados nesse processo
são apagados ao final.
