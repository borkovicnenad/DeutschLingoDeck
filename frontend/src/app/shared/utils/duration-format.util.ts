/**
 * Formats a duration given in seconds as a short, human-readable string:
 * - under a minute: "35 sec"
 * - under an hour: "2 min" or "2 min 14 sec" (seconds omitted only when there's no remainder)
 * - an hour or more: "1 h" or "1 h 8 min" (seconds dropped entirely at this scale)
 */
export function formatDuration(totalSeconds: number): string {
  const seconds = Math.max(0, Math.round(totalSeconds));

  if (seconds < 60) {
    return `${seconds} sec`;
  }

  if (seconds < 3600) {
    const minutes = Math.floor(seconds / 60);
    const remainderSeconds = seconds % 60;
    return remainderSeconds > 0 ? `${minutes} min ${remainderSeconds} sec` : `${minutes} min`;
  }

  const hours = Math.floor(seconds / 3600);
  const remainderMinutes = Math.floor((seconds % 3600) / 60);
  return remainderMinutes > 0 ? `${hours} h ${remainderMinutes} min` : `${hours} h`;
}
