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
- [ ] **Busca & Filtragem**: Listagem dinâmica e busca por título da obra.
- [X] **Gerenciamento de Volumes**: Adicionar ou remover volumes físicos da coleção.
- [X] **Status de Leitura/Coleção**: Marcar obras como *completa*, *em andamento* ou *wishlist*.
- [X] **Persistência de Dados**: Integração com banco de dados relacional PostgreSQL.

### 🚀 Funcionalidades Futuras
- [ ] **Controle Financeiro**: Acompanhamento de gastos individuais por obra e volumes.
- [ ] **Painel de Estatísticas**: Dashboard interativo com dados da coleção (total gasto, volumes lidos, etc.).
- [ ] **Autenticação Segura**: Controle de acesso por usuário via Spring Security (JWT).
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

## 🗃️ Modelo de Dados Inicial

### 1. Work (Obra)
Representa uma obra registrada na coleção.

| Campo | Tipo    | Descrição |
| :--- |:--------| :--- |
| `id` | Long    | Identificador único da obra |
| `title` | String  | Título principal da obra |
| `type` | Enum    | Tipo da obra (`MANGA`, `COMIC`, `GRAPHIC_NOVEL`, `MANHWA`) |
| `publisher` | String  | Editora responsável pela publicação |
| `author` | String  | Autor/Roteirista/Desenhista da obra |
| `totalVolumes` | Integer | Quantidade total de volumes lançados |
| `status` | Enum    | Status atual da coleção (`COMPLETED`, `ONGOING`, `WISHLIST`) |
| `coverUrl` | String  | URL para a imagem da capa |

### 2. Volume (Volume)
Representa uma edição/volume específico associado a uma obra.

| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `id` | UUID / Long | Identificador único do volume |
| `number` | Integer | Número da edição/volume |
| `purchaseDate` | LocalDate | Data da compra do volume |
| `purchasePrice` | BigDecimal | Valor pago pelo volume |
| `owned` | Boolean | Sinaliza se o volume já foi adquirido |
| `workId` | Long | Chave estrangeira ligando à obra |

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
| **Obras** | `GET` | `/works` | Lista todas as obras cadastradas |
| | `GET` | `/works/{id}` | Busca os detalhes de uma obra específica |
| | `POST` | `/works` | Cadastra uma nova obra |
| | `PUT` | `/works/{id}` | Atualiza as informações de uma obra existente |
| | `DELETE` | `/works/{id}` | Exclui uma obra cadastrada |
| **Volumes**| `POST` | `/works/{workId}/volumes` | Cadastra um volume associado a uma obra |
| | `GET` | `/works/{workId}/volumes` | Lista todos os volumes de uma obra específica |
| | `GET` | `/volumes/{id}` | Busca os detalhes de um volume individual |
| | `DELETE` | `/volumes/{id}` | Exclui um volume cadastrado |

### 🧪 Como Testar a API (Basic Auth)

Como o Spring Security está ativo no projeto, ao iniciar a aplicação o console do Spring Boot exibirá uma senha temporária gerada automaticamente (procure por `Using generated security password:` nos logs do terminal).

Para testar os endpoints através de clientes como **Postman**, **Insomnia** ou **cURL**:

1. Configure a autenticação da requisição para **Basic Auth**:
   * **Username**: `user`
   * **Password**: (insira a senha gerada no console)

2. **Exemplo de Cadastro de Obra (POST `/works`):**
   * Envie uma requisição para `POST http://localhost:8080/works` com o corpo JSON:
     ```json
     {
       "title": "Chainsaw Man",
       "type": "MANGA",
       "publisher": "Panini",
       "author": "Tatsuki Fujimoto",
       "totalVolumes": 18,
       "status": "ONGOING"
     }
     ```

3. **Exemplo de Cadastro de Volume (POST `/works/{workId}/volumes`):**
   * Envie uma requisição para `POST http://localhost:8080/works/1/volumes` (substitua `1` pelo ID de uma obra existente) com o corpo JSON:
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