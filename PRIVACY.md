# Privacidade e proteção de dados — Remédio Hora Certa

**Versão:** minuta técnica para desenvolvimento, **não constitui política definitiva publicada**.  
**Data da minuta:** 2026-11-10.  
**Estado:** os compromissos abaixo são **requisitos planejados**, não afirmações verificadas sobre um aplicativo já implementado.

> **Antes da publicação ou distribuição:** identificar o responsável pelo tratamento de dados, incluir canal de contato válido, conferir a implementação e revisar este texto com apoio jurídico quando necessário. A documentação não comprova, por si só, conformidade com a Lei Geral de Proteção de Dados (LGPD).

## 1. Propósito do aplicativo

O Remédio Hora Certa é um aplicativo de uso pessoal destinado a organizar medicamentos, horários e tratamentos informados pelo próprio usuário. Também pretende permitir o armazenamento de fotografias e receitas e o registro de confirmações de administração.

O aplicativo **não é serviço médico**, não faz diagnóstico, não prescreve e não recomenda alterações de dose. Em caso de esquecimento ou dúvidas sobre o tratamento, o usuário deverá seguir orientação de profissional de saúde.

## 2. Dados que o aplicativo pretende processar

| Categoria | Exemplos | Finalidade |
|---|---|---|
| Medicamentos | nome, apresentação, quantidade, observações | organizar cadastro |
| Agenda de tratamento | dias, horas, início, fim, uso contínuo | gerar lembretes |
| Histórico de uso | horário programado, confirmação voluntária, situação | mostrar acompanhamento pessoal |
| Documentos | PDFs de receita, imagens de embalagem e medicamento | referência voluntária do usuário |
| Preferências | tema, Modo Simplificado, sons, adiamento | personalização da experiência |
| Dados técnicos locais | identificadores internos e estado operacional dos avisos | persistência e prevenção de duplicidade |

Informações sobre medicamentos e receitas podem revelar dados referentes à saúde; a LGPD reconhece dados de saúde como **dados pessoais sensíveis**. A proteção deve ser proporcional a essa criticidade.

## 3. Princípios de minimização

A proposta inicial **não necessita** de:

- Nome civil completo, CPF, endereço ou telefone para funcionamento local.
- Criação de conta ou senha em servidor externo.
- Acesso permanente à localização ou aos contatos.
- Publicidade direcionada ou serviços de rastreamento.
- OCR ou envio das receitas a serviços externos.
- Integração com redes sociais.

Qualquer funcionalidade futura que passe a usar esses recursos deverá ter finalidade definida, atualização da política e análise das bases legais aplicáveis.

## 4. Armazenamento e compartilhamento — comportamento pretendido

**Estratégia para o MVP:**

1. Registrar dados estruturados no SQLite privado gerenciado por Room.
2. Manter fotos e PDFs em área privada do aplicativo com acesso controlado.
3. Guardar preferências não clínicas em DataStore.
4. Não transmitir informações médicas a servidores do desenvolvedor.
5. Não incluir SDKs de publicidade, analytics ou rastreamento comportamental.
6. Não compartilhar automaticamente receitas com aplicativos terceiros.
7. Não coletar dados para treinamento de modelos de inteligência artificial.

A mera utilização de armazenamento privado **não equivale a criptografia individual de todos os arquivos**. A equipe deverá avaliar proteção em repouso do dispositivo, criptografia adicional de anexos, tratamento de chaves e exposição via intents/visualizadores.

**Importante:** a ausência de servidor próprio não impede, por si só, backups do sistema Android. A configuração de backup deverá ser revista e verificada antes de distribuir a aplicação.

## 5. Permissões solicitadas

A equipe deve apresentar explicações contextuais antes de solicitações que possam afetar a experiência.

| Permissão/acesso | Necessidade prevista | Cuidados |
|---|---|---|
| Notificações (`POST_NOTIFICATIONS`, Android 13+) | exibir lembretes | solicitar no momento apropriado e explicar ausência de aviso |
| Alarmes e lembretes (`SCHEDULE_EXACT_ALARM`, quando aplicável) | disparos mais pontuais | verificar capacidade; respeitar a revogação |
| Câmera (se usada diretamente) | fotografar embalagem ou receita | pedir apenas se o fluxo exigir; usar mecanismos seguros |
| Photo Picker | selecionar fotografia | preferir seletor do sistema à leitura ampla da galeria |
| Storage Access Framework | selecionar PDF/documentos | importar apenas arquivos escolhidos pelo usuário |
| Inicialização concluída (`RECEIVE_BOOT_COMPLETED`, quando implementado) | reagendar avisos futuros | não expor dados no boot ou em logs |

Não usar permissões de acesso irrestrito ao armazenamento sem necessidade demonstrada. Permissões de alarmes exatos possuem requisitos específicos de plataforma e, em distribuição por loja, políticas próprias. Não presumir autorização automática.

## 6. Notificações na tela bloqueada

Notificações podem revelar o nome de medicamentos a outras pessoas que enxerguem o aparelho.

A implementação deverá:

- Oferecer opção de ocultar informações sensíveis no bloqueio.
- Utilizar visibilidade/canal de notificação coerentes com a preferência.
- Evitar textos excessivos contendo dados médicos na notificação.
- Não colocar informações sensíveis em `PendingIntent` exposto, logs ou mensagens de erro.
- Validar a identidade da ocorrência e o estado persistido antes de confirmar ações originadas da notificação.

Uma notificação dispensada não comprova que o medicamento foi tomado; o registro depende de confirmação explícita.

