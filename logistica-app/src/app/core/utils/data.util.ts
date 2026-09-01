/**
 * Formata uma data no calendário local (yyyy-MM-dd).
 * Date#toISOString() usa UTC e, em fusos a oeste de Greenwich
 * (ex.: Brasil, UTC-3), vira o dia seguinte a partir das 21h locais.
 */
export function paraIsoLocal(data: Date = new Date()): string {
  const ano = data.getFullYear();
  const mes = (data.getMonth() + 1).toString().padStart(2, '0');
  const dia = data.getDate().toString().padStart(2, '0');
  return `${ano}-${mes}-${dia}`;
}
