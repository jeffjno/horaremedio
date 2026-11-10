# Histórico de mudanças — Remédio Hora Certa

Todas as mudanças relevantes devem ser registradas neste arquivo. Recomenda-se o formato [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/) e numeração de versões conforme [Semantic Versioning](https://semver.org/lang/pt-BR/), quando houver versões distribuídas.

**Atenção:** a seção `Não lançado` reúne a base implementada e o planejamento ainda pendente. As datas e versões de lançamento público continuam indefinidas.

## [Não lançado]

### Base Android multilíngue — 2026-11-10

#### Adicionado

- Projeto Android nativo com Gradle, Kotlin, Jetpack Compose e Material 3.
- Interface inicial com painel de hoje e área de configurações.
- Seleção de idioma por aplicativo, com opção de seguir o sistema e persistência compatível com Android 12 ou anterior.
- Recursos completos para 20 idiomas e suporte a layout RTL em árabe.
- Geração automática da lista de idiomas exibida nas configurações do Android 13 ou superior.
- Testes unitários para a lista de idiomas e correspondência de códigos regionais.

#### Validado

- `assembleDebug`, `testDebugUnitTest` e `lintDebug` executados com sucesso.
- APK de depuração gerado localmente.

### Documentação inicial — 2026-11-10

#### Adicionado

- `README.md`: objetivos, escopo do MVP, preparação do ambiente, estrutura e plano de commits Git.
- `docs/ARCHITECTURE.md`: arquitetura proposta, separação em camadas e regras de agendamento.
- `docs/DATABASE.md`: modelo relacional proposto, estados de dose, integridade e migração.
- `docs/TESTING.md`: matriz de testes planejada, critérios de qualidade e roteiros manuais.
- `docs/PRIVACY.md`: minuta de privacidade, medidas de segurança e pendências antes de publicar.

#### Pendente

- Criar e compilar o projeto Android real.
- Implementar e testar as funcionalidades descritas no planejamento.
- Definir ID do aplicativo, `minSdk`/`targetSdk`, dependências e licença.
- Confirmar responsáveis, contatos e política efetiva de tratamento de dados.
- Executar testes em dispositivos reais e registrar evidências.

## Como registrar futuras alterações

Criar uma seção por versão, por exemplo:

```markdown
## [0.1.0] - AAAA-MM-DD

### Adicionado
- Cadastro local de medicamentos com Room, verificado por testes.

### Corrigido
- Descrição objetiva de um erro realmente corrigido.

### Segurança
- Mudança validada relacionada a permissões ou dados sensíveis.
```

Somente registrar uma funcionalidade como **adicionada** quando sua implementação estiver integrada e validada; anotar hash de commit ou referência de issue quando útil.

## Convenção de commits

Preferir [Conventional Commits](https://www.conventionalcommits.org/pt-br/v1.0.0/):

- `feat:` funcionalidades implementadas.
- `fix:` correções de defeitos.
- `test:` testes e cenários.
- `docs:` documentação.
- `refactor:` mudanças internas sem mudança intencional de comportamento.
- `chore:` manutenção e configuração.
- `security:` prefixo local opcional para medidas de segurança (documentar a convenção se adotado).

**Não publicar em Git remoto sem autorização do responsável pelo projeto.**
