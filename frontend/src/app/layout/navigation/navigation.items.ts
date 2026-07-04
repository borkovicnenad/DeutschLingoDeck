export interface NavigationItem {
  label: string;
  icon: string;
  route: string;
}

/** Primary navigation entries shown in the application shell sidebar. */
export const NAVIGATION_ITEMS: readonly NavigationItem[] = [
  { label: 'Dashboard', icon: 'space_dashboard', route: '/dashboard' },
  { label: 'Dictionaries', icon: 'menu_book', route: '/dictionaries' },
  { label: 'Games', icon: 'sports_esports', route: '/games' },
  { label: 'Statistics', icon: 'insights', route: '/statistics' },
  { label: 'Profile', icon: 'person', route: '/profile' },
];
