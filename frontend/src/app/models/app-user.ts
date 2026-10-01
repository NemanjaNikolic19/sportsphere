export interface AppUser {
    id: number;
    username: string;
    role: string;
    status: string;
    firstName: string;
    lastName: string;
    phone: string;
    email: string;
    profileImagePath: string | null;
    createdAt: string;
}
