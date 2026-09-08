import { formatDuration } from './duration-format.util';

describe('formatDuration', () => {
  it('formats sub-minute durations as seconds', () => {
    expect(formatDuration(35)).toBe('35 sec');
    expect(formatDuration(0)).toBe('0 sec');
  });

  it('formats minute-scale durations, omitting seconds only when there is no remainder', () => {
    expect(formatDuration(134)).toBe('2 min 14 sec');
    expect(formatDuration(720)).toBe('12 min');
  });

  it('formats hour-scale durations, dropping seconds entirely', () => {
    expect(formatDuration(4080)).toBe('1 h 8 min');
    expect(formatDuration(7200)).toBe('2 h');
  });

  it('treats exactly 60s and exactly 3600s as the start of the next unit', () => {
    expect(formatDuration(60)).toBe('1 min');
    expect(formatDuration(3600)).toBe('1 h');
  });
});
