export interface TrainingSession {
    id: number;
    athleteId: number;
    trainerId: number;
    facilityId: number;
    resourceId: number;
    sportId: number;
    startTime: string;
    endTime: string;
    pricePerHour: number;
    status: string;
    createdAt: string;
}
