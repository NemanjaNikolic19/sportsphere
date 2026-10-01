export interface Reservation {
    id: number;
    athleteId: number;
    facilityId: number;
    resourceId: number;
    sportId: number;
    startTime: string;
    endTime: string;
    status: string;
    createdAt: string;
}