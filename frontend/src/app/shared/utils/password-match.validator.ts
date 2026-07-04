import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/** Cross-field validator ensuring two controls hold the same value. */
export function passwordsMatchValidator(
  passwordControlName: string,
  confirmControlName: string,
): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    const password = group.get(passwordControlName)?.value;
    const confirmPassword = group.get(confirmControlName)?.value;

    if (!password || !confirmPassword || password === confirmPassword) {
      return null;
    }

    return { passwordMismatch: true };
  };
}
