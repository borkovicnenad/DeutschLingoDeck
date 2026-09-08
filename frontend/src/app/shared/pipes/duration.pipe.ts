import { Pipe, PipeTransform } from '@angular/core';

import { formatDuration } from '../utils/duration-format.util';

/** Renders a duration given in seconds as "35 sec" / "2 min 14 sec" / "12 min" / "1 h 8 min". */
@Pipe({ name: 'appDuration' })
export class DurationPipe implements PipeTransform {
  transform(totalSeconds: number | null | undefined): string {
    return totalSeconds === null || totalSeconds === undefined ? '—' : formatDuration(totalSeconds);
  }
}
