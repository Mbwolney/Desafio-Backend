# Desafio Backend - Requisitos

## 1. Validações

Você deve ajustar as entidades (model e sql) de acordo com as regras abaixo: 

- `Product.name` é obrigatório, não pode ser vazio e deve ter no máximo 100 caracteres.
- `Product.description` é opcional e pode ter no máximo 255 caracteres.
- `Product.price` é obrigatório deve ser > 0.
- `Product.status` é obrigatório.
- `Product.category` é obrigatório.
- `Category.name` deve ter no máximo 100 caracteres.
- `Category.description` é opcional e pode ter no máximo 255 caracteres.

## 2. Otimização de Performance
- Analisar consultas para identificar possíveis gargalos.
- Utilizar índices e restrições de unicidade quando necessário.
- Implementar paginação nos endpoints para garantir a escala conforme o volume de dados crescer.
- Utilizar cache com `Redis` para o endpoint `/auth/context`, garantindo que a invalidação seja feita em caso de alteração dos dados.

## 3. Logging
- Registrar logs em arquivos utilizando um formato estruturado (ex.: JSON).
- Implementar níveis de log: DEBUG, INFO, WARNING, ERROR, CRITICAL.
- Utilizar logging assíncrono.
- Definir estratégias de retenção e compressão dos logs.

## 4. Refatoração
- Atualizar a entidade `Product`:
  - Alterar o atributo `code` para o tipo inteiro.
- Versionamento da API:
  - Manter o endpoint atual (v1) em `/api/products` com os códigos iniciados por `PROD-`.
  - Criar uma nova versão (v2) em `/api/v2/products` onde `code` é inteiro.

## 5. Integração com Swagger
- Documentar todos os endpoints com:
  - Descrições detalhadas.
  - Exemplos de JSON para requisições e respostas.
  - Listagem de códigos HTTP e mensagens de erro.

## 6. Autenticação e Gerenciamento de Usuários
- Criar a tabela `users` com as colunas:
  - `id` (chave primária com incremento automático)
  - `name` (obrigatório)
  - `email` (obrigatório, único e com formato válido)
  - `password` (obrigatório)
  - `role` (obrigatório e com valores permitidos: `admin` ou `user`)
- Inserir um usuário admin inicial:
  - Email: `contato@simplesdental.com`
  - Password: `KMbT%5wT*R!46i@@YHqx`
- Endpoints:
  - `POST /auth/login` - Realiza login.
  - `POST /auth/register` - Registra novos usuários (se permitido).
  - `GET /auth/context` - Retorna `id`, `email` e `role` do usuário autenticado.
  - `PUT /users/password` - Atualiza a senha do usuário autenticado.

## 7. Permissões e Controle de Acesso
- Usuários com `role` admin podem criar, alterar, consultar e excluir produtos, categorias e outros usuários.
- Usuários com `role` user podem:
  - Consultar produtos e categorias.
  - Atualizar apenas sua própria senha.
  - Não acessar ou alterar dados de outros usuários.

## 8. Testes
- Desenvolver testes unitários para os módulos de autenticação, autorização e operações CRUD.


## 🧪 Testes com Postman

Importe a collection abaixo no Postman:

📁 [Download da Collection](src/main/java/com/simplesdental/product/docs/Desafio.postman_collection.json)

## Swagger

Link -> http://localhost:8080/swagger-ui/index.html#/Produtos%20v2

---

# Perguntas

1. **Se tivesse a oportunidade de criar o projeto do zero ou refatorar o projeto atual, qual arquitetura você utilizaria e por quê?**
   Optaria por uma arquitetura baseada em microservices e micro frontends, principalmente pensando em escalabilidade, manutenção e autonomia das equipes.
Com os microservices, cada serviço pode evoluir de forma independente, o que facilita bastante a organização do código e dos times.
Já no front, os micro frontends complementam bem esse modelo, permitindo que diferentes equipes entreguem partes da interface de forma isolada, sem depender de uma única aplicação monolítica.
2. **Qual é a melhor estratégia para garantir a escalabilidade do código mantendo o projeto organizado?**  
   Para garantir melhor estratégica seria o DDD (Domain-Driven Design), dividindo o projeto em camadas claras (como controller, service, repository) ou até mesmo em módulos ou serviços independentes, dependendo do tamanho da aplicação.
