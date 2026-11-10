# Remédio Hora Certa

> **Status:** base Android funcional criada em 2026-11-10, compilada e validada com testes unitários e Android Lint. O cadastro, persistência, alarmes e demais recursos clínicos do MVP ainda serão implementados nas próximas etapas.

**Remédio Hora Certa** é um projeto de aplicativo Android nativo para organizar horários de medicamentos, lembrar as doses programadas e permitir que a pessoa registre o que efetivamente tomou. A experiência prioriza pessoas idosas, pessoas com TDAH e outras pessoas que se beneficiam de uma interface objetiva e acessível.

**Aviso importante:** o aplicativo será uma ferramenta de organização pessoal, não um dispositivo de diagnóstico ou orientação clínica. O usuário deverá informar horários e doses conforme prescrição ou orientação profissional. Em caso de dose esquecida, o aplicativo **não** deve sugerir tomar dose extra, duplicar a próxima dose ou alterar a prescrição: a pessoa deve consultar a orientação do profissional de saúde ou a bula aplicável.

## 1. Estado atual do projeto

| Item | Situação inicial |
|---|---|
| Especificação funcional | Documentada |
| Arquitetura e modelo de dados | Propostos, sujeitos a validação |
| Interface em Jetpack Compose | Base implementada: painel inicial, configurações e seletor de idioma |
| Cadastro e banco Room | A implementar |
| Alarmes e notificações | A implementar e validar em aparelho físico |
| Anexos de imagens e PDFs | A implementar |
| Testes automatizados e manuais | Testes unitários e lint executados; testes em dispositivo ainda pendentes |
| APK/AAB publicável | APK de depuração gerado; versão de produção ainda não preparada |

**Como atualizar esta tabela:** ao concluir cada etapa, atualizar o status com base em código, testes e commits reais. Nunca marcar um recurso como concluído apenas porque ele está descrito neste README.

## 2. Funcionalidades previstas para o MVP

- Cadastro de medicamento, apresentação, concentração, quantidade por administração, observações e fotografia.
- Definição de vários horários por dia, dias específicos da semana e períodos de tratamento.
- Diferenciação entre **uso contínuo** e **tratamento com data final**.
- Tratamentos definidos por horários ou por intervalos explicitamente prescritos e configurados.
- Importação e visualização de receitas em PDF e fotografias, armazenadas no espaço privado do app.
- Lembretes com opções **Tomei agora**, **Lembrar depois** e **Abrir medicamento**.
- Histórico de doses confirmadas, explicitamente não tomadas e ainda não confirmadas.
- Tela **Minha Medicação de Hoje**, Modo Simplificado, fontes ampliadas e compatibilidade com TalkBack.
- Operação local sem conta e sem dependência de internet para as funções principais.

**Fora do MVP:** reconhecimento de receitas com OCR, recomendação de posologia, integração clínica, sincronização em nuvem, monitoramento remoto por cuidadores e sistema multiusuário. Qualquer evolução deve passar por avaliação de segurança e privacidade.

## 3. Tecnologias propostas

### Idiomas disponíveis

A interface base oferece 20 idiomas: português (Brasil), inglês, espanhol, francês, alemão, italiano, neerlandês, polonês, russo, ucraniano, turco, árabe, hindi, bengali, chinês simplificado, japonês, coreano, indonésio, vietnamita e tailandês.

O idioma pode seguir o sistema ou ser escolhido em **Configurações > Idioma**. A preferência usa as APIs públicas de idioma por aplicativo do Android, aparece nas configurações do sistema a partir do Android 13 e é preservada pelo AppCompat nas versões anteriores. O layout oferece suporte a escrita da direita para a esquerda para o árabe.

Todo novo texto de interface deve ser criado em `app/src/main/res/values/strings.xml` e traduzido nos 19 diretórios alternativos antes da entrega. Dados digitados pelo usuário, nomes de medicamentos e anexos não são traduzidos automaticamente.

