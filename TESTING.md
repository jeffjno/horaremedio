# Plano de testes — Remédio Hora Certa

**Estado:** planejamento de qualidade; **nenhum teste de aplicativo foi executado pela criação deste documento**.  
**Revisão:** 2026-11-10.

## 1. Objetivos

Validar que o aplicativo:

1. Salva medicamentos, prescrições e cronogramas sem perda de dados.
2. Reproduz com fidelidade o horário configurado, observadas as condições reais do Android.
3. Distingue uma dose confirmada de uma notificação vista, adiada ou dispensada.
4. Evita confirmações e lembretes duplicados.
5. Funciona de modo compreensível com fonte ampliada e TalkBack.
6. Não expõe arquivos ou informações médicas desnecessariamente.
7. Permanece utilizável sem conexão à internet.

Um teste de interface bem-sucedido não demonstra, por si só, confiabilidade de alarmes com o aparelho bloqueado. É obrigatória a validação em dispositivo físico.

## 2. Estratégia e ferramentas

| Categoria | Ferramenta proposta | Foco |
|---|---|---|
| Testes unitários | JUnit, `kotlinx-coroutines-test` | regras de agenda, dose, validação |
| Testes de banco | Room em ambiente instrumentado | relações, transações, unicidade, migrações |
| Testes de UI | Compose UI Test | cadastro, acessibilidade, confirmação |
| Testes instrumentados | AndroidJUnitRunner / AndroidX Test | notificações, intents, arquivos |
| Testes manuais | Android físico | Doze, reinício, permissões, fabricante |
| Inspeção de código | revisão de permissões e armazenamento | dados sensíveis, logs, backup |

Selecionar versões compatíveis com o Gradle e o SDK realmente utilizados. Não adotar bibliotecas apenas para satisfazer a documentação.

## 3. Matriz de cenários funcionais

Legenda de status: `NÃO EXECUTADO`, `APROVADO`, `REPROVADO`, `BLOQUEADO`.