3. **Quais estratégias poderiam ser utilizadas para implementar multitenancy no projeto?**
   Se eu fosse implementar multitenancy, eu começaria entendendo o nível de isolamento necessário. Pra algo mais simples, dá pra usar um banco único com um campo tenant_id, que já resolve bem e é fácil de manter. Agora, se cada cliente precisar de mais segurança ou separação, aí vale usar um schema por tenant, ou até um banco por tenant, dependendo da complexidade.
O mais importante é ter uma forma de identificar o tenant por request — tipo pelo subdomínio, um header ou no próprio token JWT — e garantir que isso seja carregado certinho no contexto da aplicação. Costumo usar um filtro pra capturar o tenant no início da request e deixar disponível pro resto da lógica. E se o projeto tiver frontend, pode ser legal permitir customizações por tenant também, tipo logo, cores, permissões.
   4. **Como garantir a resiliência e alta disponibilidade da API durante picos de tráfego e falhas de componentes?**
    Escalar horizontalmente com instâncias atrás de um balanceador de carga, como o do Kubernetes ou algum gateway. Isso ajuda muito em picos de tráfego.
    Cache também ajuda bastante, principalmente em dados que não mudam o tempo todo. E monitoramento pelos logs.
5. **Quais práticas de segurança essenciais você implementaria para prevenir vulnerabilidades como injeção de SQL e XSS?**
   Pra garantir segurança na aplicação, sigo algumas práticas essenciais:
SQL Injection: Sempre uso ORM como JPA ou Hibernate, que já trata isso internamente. E quando preciso de queries personalizadas, uso @Query com parâmetros nomeados.
XSS: Valido e escapo todos os inputs, principalmente os que vão pro front. Também uso sanitizadores quando necessário.
Configurações adicionais: Mantenho o CORS bem configurado, ativo o CSRF no Spring Security quando necessário, e aplico validações com Bean Validation (@NotBlank, @Size, etc).
Autenticação e autorização: Uso JWT, defino bem as roles dos usuários e protejo os endpoints com base nesses perfis.
5. **Qual a abordagem mais eficaz para estruturar o tratamento de exceções de negócio, garantindo um fluxo contínuo desde sua ocorrência até o retorno da API?**
   No ControllerAdvice, trato essas exceções e retorno um JSON com código de status, mensagem e até um timestamp, se necessário.
5. **Considerando uma aplicação composta por múltiplos serviços, quais componentes você considera essenciais para assegurar sua robustez e eficiência?**
   Primeiro, a comunicação entre os serviços: pode ser síncrona com REST ou assíncrona usando mensageria, como RabbitMQ, dependendo do cenário e da necessidade de desempenho.
Também acho essencial ter monitoramento com ferramentas como Grafana e logs estruturados, pra facilitar o rastreio e a análise de problemas.
Na parte de segurança, autenticação com OAuth ajuda a centralizar o controle de acesso de forma segura.
E claro, o uso de APIs bem definidas, com cache inteligente, além de um API Gateway, que centraliza o tráfego e facilita o roteamento, autenticação e rate limiting."
6. **Como você estruturaria uma pipeline de CI/CD para automação de testes e deploy, assegurando entregas contínuas e confiáveis?**
   Eu começaria rodando os testes unitários e de integração logo de cara, pra garantir que o código novo não quebrou nada. Depois, passaria pelo SonarQube, que ajuda a checar cobertura de testes, code smells e possíveis vulnerabilidades.
Se estiver tudo certo, empacoto a aplicação e envio pro repositório de artefatos, tipo um Nexus. A partir daí, faço o deploy no ambiente de homologação, onde o time de QA pode validar tudo com calma.
Depois da aprovação, a gente libera o deploy pra produção. E, claro, tudo isso com monitoramento ativo, usando ferramentas como Grafana, análise de logs estruturados e alertas configurados pra manter o sistema estável e confiável

Obs: Forneça apenas respostas textuais; não é necessário implementar as perguntas acima.

