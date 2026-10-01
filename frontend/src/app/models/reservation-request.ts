export interface ReservationRequest {
    facilityId: number;
    resourceId: number;
    sportId: number;
    startTime: string;
    endTime: string;
}