## 7. Receitas e outros anexos

**Processamento pretendido:** o usuário escolhe o arquivo; o aplicativo valida seu formato e tamanho; a cópia é armazenada em diretório privado; o histórico de anexos guarda apenas metadados.

Requisitos de segurança:

- Limitar MIME e tamanho (limites a definir durante a implementação).
- Rejeitar arquivo inválido/corrompido com mensagem compreensível.
- Usar nomes internos aleatórios, sem incorporar dados médicos no caminho.
- Evitar compartilhamento público por engano e controlar eventuais permissões temporárias de URI.
- Implementar exclusão de arquivo e metadados, inclusive recuperação em caso de interrupção.
- Não enviar arquivos automaticamente para a nuvem.

## 8. Backup, migração de aparelho e perda de dados

A proposta para a versão inicial é **evitar que dados e documentos médicos sejam incluídos em backup automático**, até existir mecanismo de recuperação seguro, compreensível e testado.

Tarefas obrigatórias antes do lançamento:

1. Verificar `android:allowBackup` e as regras compatíveis com o SDK alvo.
2. Revisar `android:dataExtractionRules` e, conforme o suporte, `fullBackupContent`.
3. Testar backup na nuvem e transferência entre dispositivos para versões relevantes de Android.
4. Explicar ao usuário que, sem backup/restauração confiável, a desinstalação, a limpeza de dados ou a perda do aparelho podem levar à perda definitiva das informações.
5. Revisar a política caso seja incluída exportação manual criptografada ou sincronização futura.

O comportamento de backup pode variar conforme a versão Android e o fabricante; a configuração precisa ser verificada em aparelho real.

Referência: https://developer.android.com/identity/data/autobackup

## 9. Exclusão e retenção

**Durante o uso:** medicamentos ativos, cronogramas, confirmações e anexos permanecem localmente até ações de edição, arquivamento ou exclusão previstas na interface.

**Arquivar medicamento:** deve interromper avisos futuros e ocultar o cadastro dos itens ativos, preservando o histórico conforme escolha do usuário e política documentada.

**Excluir dados permanentemente:** deverá ser uma ação separada, com confirmação explícita e informação sobre o alcance da exclusão (medicamento, histórico, arquivos vinculados). A equipe deverá documentar eventuais limitações técnicas, incluindo backups/arquivos fora da aplicação, caso existam.

**Desinstalação:** em regra remove dados privados do aplicativo no dispositivo, mas devem ser considerados backups e comportamentos específicos de sistema/distribuição. Não prometer eliminação universal e irreversível sem validação.

## 10. Segurança no ciclo de desenvolvimento

- Não publicar receitas reais, PDFs de pacientes ou banco de produção no Git.
- Não versionar arquivos de assinatura, tokens ou variáveis secretas.
- Usar exemplos fictícios em screenshots, testes e documentação.
- Não gravar detalhes sensíveis em Logcat, crash reports ou analytics.
- Restringir `exported` de componentes Android quando cabível.
- Validar caminhos e formatos de documentos importados.
- Revisar dependências e permissões a cada atualização.
- Usar canais seguros para reporte de vulnerabilidades, após definição de contato.
- Testar a visibilidade das notificações na tela bloqueada.
- Registrar decisões de segurança relevantes em revisões de código.

## 11. Lei Geral de Proteção de Dados (LGPD)

A Lei nº 13.709/2018 estabelece regras sobre o tratamento de dados pessoais, incluindo dados sensíveis. A análise de obrigações depende do contexto efetivo de uso, de quem determina as finalidades e meios do tratamento e de eventuais exceções legais aplicáveis.

**Não presumir** que todo tratamento de dados de saúde se apoia em uma mesma base legal. Para o caso concreto, revisar as hipóteses do art. 11, o papel dos agentes de tratamento, a necessidade de informação transparente e os direitos aplicáveis aos titulares.

Fontes públicas:

- ANPD — Perguntas frequentes: https://www.gov.br/anpd/pt-br/acesso-a-informacao/perguntas-frequentes
- Ministério da Saúde — LGPD: https://www.gov.br/saude/pt-br/acesso-a-informacao/lgpd

Esta minuta não substitui avaliação jurídica e técnica antes da distribuição pública.

## 12. Mudanças futuras que exigem reavaliação

Antes de implementar qualquer item abaixo, atualizar a análise de risco e os documentos do projeto:

- Sincronização com servidor, Google Drive ou outro serviço externo.
- Gestão por cuidador ou familiar de dados de outra pessoa.
- Login, contas, compartilhamento de histórico ou multiusuário.
- Reconhecimento de texto de receitas por OCR/IA.
- Serviço de analytics, telemetria ou relatórios de falha de terceiros.
- Exportação de PDF com histórico e dados pessoais.
- Integração com serviços de saúde ou prescrições digitais.

## 13. Campos pendentes antes da publicação

| Informação | Estado |
|---|---|
| Responsável/mantenedor do aplicativo | **A definir** |
| Canal de privacidade e suporte | **A definir** |
| Versão publicada e data de vigência | **A definir** |
| Versões Android suportadas | **A definir** |
| Inventário real de permissões e SDKs | **A auditar após implementação** |
| Política real de backup/exclusão | **A validar** |
| Revisão jurídica/LGPD | **Pendente** |
| Declarações de privacidade para a loja | **A preencher segundo o app real** |

---

*Texto técnico preparatório. Não apresentar esta minuta como política definitiva de aplicativo funcional sem revisão, preenchimento e testes.*
