import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

export interface BarChartPoint {
  label: string;
  value: number;
}

const VIEW_WIDTH = 320;
const VIEW_HEIGHT = 160;
const BAR_GAP = 8;
const AXIS_HEIGHT = 20;

@Component({
  selector: 'app-chart-bar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <svg
      class="chart-bar"
      [attr.viewBox]="'0 0 ' + viewWidth + ' ' + viewHeight"
      preserveAspectRatio="none"
      role="img"
      [attr.aria-label]="ariaLabel()"
    >
      @for (bar of bars(); track bar.label) {
        <rect
          class="chart-bar__bar"
          [attr.x]="bar.x"
          [attr.y]="bar.y"
          [attr.width]="bar.width"
          [attr.height]="bar.height"
          rx="3"
        />
        <text class="chart-bar__value" [attr.x]="bar.x + bar.width / 2" [attr.y]="bar.y - 4">
          {{ bar.value }}
        </text>
        <text
          class="chart-bar__label"
          [attr.x]="bar.x + bar.width / 2"
          [attr.y]="viewHeight - 4"
        >
          {{ bar.label }}
        </text>
      }
    </svg>
  `,
  styles: `
    .chart-bar {
      width: 100%;
      height: 100%;
      min-height: 160px;
      overflow: visible;
    }

    .chart-bar__bar {
      fill: var(--mat-sys-primary);
    }

    .chart-bar__value {
      font-size: 9px;
      fill: var(--mat-sys-on-surface-variant);
      text-anchor: middle;
    }

    .chart-bar__label {
      font-size: 9px;
      fill: var(--mat-sys-on-surface-variant);
      text-anchor: middle;
    }
  `,
})
export class ChartBarComponent {
  readonly data = input.required<BarChartPoint[]>();
  readonly ariaLabel = input<string>('Bar chart');

  protected readonly viewWidth = VIEW_WIDTH;
  protected readonly viewHeight = VIEW_HEIGHT;

  protected readonly bars = computed(() => {
    const points = this.data();
    const plotHeight = VIEW_HEIGHT - AXIS_HEIGHT - 12;
    const maxValue = Math.max(1, ...points.map((p) => p.value));
    const barWidth = points.length > 0 ? VIEW_WIDTH / points.length - BAR_GAP : 0;

    return points.map((point, index) => {
      const height = (point.value / maxValue) * plotHeight;
      return {
        label: point.label,
        value: point.value,
        x: index * (barWidth + BAR_GAP) + BAR_GAP / 2,
        y: plotHeight - height + 12,
        width: barWidth,
        height,
      };
    });
  });
}
