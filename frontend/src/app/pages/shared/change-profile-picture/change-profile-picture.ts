import {
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  ViewChild
} from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { Observable } from 'rxjs';

import { Avatar, Style } from '@dicebear/core';
import { toPng } from '@dicebear/converter';
import lorelei from '@dicebear/styles/lorelei.json' with { type: 'json' };

import { AppUser } from '../../../models/app-user';
import { User } from '../../../models/user';
import { AthleteService } from '../../../services/athlete.service';
import { AuthService } from '../../../services/auth.service';
import { EmployeeService } from '../../../services/employee.service';

const avatarStyle = new Style(lorelei);

@Component({
  selector: 'app-change-profile-picture',
  imports: [RouterLink],
  templateUrl: './change-profile-picture.html',
  styleUrl: './change-profile-picture.css'
})
export class ChangeProfilePicture
  implements OnInit, OnDestroy {

  @ViewChild('profileImageInput')
  profileImageInput?:
    ElementRef<HTMLInputElement>;

  user: User | null = null;

  selectedImage: File | null = null;
  selectedImagePreview: string | null = null;

  generatedAvatarSvg: string | null = null;
  generatedAvatarPreview: string | null = null;

  savingGeneratedAvatar = false;
  uploading = false;
  removing = false;

  errorMessage = '';
  successMessage = '';

  private readonly maxImageSize =
    5 * 1024 * 1024;

  constructor(
    private authService: AuthService,
    private athleteService: AthleteService,
    private employeeService: EmployeeService,
    private router: Router
  ) {
  }

  ngOnInit(): void {
    this.user = this.authService.currentUser();

    if(!this.user) {
      this.errorMessage =
        'You must be logged in.';
      return;
    }

    if(
      this.user.role !== 'ATHLETE' &&
      this.user.role !== 'EMPLOYEE'
    ) {
      this.errorMessage =
        'Only athletes and employees can change a profile picture.';
    }
  }

  backRoute(): string {
    return this.user?.role === 'EMPLOYEE'
      ? '/employee/profile'
      : '/athlete/profile';
  }

  currentImageUrl(): string {
    const path = this.user?.profileImagePath;

    if(!path) {
      return '/api/profile-images/default.png';
    }

    if(path.startsWith('/api/profile-images/')) {
      return path;
    }

    return `/api/profile-images/${path}`;
  }

  isDefaultPicture(): boolean {
    const path = this.user?.profileImagePath;

    return (
      !path ||
      path === 'default.png' ||
      path.endsWith('/default.png')
    );
  }

  onProfileImageSelected(event: Event): void {
    this.errorMessage = '';
    this.successMessage = '';

    const input =
      event.target as HTMLInputElement;

    const file = input.files?.[0];

    if(!file) {
      return;
    }

    if(
      file.type !== 'image/png' &&
      file.type !== 'image/jpeg'
    ) {
      this.errorMessage =
        'Profile image must be PNG or JPG.';

      input.value = '';
      return;
    }

    if(file.size > this.maxImageSize) {
      this.errorMessage =
        'Profile image must not exceed 5 MB.';

      input.value = '';
      return;
    }

    this.clearGeneratedAvatar();
    this.setSelectedImage(file);
  }

  generateAvatar(): void {
    this.errorMessage = '';
    this.successMessage = '';

    const username =
      this.user?.username || 'sportsphere-user';

    const avatar = new Avatar(
      avatarStyle,
      {
        seed:
          `${username}-${Date.now()}-${Math.random()}`,
        size: 256,
        backgroundColor: ['#dbeafe']
      }
    );

    this.generatedAvatarSvg = avatar.toString();
    this.generatedAvatarPreview =
      avatar.toDataUri();
  }

  async useGeneratedAvatar(): Promise<void> {
    if(this.generatedAvatarSvg === null) {
      this.errorMessage =
        'Generate an avatar first.';
      return;
    }

    this.savingGeneratedAvatar = true;
    this.errorMessage = '';
    this.successMessage = '';

    try {
      const buffer = await toPng(
        this.generatedAvatarSvg,
        { size: 512 }
      ).toArrayBuffer();

      const pngBytes =
        new Uint8Array(buffer.byteLength);

      pngBytes.set(new Uint8Array(buffer));

      const file = new File(
        [pngBytes],
        `generated-avatar-${Date.now()}.png`,
        { type: 'image/png' }
      );

      this.setSelectedImage(file);

      if(this.profileImageInput) {
        this.profileImageInput.nativeElement.value = '';
      }

      this.successMessage =
        'Avatar selected. Press Save picture to upload it.';
    } catch(error) {
      console.error(error);

      this.errorMessage =
        'Could not convert the avatar to PNG.';
    } finally {
      this.savingGeneratedAvatar = false;
    }
  }

  savePicture(): void {
    if(!this.user || !this.selectedImage) {
      this.errorMessage =
        'Upload an image or select a generated avatar first.';
      return;
    }

    let request: Observable<AppUser>;

    if(this.user.role === 'ATHLETE') {
      request =
        this.athleteService.updateProfileImage(
          this.user.id,
          this.selectedImage
        );
    } else if(this.user.role === 'EMPLOYEE') {
      request =
        this.employeeService.updateProfileImage(
          this.user.id,
          this.selectedImage
        );
    } else {
      this.errorMessage =
        'Only athletes and employees can change a profile picture.';
      return;
    }

    this.uploading = true;
    this.errorMessage = '';
    this.successMessage = '';

    request.subscribe({
      next: updatedUser => {
        if(!this.user) {
          return;
        }

        this.user.profileImagePath =
          updatedUser.profileImagePath;

        this.authService
          .updateCurrentUserProfileImage(
            updatedUser.profileImagePath
          );

        this.clearSelectedImage();
        this.clearGeneratedAvatar();
        this.uploading = false;
        this.successMessage =
          'Profile picture updated successfully.';
      },
      error: err => {
        this.uploading = false;
        this.errorMessage =
          err.error?.message ||
          'Could not update profile picture.';
      }
    });
  }

  removePicture(): void {
    if(!this.user || this.isDefaultPicture()) {
      return;
    }

    let request: Observable<AppUser>;

    if(this.user.role === 'ATHLETE') {
      request =
        this.athleteService.removeProfileImage(
          this.user.id
        );
    } else if(this.user.role === 'EMPLOYEE') {
      request =
        this.employeeService.removeProfileImage(
          this.user.id
        );
    } else {
      this.errorMessage =
        'Only athletes and employees can change a profile picture.';
      return;
    }

    this.removing = true;
    this.errorMessage = '';
    this.successMessage = '';

    request.subscribe({
      next: updatedUser => {
        if(!this.user) {
          return;
        }

        this.user.profileImagePath =
          updatedUser.profileImagePath;

        this.authService
          .updateCurrentUserProfileImage(
            updatedUser.profileImagePath
          );

        this.clearSelectedImage();
        this.clearGeneratedAvatar();
        this.removing = false;
        this.successMessage =
          'Profile picture removed. The default picture is now active.';
      },
      error: err => {
        this.removing = false;
        this.errorMessage =
          err.error?.message ||
          'Could not remove profile picture.';
      }
    });
  }

  cancel(): void {
    this.router.navigateByUrl(this.backRoute());
  }

  clearSelectedImage(): void {
    this.revokeSelectedImagePreview();

    this.selectedImage = null;
    this.selectedImagePreview = null;

    if(this.profileImageInput) {
      this.profileImageInput.nativeElement.value = '';
    }
  }

  ngOnDestroy(): void {
    this.revokeSelectedImagePreview();
  }

  private setSelectedImage(file: File): void {
    this.revokeSelectedImagePreview();

    this.selectedImage = file;
    this.selectedImagePreview =
      URL.createObjectURL(file);
  }

  private revokeSelectedImagePreview(): void {
    if(this.selectedImagePreview !== null) {
      URL.revokeObjectURL(
        this.selectedImagePreview
      );
    }
  }

  private clearGeneratedAvatar(): void {
    this.generatedAvatarSvg = null;
    this.generatedAvatarPreview = null;
  }
}
