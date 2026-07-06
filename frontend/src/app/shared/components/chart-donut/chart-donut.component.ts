import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

export type DonutColor = 'primary' | 'success' | 'warning' | 'danger' | 'neutral';

export interface DonutChartSegment {
  label: string;
  value: number;
  color: DonutColor;
}

const SIZE = 120;
const STROKE_WIDTH = 16;
const RADIUS = (SIZE - STROKE_WIDTH) / 2;
const CIRCUMFERENCE = 2 * Math.PI * RADIUS;

@Component({
  selector: 'app-chart-donut',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="chart-donut">
      <svg
        class="chart-donut__svg"
        [attr.viewBox]="'0 0 ' + size + ' ' + size"
        role="img"
        [attr.aria-label]="ariaLabel()"
      >
        <g [attr.transform]="'rotate(-90 ' + size / 2 + ' ' + size / 2 + ')'">
          @if (arcs().length === 0) {
            <circle
              class="chart-donut__track"
              [attr.cx]="size / 2"
              [attr.cy]="size / 2"
              [attr.r]="radius"
              [attr.stroke-width]="strokeWidth"
            />
          }
          @for (arc of arcs(); track arc.label) {
            <circle
              class="chart-donut__arc"
              [class]="'chart-donut__arc--' + arc.color"
              [attr.cx]="size / 2"
              [attr.cy]="size / 2"
              [attr.r]="radius"
              [attr.stroke-width]="strokeWidth"
              [attr.stroke-dasharray]="arc.dashArray"
              [attr.stroke-dashoffset]="arc.dashOffset"
            />
          }
        </g>
        <text class="chart-donut__total" x="50%" y="50%" dominant-baseline="central" text-anchor="middle">
          {{ total() }}
        </text>
      </svg>
      <ul class="chart-donut__legend">
        @for (segment of segments(); track segment.label) {
          <li class="chart-donut__legend-item">
            <span class="chart-donut__swatch" [class]="'chart-donut__swatch--' + segment.color"></span>
            {{ segment.label }} ({{ segment.value }})
          </li>
        }
      </ul>
    </div>
  `,
  styles: `
    .chart-donut {
      display: flex;
      align-items: center;
      gap: 1.25rem;
      flex-wrap: wrap;
    }

    .chart-donut__svg {
      width: 120px;
      height: 120px;
      flex-shrink: 0;
    }

    .chart-donut__track {
      fill: none;
      stroke: var(--mat-sys-surface-variant);
    }

    .chart-donut__arc {
      fill: none;
      stroke-linecap: round;
      transition: stroke-dasharray 0.3s ease;
    }

    .chart-donut__arc--primary {
      stroke: var(--mat-sys-primary);
    }
    .chart-donut__arc--success {
      stroke: #1e8e3e;
    }
    .chart-donut__arc--warning {
      stroke: #b06000;
    }
    .chart-donut__arc--danger {
      stroke: #b3261e;
    }
    .chart-donut__arc--neutral {
      stroke: var(--mat-sys-outline);
    }

    .chart-donut__total {
      font-size: 1.25rem;
      font-weight: 600;
      fill: var(--mat-sys-on-surface);
    }

    .chart-donut__legend {
      list-style: none;
      margin: 0;
      padding: 0;
      display: flex;
      flex-direction: column;
      gap: 0.375rem;
      font-size: 0.875rem;
      color: var(--mat-sys-on-surface-variant);
    }

    .chart-donut__legend-item {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .chart-donut__swatch {
      width: 0.625rem;
      height: 0.625rem;
      border-radius: 999px;
      flex-shrink: 0;
    }

    .chart-donut__swatch--primary {
      background: var(--mat-sys-primary);
    }
    .chart-donut__swatch--success {
      background: #1e8e3e;
    }
    .chart-donut__swatch--warning {
      background: #b06000;
    }
    .chart-donut__swatch--danger {
      background: #b3261e;
    }
    .chart-donut__swatch--neutral {
      background: var(--mat-sys-outline);
    }
  `,
})
export class ChartDonutComponent {
  readonly segments = input.required<DonutChartSegment[]>();
  readonly ariaLabel = input<string>('Donut chart');

  protected readonly size = SIZE;
  protected readonly radius = RADIUS;
  protected readonly strokeWidth = STROKE_WIDTH;

  protected readonly total = computed(() =>
    this.segments().reduce((sum, segment) => sum + segment.value, 0),
  );

  protected readonly arcs = computed(() => {
    const total = this.total();
    if (total === 0) {
      return [];
    }

    let offsetSoFar = 0;
    return this.segments()
      .filter((segment) => segment.value > 0)
      .map((segment) => {
        const fraction = segment.value / total;
        const arcLength = fraction * CIRCUMFERENCE;
        const arc = {
          label: segment.label,
          color: segment.color,
          dashArray: `${arcLength} ${CIRCUMFERENCE - arcLength}`,
          dashOffset: -offsetSoFar,
        };
        offsetSoFar += arcLength;
        return arc;
      });
  });
}
