import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

const PALETTE = ['#4f6bed', '#1e8e3e', '#b06000', '#b3261e', '#7c4dff', '#00838f'] as const;

function hashString(value: string): number {
  let hash = 0;
  for (let i = 0; i < value.length; i++) {
    hash = (hash << 5) - hash + value.charCodeAt(i);
    hash |= 0;
  }
  return Math.abs(hash);
}

function initialsOf(name: string): string {
  const words = name.trim().split(/\s+/).filter(Boolean);
  if (words.length === 0) {
    return '?';
  }
  const first = words[0][0] ?? '';
  const second = words.length > 1 ? (words[1][0] ?? '') : '';
  return (first + second).toUpperCase();
}

@Component({
  selector: 'app-avatar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <span
      class="avatar"
      [style.width.px]="size()"
      [style.height.px]="size()"
      [style.background]="color()"
      [style.font-size.px]="size() * 0.4"
    >
      {{ initials() }}
    </span>
  `,
  styles: `
    .avatar {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      border-radius: 999px;
      color: #fff;
      font-weight: 600;
      line-height: 1;
      flex-shrink: 0;
      user-select: none;
    }
  `,
})
export class AvatarComponent {
  readonly name = input.required<string>();
  readonly size = input<number>(32);

  protected readonly initials = computed(() => initialsOf(this.name()));
  protected readonly color = computed(() => PALETTE[hashString(this.name()) % PALETTE.length]);
}
