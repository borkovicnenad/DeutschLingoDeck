import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

export interface LineChartPoint {
  label: string;
  value: number;
}

const VIEW_WIDTH = 320;
const VIEW_HEIGHT = 160;
const PADDING_X = 12;
const PADDING_TOP = 16;
const AXIS_HEIGHT = 20;

@Component({
  selector: 'app-chart-line',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <svg
      class="chart-line"
      [attr.viewBox]="'0 0 ' + viewWidth + ' ' + viewHeight"
      preserveAspectRatio="none"
      role="img"
      [attr.aria-label]="ariaLabel()"
    >
      @if (areaPath()) {
        <path class="chart-line__area" [attr.d]="areaPath()" />
      }
      @if (linePath()) {
        <path class="chart-line__line" [attr.d]="linePath()" />
      }
      @for (point of points(); track point.label) {
        <circle class="chart-line__dot" [attr.cx]="point.x" [attr.cy]="point.y" r="2.5" />
        <text class="chart-line__label" [attr.x]="point.x" [attr.y]="viewHeight - 4">
          {{ point.label }}
        </text>
      }
    </svg>
  `,
  styles: `
    .chart-line {
      width: 100%;
      height: 100%;
      min-height: 160px;
      overflow: visible;
    }

    .chart-line__area {
      fill: var(--mat-sys-primary);
      opacity: 0.12;
      stroke: none;
    }

    .chart-line__line {
      fill: none;
      stroke: var(--mat-sys-primary);
      stroke-width: 2;
      stroke-linejoin: round;
      stroke-linecap: round;
    }

    .chart-line__dot {
      fill: var(--mat-sys-primary);
    }

    .chart-line__label {
      font-size: 9px;
      fill: var(--mat-sys-on-surface-variant);
      text-anchor: middle;
    }
  `,
})
export class ChartLineComponent {
  readonly data = input.required<LineChartPoint[]>();
  readonly ariaLabel = input<string>('Line chart');

  protected readonly viewWidth = VIEW_WIDTH;
  protected readonly viewHeight = VIEW_HEIGHT;

  protected readonly points = computed(() => {
    const values = this.data();
    const plotHeight = VIEW_HEIGHT - AXIS_HEIGHT - PADDING_TOP;
    const plotWidth = VIEW_WIDTH - PADDING_X * 2;
    const maxValue = Math.max(1, ...values.map((v) => v.value));
    const minValue = Math.min(0, ...values.map((v) => v.value));
    const range = maxValue - minValue || 1;
    const step = values.length > 1 ? plotWidth / (values.length - 1) : 0;

    return values.map((point, index) => ({
      label: point.label,
      value: point.value,
      x: PADDING_X + step * index,
      y: PADDING_TOP + plotHeight - ((point.value - minValue) / range) * plotHeight,
    }));
  });

  protected readonly linePath = computed(() =>
    this.points()
      .map((p, i) => `${i === 0 ? 'M' : 'L'}${p.x},${p.y}`)
      .join(' '),
  );

  protected readonly areaPath = computed(() => {
    const pts = this.points();
    if (pts.length === 0) {
      return '';
    }
    const baseline = VIEW_HEIGHT - AXIS_HEIGHT;
    const first = pts[0];
    const last = pts[pts.length - 1];
    const line = pts.map((p, i) => `${i === 0 ? 'M' : 'L'}${p.x},${p.y}`).join(' ');
    return `${line} L${last.x},${baseline} L${first.x},${baseline} Z`;
  });
}
