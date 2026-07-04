export interface Profile {
  id: number;
  email: string;
  displayName: string;
  createdAt: string;
}

export interface UpdateProfileRequest {
  displayName: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}
