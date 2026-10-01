import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Sport } from '../../../models/sport';
import { RegisterRequest } from '../../../models/user';
import { AuthService } from '../../../services/auth.service';
import { PublicService } from '../../../services/public.service';
import { Avatar, Style } from '@dicebear/core';
import lorelei from '@dicebear/styles/lorelei.json' with { type: 'json' };
import { toPng } from '@dicebear/converter';

const avatarStyle = new Style(lorelei);

@Component({
  selector: 'app-register',
  imports: [FormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register implements OnInit, OnDestroy {
  
  @ViewChild('profileImageInput')
  profileImageInput?: ElementRef<HTMLInputElement>;

  profileImage: File | null = null;
  profileImagePreview: string | null = null;

  private readonly maxProfileImageSize = 5 * 1024 * 1024;

  generatedAvatarSvg: string | null = null;
  generatedAvatarPreview: string | null = null;

  savingGeneratedAvatar = false;
  avatarMessage = '';

  sports: Sport[] = [];

  request: RegisterRequest = {
    username: '',
    password: '',
    role: 'ATHLETE',

    firstName: '',
    lastName: '',
    phone: '',
    email: '',

    favoriteSportIds: [],

    organizationName: '',
    headquartersAddress: '',
    registrationNumber: '',
    taxNumber: ''
  }

  confirmPassword = '';

  loading = false;
  loadingSports = false;
  errorMessage = '';
  successMessage= '';

  constructor(
    private authService: AuthService,
    private publicService: PublicService
  ){
  }

  ngOnInit(): void {
    this.loadSports();
  }

  loadSports(): void {
    this.loadingSports = true;

    this.publicService.getSports().subscribe({
      next: sports => {
        this.sports = sports;
        this.loadingSports = false;
      },
      error: err => {
        this.loadingSports = false;
        this.errorMessage = err.error?.message || 'Could not load sports.';
      }
    });
  }

  isEmployee(): boolean {
    return this.request.role === 'EMPLOYEE';
  }

  isFavoriteSportSelected(sportId: number): boolean {
    return this.request.favoriteSportIds.includes(sportId);
  }

  toggleFavoriteSport(sportId: number, event: Event): void {
    const checkbox = event.target as HTMLInputElement;
    
    if(checkbox.checked) {
      if(this.request.favoriteSportIds.length >= 5) {
        checkbox.checked = false;
        this.errorMessage = 'You can choose at most five favorite sports.';
      }

      this.request.favoriteSportIds.push(sportId);
      this.errorMessage = '';
      return;
    }

    this.request.favoriteSportIds = this.request.favoriteSportIds.filter(id => id !== sportId);
  }

  resetForm(): void {
    this.request = {
      username: '',
      password: '',
      role: 'ATHLETE',

      firstName: '',
      lastName: '',
      phone: '',
      email: '',

      favoriteSportIds: [],

      organizationName: '',
      headquartersAddress: '',
      registrationNumber: '',
      taxNumber: ''
    }

    this.confirmPassword = '';

    this.removeProfileImage();
    this.clearGeneratedAvatar();
  }

  submit(): void  {
    this.errorMessage = '';
    this.successMessage = '';

    if(this.request.password !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match.';
      return;
    }

    if(!this.isEmployee()) {
      this.request.organizationName = '';
      this.request.headquartersAddress = '';
      this.request.registrationNumber = '';
      this.request.taxNumber = '';
    }

    this.loading = true;

    this.authService.register(this.request, this.profileImage).subscribe({
      next: response => {
        this.loading = false;
        this.successMessage = response.message;
        this.resetForm();
      },
      error: err => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Registration failed.';
      }
    });
  }

  onProfileImageSelected(event: Event): void {
    this.errorMessage = '';

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

    if(file.size > this.maxProfileImageSize) {
      this.errorMessage =
        'Profile image must not exceed 5 MB.';

      input.value = '';
      return;
    }

    this.clearGeneratedAvatar();
    this.setProfileImage(file);
  }

  private setProfileImage(file: File): void {
    
    this.revokeProfileImagePreview();

    this.profileImage = file;
    this.profileImagePreview =
      URL.createObjectURL(file);

    console.log('Selected file:', this.profileImage);
    console.log('Preview URL:', this.profileImagePreview);
    console.log('File size:', file.size);
  }

  removeProfileImage(): void {
    this.revokeProfileImagePreview();

    this.profileImage = null;
    this.profileImagePreview = null;

    this.avatarMessage = '';

    if(this.profileImageInput) {
      this.profileImageInput
        .nativeElement.value = '';
    }
  }

  private revokeProfileImagePreview(): void {
    if(this.profileImagePreview !== null) {
      URL.revokeObjectURL(
        this.profileImagePreview
      );
    }
  }

  ngOnDestroy(): void {
    this.revokeProfileImagePreview();
  }

  generateAvatar(): void {
    this.errorMessage = '';
    this.avatarMessage = '';

    const username = this.request.username.trim() || 'sportsphere-user';
    const seed = `${username}-${Date.now()}-${Math.random()}`;

    const avatar = new Avatar(
      avatarStyle,
      {
        seed: seed,
        size: 256,
        backgroundColor: ['#dbeafe']
      }
    );

    this.generatedAvatarSvg = avatar.toString();
    this.generatedAvatarPreview = avatar.toDataUri();
  }

  async saveGeneratedAvatar(): Promise<void> {
    if(this.generatedAvatarSvg === null) {
      this.errorMessage = 'Generate an avatar first.';

      return;
    }

    this.savingGeneratedAvatar = true;
    this.errorMessage = '';
    this.avatarMessage = '';

    try {
      const buffer = await toPng(
        this.generatedAvatarSvg,
        {
          size: 512
        }
      ).toArrayBuffer();

      const pngBytes = new Uint8Array(buffer.byteLength);

      pngBytes.set(new Uint8Array(buffer));

      const file = new File(
        [pngBytes],
        `generated-avatar-${Date.now()}.png`,
        {
          type: 'image/png'
        }
      );

      if(file.size > this.maxProfileImageSize) {
        throw new Error(
          'Generated image exceeds 5 MB.'
        );
      }

      this.setProfileImage(file);

      if(this.profileImageInput) {
        this.profileImageInput.nativeElement.value = '';
      }

      this.avatarMessage = 'Avatar saved as the selected profile image.';

    } catch(error) {
      console.error(error);

      this.errorMessage = 'Could not convert the avatar to PNG.';

    } finally {
      this.savingGeneratedAvatar = false;
    }
  }

  private clearGeneratedAvatar(): void {
    this.generatedAvatarSvg = null;
    this.generatedAvatarPreview = null;
    this.avatarMessage = '';
  }
}