| Camada | Tecnologia |
|---|---|
| Plataforma | Android nativo (minSdk 26, targetSdk 37) |
| Linguagem | Kotlin |
| Interface | Jetpack Compose, Material 3 e Compose BOM 2026.09.00 |
| Arquitetura | MVVM, casos de uso e Repository Pattern |
| Persistência | Room (SQLite) |
| Preferências | DataStore |
| Assincronismo | Coroutines e StateFlow |
| Navegação | Navigation Compose |
| Alarmes | AlarmManager, quando cabível e autorizado pelo sistema |
| Notificações | NotificationManager e canais de notificação |
| Manutenção assíncrona | WorkManager (não como substituto de alarme exato) |
| Anexos | Android Photo Picker, câmera/arquivo privado e Storage Access Framework |
| Build | Android Gradle Plugin 9.3.3 e Gradle 9.5.0 |
| Versionamento | Git local; Git remoto opcional |

As **versões exatas** de SDK, Kotlin, Gradle e bibliotecas devem ser escolhidas na criação do projeto com base na compatibilidade da versão estável do Android Studio e registradas nos arquivos Gradle. Este documento não pressupõe versões já instaladas.

## 4. Preparação do ambiente

### Pré-requisitos

1. Instalar Android Studio em versão estável compatível com o projeto.
2. Instalar o Android SDK e aceitar as licenças necessárias.
3. Instalar Git e configurar `user.name` e `user.email` no ambiente local.
4. Disponibilizar um dispositivo Android físico para validar permissões, execução em segundo plano e alarmes. Emuladores são úteis, mas não substituem essa validação.

### Abrir o projeto

Quando o código Android estiver criado:

1. Abrir a pasta raiz no Android Studio.
2. Aguardar a sincronização do Gradle.
3. Confirmar que o SDK requerido está instalado.
4. Selecionar um dispositivo ou emulador.
5. Executar a configuração **app**.

Com um projeto Gradle inicializado, os comandos habituais, executados na raiz, são:

```bash
# Linux/macOS
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

```powershell
# Windows PowerShell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
```

**Atenção:** esses comandos só funcionam **depois** que o projeto Android/Gradle e o wrapper `gradlew` tiverem sido gerados. Não são testes já executados para esta documentação.

## 5. Organização prevista

```text
Remedio-Hora-Certa/
├── README.md
├── CHANGELOG.md
├── docs/
│   ├── ARCHITECTURE.md
│   ├── DATABASE.md
│   ├── TESTING.md
│   └── PRIVACY.md
├── .gitignore                 # adicionar na inicialização do projeto
├── settings.gradle.kts       # a gerar
├── build.gradle.kts          # a gerar
├── gradlew                   # a gerar
├── gradlew.bat               # a gerar
└── app/                      # a gerar
    └── src/main/...