| ID | Cenário | Resultado esperado | Prioridade | Status inicial |
|---|---|---|---|---|
| CAD-01 | Cadastrar medicamento válido | Cadastro persistido e recuperado | Alta | NÃO EXECUTADO |
| CAD-02 | Nome vazio | Formulário informa erro sem gravar | Alta | NÃO EXECUTADO |
| CAD-03 | Editar apresentação/dose | Cadastro atualizado sem destruir histórico | Alta | NÃO EXECUTADO |
| CAD-04 | Pausar medicamento | Não agendar novos avisos; preservar passado | Alta | NÃO EXECUTADO |
| CAD-05 | Arquivar medicamento | Retirar da lista ativa sem apagar histórico | Média | NÃO EXECUTADO |
| AGD-01 | Dois horários fixos no mesmo dia | Duas ocorrências distintas | Alta | NÃO EXECUTADO |
| AGD-02 | Dias específicos da semana | Ocorrências só em dias selecionados | Alta | NÃO EXECUTADO |
| AGD-03 | Uso contínuo | Sem data final automática | Alta | NÃO EXECUTADO |
| AGD-04 | Tratamento com fim determinado | Nenhum aviso após a data final | Alta | NÃO EXECUTADO |
| AGD-05 | Intervalo atravessando meia-noite | Próxima ocorrência calculada corretamente | Alta | NÃO EXECUTADO |
| AGD-06 | Editar horário futuro | Cancelar aviso antigo e programar novo | Alta | NÃO EXECUTADO |
| AGD-07 | Mudança de fuso | Aplicar política e não gerar duplicidade | Alta | NÃO EXECUTADO |
| AGD-08 | Horário inexistente/duplicado em mudança DST | Resolver conforme regra documentada | Média | NÃO EXECUTADO |
| ALM-01 | Notificação no horário configurado | Aviso emitido se condições/permissões permitirem | Crítica | NÃO EXECUTADO |
| ALM-02 | Aplicativo fora de primeiro plano | Aviso continua elegível | Crítica | NÃO EXECUTADO |
| ALM-03 | Tela bloqueada | Notificação exibida conforme privacidade | Crítica | NÃO EXECUTADO |
| ALM-04 | Dispositivo reiniciado | Alarmes futuros reconciliados | Crítica | NÃO EXECUTADO |
| ALM-05 | Notificações negadas | Diagnóstico legível, sem alegar entrega | Crítica | NÃO EXECUTADO |
| ALM-06 | Alarme exato não autorizado | App orienta usuário e explicita limitação | Crítica | NÃO EXECUTADO |
| ALM-07 | Acesso especial revogado | Não ocorre falha silenciosa; estado corrigido | Crítica | NÃO EXECUTADO |
| ALM-08 | Alterar relógio/data do dispositivo | Alarmes recalculados conforme política | Alta | NÃO EXECUTADO |
| ALM-09 | Muitas agendas simultâneas | Sem IDs conflitantes ou explosão de avisos | Alta | NÃO EXECUTADO |
| INT-01 | Confirmar **Tomei agora** | Uma confirmação vinculada à dose correta | Crítica | NÃO EXECUTADO |
| INT-02 | Tocar duas vezes em **Tomei agora** | Uma só declaração, sem duplicidade | Crítica | NÃO EXECUTADO |
| INT-03 | Descartar notificação | Não marcar dose como tomada | Crítica | NÃO EXECUTADO |
| INT-04 | Adiar aviso | Mantém mesma ocorrência e posologia | Crítica | NÃO EXECUTADO |
| INT-05 | Horário passou sem confirmação | Mostrar **não confirmada**, não **não tomada** | Crítica | NÃO EXECUTADO |
| INT-06 | Marcar explicitamente **Não tomei** | Registrar ação de forma distinta de atraso | Alta | NÃO EXECUTADO |
| INT-07 | Usar notificação obsoleta após editar agenda | Revalidar; não confirmar dose indevida | Crítica | NÃO EXECUTADO |
| HIS-01 | Fechar e reabrir aplicativo | Histórico preservado | Alta | NÃO EXECUTADO |
| HIS-02 | Editar medicamento com histórico | Registros anteriores identificáveis | Alta | NÃO EXECUTADO |
| ANX-01 | Anexar imagem válida | Cópia privada criada e visível | Alta | NÃO EXECUTADO |
| ANX-02 | Anexar PDF válido | Documento aberto em fluxo controlado | Alta | NÃO EXECUTADO |
| ANX-03 | Arquivo inexistente/corrompido | Erro claro sem travar app | Alta | NÃO EXECUTADO |
| ANX-04 | Arquivo excessivamente grande | Recusa informativa e sem gravação parcial | Média | NÃO EXECUTADO |
| ANX-05 | Excluir anexo | Referência e arquivo tratados corretamente | Alta | NÃO EXECUTADO |
| OFF-01 | Modo avião | Cadastro, histórico e agendamento local funcionam | Alta | NÃO EXECUTADO |
| ACC-01 | Fonte do sistema ampliada | Textos e ações críticos permanecem acessíveis | Crítica | NÃO EXECUTADO |
| ACC-02 | TalkBack | Conteúdo e ações compreensíveis e navegáveis | Crítica | NÃO EXECUTADO |
| ACC-03 | Modo Simplificado | Fluxo de confirmação sem ambiguidade | Alta | NÃO EXECUTADO |
| PRV-01 | Logs e crash reports | Não expõem dados médicos | Crítica | NÃO EXECUTADO |
| PRV-02 | Arquivos privados | Outros apps não recebem acesso indevido | Crítica | NÃO EXECUTADO |
| PRV-03 | Backup automático | Cumpre política de não exportar dados sensíveis | Crítica | NÃO EXECUTADO |
| PRV-04 | Tela de bloqueio | Protege informações conforme preferência | Alta | NÃO EXECUTADO |
| GIT-01 | Repositório local | Commits descrevem entregas reais | Média | NÃO EXECUTADO |
| GIT-02 | Revisão do histórico | Sem receitas reais, credenciais ou chaves | Crítica | NÃO EXECUTADO |

## 4. Roteiros de testes manuais prioritários

### Roteiro A — cadastro até confirmação

**Pré-condições:** app instalado, permissões apropriadas concedidas e hora do aparelho conhecida.

1. Cadastrar medicamento fictício **Medicamento A**, com quantidade de exemplo fictícia.
2. Criar agenda para alguns minutos adiante. Não usar um medicamento real como dado de teste.
3. Bloquear a tela e deixar o aplicativo fora de primeiro plano.
4. Aguardar o evento e registrar horário esperado e horário real do aviso.
5. Abrir o aviso e selecionar **Tomei agora**.
6. Abrir o histórico e verificar ocorrência e confirmação.
7. Repetir a ação pela mesma notificação, se possível.

**Aceite:** um único evento de confirmação, dados corretos e ausência de registro duplicado.

### Roteiro B — adiar não é administrar

1. Criar dose futura fictícia.
2. Ao receber a notificação, selecionar **Lembrar depois**.
3. Consultar histórico antes de qualquer confirmação.
4. Verificar próximo aviso e ID da ocorrência.

**Aceite:** nenhuma dose confirmada e nenhum segundo horário de administração criado.

