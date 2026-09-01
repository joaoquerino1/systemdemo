import { HttpErrorResponse } from '@angular/common/http';

/**
 * O backend responde erros em dois formatos (ver GlobalExceptionHandler):
 * - { mensagem: "..." } para erros de negócio/autenticação/acesso
 * - { campos: { nomeCampo: "mensagem" } } para erros de validação (@Valid)
 * Esta função normaliza os dois em uma única string pra exibir na tela.
 */
export function extrairMensagemErro(erro: HttpErrorResponse, padrao = 'Ocorreu um erro. Tente novamente.'): string {
  const corpo = erro.error;

  if (!corpo) return padrao;

  if (typeof corpo.mensagem === 'string') {
    return corpo.mensagem;
  }

  if (corpo.campos && typeof corpo.campos === 'object') {
    const mensagens = Object.values(corpo.campos) as string[];
    return mensagens.join(' ');
  }

  return padrao;
}
