export interface Trainer {
    id: number;
    facilityId: number;
    sportId: number;
    firstName: string;
    lastName: string;
    specialization: string;
    averageRating: number;
    pricePerHour: number;
    active: boolean;
}