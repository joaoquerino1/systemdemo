import { paraIsoLocal } from './data.util';

describe('paraIsoLocal', () => {
  it('usa o dia do calendário local, não o de UTC', () => {
    // 24/08/2026 23:30 no fuso da máquina. A oeste de UTC (ex.: Brasil)
    // isso já é 25/08 em UTC — toISOString() erraria o "hoje" do ponto.
    const noiteLocal = new Date(2026, 7, 24, 23, 30, 0);

    const utcIso = noiteLocal.toISOString().substring(0, 10);
    expect(paraIsoLocal(noiteLocal)).toBe('2026-08-24');
    if (utcIso !== '2026-08-24') {
      expect(paraIsoLocal(noiteLocal)).not.toBe(utcIso);
    }
  });

  it('preenche mês e dia com zero à esquerda', () => {
    expect(paraIsoLocal(new Date(2026, 0, 5, 8, 0, 0))).toBe('2026-01-05');
  });
});
