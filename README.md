# WePayU

## Visão geral
O WePayU é um sistema de folha de pagamento sem interface gráfica que centraliza o cadastro de empregados, o registro de eventos (cartões de ponto, vendas e taxas) e o cálculo recorrente das folhas com deduções sindicais. A aplicação é dirigida por uma fachada (`Facade`) que conecta os serviços de empregados, folha de pagamento, histórico de undo/redo e persistência em XML para atender aos comandos exercitados pelos testes de aceitação EasyAccept.

## Arquitetura principal
- **Facade** – inicializa os serviços, carrega dados persistidos, delega cada comando de alto nível (criar, alterar, remover, lançar eventos, rodar folha, undo/redo) e salva o estado ao encerrar o sistema.
- **EmpregadoService** – mantém o banco de empregados, valida entradas e encapsula regras de negócio de cadastro, alterações, registros de cartões/vendas/taxas e manutenção das agendas de pagamento.
- **FolhaPagamentoService** – calcula folhas completas em uma data informada, gera arquivos de saída, reaproveita resultados já processados e acumula totais brutos.
- **AgendaRepository** – registra agendas padrão, personalizadas e recém-criadas, garantindo consistência de descrição e atribuição automática conforme o tipo do empregado.
- **UndoRedoService** – guarda snapshots do estado para desfazer ou refazer operações, sincronizando empregados, folhas processadas e agendas.
- **LoadSaveService** – realiza a persistência em `empregados.xml`, serializando empregados e agendas personalizadas com `XMLEncoder`/`XMLDecoder`.

## Fluxo da folha de pagamento
Ao rodar a folha, o serviço coleta todos os empregados elegíveis naquela data com base nas agendas, soma horas normais e extras, calcula comissões e descontos (taxa sindical diária e taxas de serviço não deduzidas), atualiza a data do último pagamento e grava o contracheque completo em arquivo. Consultas ao total bruto podem reutilizar cálculos anteriores.

## User Stories implementadas
1. **Adicionar empregado** – cria registros horistas, assalariados ou comissionados, valida entradas, gera ID único, define pagamento em mãos e agenda padrão.
2. **Remover empregado** – exclui o cadastro validando a identificação e permite desfazer a operação.
3. **Lançar cartão de ponto** – adiciona cartões para horistas com validação de data e de horas positivas, alimentando os cálculos de horas normais/extras.
4. **Lançar resultado de venda** – registra vendas para comissionados, garantindo valores positivos e associando-as ao período correto.
5. **Lançar taxa de serviço** – localiza o membro sindicalizado, valida entrada e registra taxas extras dedutíveis.
6. **Alterar detalhes do empregado** – cobre nome, endereço, salário, comissão, sindicalização, método de pagamento (em mãos, correios ou banco), agenda, além de conversões de tipo com atualização das regras associadas.
7. **Rodar folha de pagamento** – processa horistas, assalariados e comissionados, calcula bruto/líquido, desconta taxas, atualiza última data paga e gera o arquivo solicitado.
8. **Undo/Redo de transações** – mantém pilhas para desfazer/refazer todas as operações suportadas, inclusive rodadas de folha.
9. **Agenda de pagamento** – permite consultar e alterar agendas válidas por empregado, atribui padrões conforme o tipo e respeita frequências/nomes canônicos.
10. **Criação de novas agendas** – possibilita registrar agendas semanais ou mensais personalizadas (incluindo apelidos), evitando duplicidades e validando descrições.
11. **Persistência do sistema** – salva e restaura empregados, agendas personalizadas e folhas processadas entre execuções por meio de arquivo XML.

## Como começar
Os cenários de aceitação EasyAccept localizados na pasta `tests/` exercitam todos os comandos públicos da `Facade`. Para executar a suíte principal, utilize o runner configurado pelo professor (ex.: `java -cp <classpath> easyaccept.EasyAccept tests/AllTests.txt`).