### Roteiro C — alteração de cronograma

1. Agendar uma ocorrência futura.
2. Modificar o horário para mais tarde ou pausar o tratamento.
3. Verificar cancelamento/reconciliação de alarmes.
4. Observar se a notificação antiga não aparece ou, caso já exista, não provoca atualização indevida.

**Aceite:** apenas ocorrências vigentes são acionáveis.

### Roteiro D — permissões

1. Negar `POST_NOTIFICATIONS` onde aplicável.
2. Observar mensagem e configuração de ajuda.
3. Reativar notificação e repetir.
4. Negar/revogar acesso a alarmes exatos onde aplicável.
5. Verificar se o app explica a indisponibilidade de entrega pontual.

**Aceite:** nenhuma promessa falsa de entrega; nenhuma falha silenciosa.

### Roteiro E — reinicialização e economia de energia

1. Preparar lembrete futuro com margem suficiente.
2. Reiniciar o aparelho.
3. Desbloquear o dispositivo, quando necessário, para liberar dados protegidos por credenciais.
4. Verificar se o lembrete foi reconstituído.
5. Repetir com bateria otimizada e, quando viável, com o aparelho ocioso.

**Aceite:** sistema realiza reconciliação e registra limitações efetivamente observadas. Não afirmar que testes garantem comportamento idêntico em todos os modelos Android.

## 5. Matriz de dispositivos

Registrar as execuções efetivas nesta tabela, **sem preencher com resultados fictícios**.

| Fabricante/modelo | Android/API | Modo de bateria | Alarmes exatos | Notificações | Resultado | Evidência |
|---|---|---|---|---|---|---|
| A preencher | A preencher | A preencher | A preencher | A preencher | NÃO EXECUTADO | — |

Recomenda-se testar no mínimo:

- Uma versão recente do Android (com permissões atuais).
- Outra versão suportada pelo `minSdk` definido no projeto.
- Pelo menos um aparelho real com comportamento específico de economia de energia.
- Acessibilidade com TalkBack e escala ampliada.

## 6. Comandos de execução

**Somente após gerar o projeto Android e configurar os testes.**

Linux/macOS:

```bash
./gradlew clean assembleDebug
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
```

Windows PowerShell:

```powershell
.\gradlew.bat clean assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat connectedDebugAndroidTest
```

`connectedDebugAndroidTest` requer dispositivo/emulador disponível e testes instrumentados configurados. Um comando que retorna sem testes encontrados **não** valida as funcionalidades.

## 7. Modelos de testes automatizados a implementar

**Domínio:**

- Entrada válida e inválida no cadastro.
- Geração de agenda fixa e por intervalo.
- Limite de data final.
- Conversão de horários com fuso e mudanças de relógio.
- Distinção entre estado persistido e indicador de atraso calculado.
- Idempotência `ConfirmDoseTaken`.
- `SnoozeReminder` sem mudança de `DoseOccurrence`.

**Room:**

- Foreign keys e índices de unicidade.
- Rollback quando operação transacional falha.
- Migração preservando registros.
- Arquivamento sem perda de histórico.

**Interface:**

- Campos rotulados de maneira compreensível.
- Ações disponíveis ao TalkBack.
- Falhas de permissão anunciadas em texto.
- Navegação em modo simplificado.

## 8. Formato de relatório de execução

Registrar em cada sessão de validação:

```text
Data/hora da execução:
Versão do aplicativo / commit Git:
Ambiente: fabricante, modelo, Android e API:
ID do cenário:
Pré-condições e permissões:
Passos executados:
Resultado esperado:
Resultado observado:
Status: APROVADO | REPROVADO | BLOQUEADO
Evidência (print/log sanitizado):
Bug associado (se houver):
Responsável:
```

**Nunca anexar telas com nomes de medicamentos reais, receitas, dados pessoais ou logs não sanitizados ao repositório público.**

## 9. Critério de liberação do MVP

Uma versão candidata só deve ser liberada quando:

- Casos de prioridade **Crítica** estejam aprovados em ambientes compatíveis ou formalmente bloqueados com justificativa e decisão documentada.
- Não existam falhas conhecidas de duplicação de registro de dose.
- Permissões e limitações dos alarmes estejam claramente informadas.
- Testes de migração e persistência não apresentem perda de histórico.
- A interface principal esteja utilizável com leitor de tela e fontes grandes.
- Política de privacidade reflita o que o código efetivamente faz.

Se uma função crítica não funcionar, **não** anunciar o aplicativo como confiável para lembretes de tratamentos reais.

---

*Este documento é um plano. Substituir os status iniciais somente com evidência de execução.*
