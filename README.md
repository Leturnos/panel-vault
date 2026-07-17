# PanelVault 📚

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-brightgreen?logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-enabled-blue?logo=docker&logoColor=white)](https://www.docker.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

PanelVault é uma aplicação robusta para gerenciar coleções pessoais de mangás, HQs e graphic novels. 

O principal objetivo do projeto é permitir que os colecionadores organizem suas obras, acompanhem os volumes adquiridos, controlem gastos detalhados e gerenciem listas de desejos (wishlists) em uma plataforma centralizada.

Além de sua utilidade prática, o projeto serve como um estudo aprofundado no desenvolvimento de APIs RESTful utilizando Java 21 e o ecossistema Spring.

---

## ✨ Funcionalidades Planejadas

### 🎯 MVP (Minimum Viable Product)
- [X] **Cadastro de Obras**: Registro completo com metadados (título, autor, editora, etc.).
- [X] **Busca & Filtragem**: Listagem dinâmica e busca por título da obra.
- [X] **Gerenciamento de Volumes**: Adicionar ou remover volumes físicos da coleção.
- [X] **Status de Leitura/Coleção**: Marcar obras como *completa*, *em andamento* ou *wishlist*.
- [X] **Persistência de Dados**: Integração com banco de dados relacional PostgreSQL.

### 🚀 Funcionalidades Futuras
- [ ] **Controle Financeiro**: Acompanhamento de gastos individuais por obra e volumes.
- [ ] **Painel de Estatísticas**: Dashboard interativo com dados da coleção (total gasto, volumes lidos, etc.).
- [X] **Autenticação Segura**: Controle de acesso por usuário via Spring Security (JWT).
- [ ] **Armazenamento de Mídia**: Upload de capas personalizadas integrando com serviços na nuvem.
- [ ] **Integração com APIs Externas**: Busca automática de informações de obras e volumes.
- [ ] **Notificações**: Alertas sobre lançamentos ou novos volumes disponíveis.
- [ ] **Relatórios**: Exportação da coleção nos formatos CSV e PDF.

---

## 🏗️ Stack Tecnológica

- **Linguagem:** Java 21
- **Framework Principal:** Spring Boot 3.5
- **Persistência:** Spring Data JPA & Hibernate
- **Segurança:** Spring Security
- **Banco de Dados:** PostgreSQL 17
- **Gerenciador de Dependências:** Maven
- **Conteinerização:** Docker & Docker Compose

---

## 📂 Estrutura do Projeto

```text
src/main/java/io/github/leturnos/panelvault
├── config        # Configurações gerais da aplicação (Segurança, CORS, etc.)
├── controller    # Controladores REST expostos pela API
├── dto           # Data Transfer Objects (Request e Response)
├── exception     # Tratamento global de exceções
├── model         # Entidades de banco de dados (JPA Entities)
├── repository    # Interfaces de acesso ao banco (Spring Data)
├── service       # Regras de negócio da aplicação
└── PanelvaultApplication.java  # Classe de inicialização principal
```

---

## 🗃️ Modelo de Dados

### 1. Work (Obra)
Representa uma obra no catálogo global do sistema.

| Campo | Tipo    | Descrição |
| :--- |:--------| :--- |
| `id` | Long    | Identificador único da obra |
| `title` | String  | Título principal da obra |
| `type` | Enum    | Tipo da obra (`MANGA`, `COMIC`, `GRAPHIC_NOVEL`, `MANHWA`) |
| `publisher` | String  | Editora responsável pela publicação |
| `author` | String  | Autor/Roteirista/Desenhista da obra |
| `totalVolumes` | Integer | Quantidade total de volumes lançados |
| `coverUrl` | String  | URL para a imagem da capa |

### 2. Volume (Volume)
Representa uma edição/volume específico associado a uma obra.

| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `id` | Long | Identificador único do volume |
| `number` | Integer | Número da edição/volume |
| `purchaseDate` | LocalDate | Data da compra do volume |
| `purchasePrice` | BigDecimal | Valor pago pelo volume |
| `owned` | Boolean | Sinaliza se o volume já foi adquirido |
| `workId` | Long | Chave estrangeira ligando à obra |
| `userId` | Long | Chave estrangeira ligando ao usuário dono do volume |

### 3. User (Usuário)
Representa um usuário cadastrado na aplicação.

| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `id` | Long | Identificador único do usuário |
| `username` | String | Nome de usuário único para login |
| `email` | String | E-mail de cadastro único |
| `password` | String | Senha criptografada (BCrypt) |

### 4. UserWork (Coleção do Usuário)
Mapeia o relacionamento de coleção e progresso de um usuário com uma obra do catálogo.

| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `id` | Long | Identificador único do registro de coleção |
| `userId` | Long | Chave estrangeira ligando ao usuário |
| `workId` | Long | Chave estrangeira ligando à obra |
| `status` | Enum | Status de coleção (`COMPLETED`, `ONGOING`, `WISHLIST`) |
| `rating` | BigDecimal | Avaliação pessoal do usuário sobre a obra (0.0 a 10.0, passo 0.5) |

---

## 🚀 Como Executar Localmente

### Pré-requisitos
Antes de começar, certifique-se de ter instalado em sua máquina:
* Java 21 (JDK)
* Docker & Docker Compose
* Git

### Passos para Instalação e Execução

1. **Clonar o Repositório:**
   ```bash
   git clone https://github.com/leturnos/panel-vault.git
   cd panel-vault
   ```

2. **Configurar as Variáveis de Ambiente:**
   Crie o arquivo `.env` na raiz do projeto com base no arquivo de exemplo `.env.example`:
   ```bash
   cp .env.example .env
   ```
   *(Opcional: Ajuste as variáveis de usuário e senha dentro de `.env` caso queira).*

3. **Iniciar o Banco de Dados (Docker):**
   Utilize o Docker Compose para subir a instância do PostgreSQL:
   ```bash
   docker compose up -d
   ```

4. **Rodar a Aplicação:**
   Para executar a aplicação carregando as variáveis do arquivo `.env`:
   
   **Linux / macOS:**
   ```bash
   export $(xargs < .env)
   ./mvnw spring-boot:run
   ```

   **Windows (PowerShell):**
   ```powershell
   Get-Content .env | ForEach-Object {
       if ($_ -notmatch "^#|^$") {
           $name, $value = $_ -split '=', 2
           [System.Environment]::SetEnvironmentVariable($name, $value, "Process")
       }
   }
   ./mvnw.cmd spring-boot:run
   ```

### 🔗 Endpoints da API

A tabela abaixo lista todos os endpoints disponíveis na aplicação:

| Recurso | Método | Endpoint | Descrição |
| :--- | :--- | :--- | :--- |
| **Obras** | `GET` | `/works` | Lista todas as obras de forma paginada (filtros opcionais: `title`, paginação: `page`, `size`, `sort`) |
| | `GET` | `/works/{id}` | Busca os detalhes de uma obra específica |
| | `POST` | `/works` | Cadastra uma nova obra |
| | `PUT` | `/works/{id}` | Atualiza as informações de uma obra existente |
| | `DELETE` | `/works/{id}` | Exclui uma obra cadastrada |
| **Coleção** | `PUT` | `/works/{workId}/collection` | Salva ou atualiza a obra na coleção pessoal (com status e nota opcional) |
| | `GET` | `/works/{workId}/collection` | Busca os detalhes de coleção da obra do usuário autenticado |
| | `DELETE` | `/works/{workId}/collection` | Remove a obra da coleção pessoal do usuário autenticado |
| **Volumes**| `POST` | `/works/{workId}/volumes` | Cadastra um volume associado a uma obra na coleção pessoal |
| | `GET` | `/works/{workId}/volumes` | Lista todos os volumes de uma obra específica da coleção pessoal |
| | `GET` | `/volumes/{id}` | Busca os detalhes de um volume individual da coleção pessoal |
| | `DELETE` | `/volumes/{id}` | Exclui um volume cadastrado da coleção pessoal |
| **Estatísticas**| `GET` | `/stats` | Retorna as estatísticas gerais do catálogo de obras e volumes. **[Público - Sem autenticação]** |
| | `GET` | `/stats/me` | Retorna as estatísticas de coleção pessoais do usuário autenticado |
| **Autenticação**| `POST` | `/auth/register` | Cadastra um novo usuário no sistema. **[Público - Sem autenticação]** |
| | `POST` | `/auth/login` | Realiza a autenticação do usuário e retorna o token JWT. **[Público - Sem autenticação]** |

### 🧪 Como Testar a API (Autenticação JWT)

A API utiliza autenticação baseada em JSON Web Tokens (JWT). Para testar os endpoints através de ferramentas como **Postman**, **Insomnia** ou **cURL**:

1. **Cadastrar um Usuário:**
   * Envie uma requisição pública para `POST http://localhost:8080/auth/register` com o corpo JSON:
     ```json
     {
       "username": "leandro",
       "email": "leandro@example.com",
       "password": "senhaSegura123"
     }
     ```

2. **Realizar o Login:**
   * Envie uma requisição pública para `POST http://localhost:8080/auth/login` com o corpo JSON:
     ```json
     {
       "username": "leandro",
       "password": "senhaSegura123"
     }
     ```
   * A API retornará um token JWT no formato:
     ```json
     {
       "token": "seu-token-jwt-gerado-aqui"
     }
     ```

3. **Acessar Endpoints Protegidos:**
   * Para requisições em endpoints protegidos (como listar/cadastrar obras e volumes), adicione o cabeçalho HTTP de Autorização:
     * **Key:** `Authorization`
     * **Value:** `Bearer <seu-token-jwt-gerado-aqui>`

4. **Exemplo de Cadastro de Obra (POST `/works`):**
   * Envie uma requisição (contendo o cabeçalho de autorização) para `POST http://localhost:8080/works` com o corpo JSON:
     ```json
     {
       "title": "Chainsaw Man",
       "type": "MANGA",
       "publisher": "Panini",
       "author": "Tatsuki Fujimoto",
       "totalVolumes": 18
     }
     ```

5. **Exemplo de Cadastro de Volume (POST `/works/{workId}/volumes`):**
   * Envie uma requisição (contendo o cabeçalho de autorização) para `POST http://localhost:8080/works/1/volumes` (substitua `1` pelo ID de uma obra existente) com o corpo JSON:
     ```json
     {
       "number": 1,
       "purchaseDate": "2026-07-10",
       "purchasePrice": 29.90,
       "owned": true
     }
     ```
     
>Para outras requisições consulte a tabela de endpoints acima
> 
---

## 🎯 Objetivos de Aprendizado

- Consolidar conceitos práticos de desenvolvimento backend em ecossistema Spring.
- Praticar modelagem de banco de dados e relacionamentos complexos JPA (One-to-Many, Many-to-One).
- Implementar autenticação moderna utilizando JSON Web Tokens (JWT).
- Desenvolver integrações resilientes com APIs de terceiros.
- Criar uma aplicação backend robusta para portfólio profissional.

---

## 📜 Licença

Este projeto é desenvolvido sob a licença [MIT](LICENSE).