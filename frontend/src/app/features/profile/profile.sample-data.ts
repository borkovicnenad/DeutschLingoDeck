import { Profile } from './models/profile.model';

/** Static placeholder shown until the real profile loads (or if it fails to). */
export const SAMPLE_PROFILE: Profile = {
  id: 9001,
  email: 'nenad@example.com',
  displayName: 'Nenad',
  createdAt: '2026-01-15T10:00:00Z',
};
