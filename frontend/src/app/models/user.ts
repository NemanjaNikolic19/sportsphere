export interface LoginRequest {
    username: string;
    password: string;
}

export interface User {
    id: number;
    username: string;
    role: string;
    status: string;
    firstName: string;
    lastName: string;
    email: string;
    profileImagePath: string | null;
}

export interface RegisterRequest {
  username: string;
  password: string;
  role: string;

  firstName: string;
  lastName: string;
  phone: string;
  email: string;

  favoriteSportIds: number[];

  organizationName: string;
  headquartersAddress: string;
  registrationNumber: string;
  taxNumber: string;
}

export interface RegisterResponse {
  id: number;
  username: string;
  role: string;
  status: string;
  message: string;
}