```

Os documentos podem ser colocados na raiz de um projeto Android já existente. Nesse caso, preservar os arquivos e a configuração do projeto original.

## 6. Fluxo esperado de uso

1. A pessoa cadastra um medicamento e informa a posologia **conforme orientação recebida**.
2. Define horários, dias e vigência (contínuo ou por período).
3. O aplicativo grava o cronograma e agenda lembretes para as próximas ocorrências.
4. No horário, a pessoa recebe uma notificação, respeitadas as permissões e limitações do aparelho.
5. A opção **Tomei agora** gera uma confirmação explícita; dispensar a notificação **não** equivale a tomar a dose.
6. O histórico permite identificar confirmações, ausências de confirmação e doses explicitamente marcadas como não tomadas.

Uma dose em atraso não deve ser automaticamente tratada como dose não tomada. Consultar [DATABASE.md](docs/DATABASE.md) para as regras de estados.

## 7. Planejamento por etapas

| Etapa | Entrega | Commit sugerido |
|---|---|---|
| 1 | Projeto Android, Gradle, Git e compilação | `chore: inicializa projeto Android Remedio Hora Certa` |
| 2 | Entidades, DAOs e repositories | `feat: implementa persistencia local de medicamentos` |
| 3 | Cadastro, edição e listagem | `feat: implementa cadastro e edicao de medicamentos` |
| 4 | Horários e duração dos tratamentos | `feat: implementa agendamento de tratamentos` |
| 5 | Fotos e receitas em PDF | `feat: implementa anexos de receitas e fotografias` |
| 6 | Alarmes, permissões e notificações | `feat: implementa alarmes e notificacoes de medicamentos` |
| 7 | Confirmações e histórico | `feat: implementa historico de administracao` |
| 8 | Modo Simplificado e acessibilidade | `feat: adiciona recursos de acessibilidade` |
| 9 | Preferências, privacidade e segurança | `feat: implementa configuracoes e privacidade` |
| 10 | Testes, correções e estabilização | `test: valida fluxos principais e corrige falhas` |
| 11 | Revisão da documentação | `docs: documenta arquitetura instalacao e testes` |
| 12 | Preparação para Git remoto | Sem `push` automático |

Um commit deve ser feito **após** a validação pertinente. Não criar commits afirmando que uma funcionalidade existe quando somente sua documentação foi escrita.

## 8. Controle de versão

Verificar primeiro se o diretório já está sob Git:

```bash
git status
```

Se não estiver, e **somente após** preparar `.gitignore` e revisar arquivos sensíveis:

```bash
git init
git branch -M main
git add README.md CHANGELOG.md docs/
git commit -m "docs: adiciona documentacao inicial do Remedio Hora Certa"
```

Na continuidade do desenvolvimento:

```bash
git status
git diff
git add <arquivos-validados>
git commit -m "feat: descreve a funcionalidade realmente implementada"
git log --oneline --decorate --graph
```

### Publicação remota opcional

Quando existir um repositório remoto vazio e o responsável autorizar a publicação:

```bash
git remote -v
# Apenas se ainda não existir origin:
git remote add origin URL_REAL_DO_REPOSITORIO
git branch --show-current
git push -u origin main
```

Se `origin` já existir, **não** executar `git remote add` novamente: revisar sua URL. Configurar autenticação no serviço Git por mecanismo seguro, sem incorporar tokens ao código.

**Antes de qualquer `push`:** revisar `.gitignore`, histórico e arquivos rastreados para garantir que não existam receitas reais, dados de pacientes, bancos locais, tokens, chaves de assinatura ou segredos. Remover somente do último commit não elimina um segredo de todo o histórico.

## 9. Critérios mínimos de qualidade

O MVP só pode ser anunciado como funcional após:

- Compilação e instalação bem-sucedidas.
- Cadastro e edição com persistência após reinicialização do app.
- Lembretes verificados com o app fechado, tela bloqueada e após reinício do dispositivo.
- Tratamento correto da permissão de notificações e do acesso a alarmes exatos.
- Confirmação idempotente de doses e histórico íntegro.
- Tratamentos temporários encerrando os agendamentos no prazo indicado.
- Importação e abertura de PDFs/imagens sem exposição desnecessária dos arquivos.
- Validação com fonte ampliada e TalkBack.
- Registro de resultados, versões de aparelho e eventuais limitações em [TESTING.md](docs/TESTING.md).

## 10. Documentação técnica

- [Arquitetura e fluxos](docs/ARCHITECTURE.md)
- [Modelo de dados e integridade](docs/DATABASE.md)
- [Plano de testes](docs/TESTING.md)
- [Privacidade e segurança](docs/PRIVACY.md)
- [Histórico de versões](CHANGELOG.md)

## 11. Referências técnicas

- [Android — Schedule alarms](https://developer.android.com/develop/background-work/services/alarms)
- [Android — Notificações dependentes de tempo](https://developer.android.com/develop/ui/views/notifications/time-sensitive)
- [Android — Photo Picker](https://developer.android.com/training/data-storage/shared/photo-picker)
- [Android — Storage Access Framework](https://developer.android.com/guide/topics/providers/document-provider)
- [Android — Backup automático](https://developer.android.com/identity/data/autobackup)

## 12. Licenciamento e responsáveis

Licença, mantenedor, meios de contato e eventual marca de publicação: **a definir antes de disponibilizar publicamente**. Não presumir licença de código aberto sem decisão expressa do responsável.

---

*Documento inicial de planejamento. Revisar periodicamente para refletir o comportamento comprovado do aplicativo.*
