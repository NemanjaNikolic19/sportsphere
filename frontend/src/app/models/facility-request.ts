export interface FacilityRequest {
    name: string;
    city: string;
    address: string;
    description: string;
    pricePerHour: number;
    workingTimeFrom: string;
    workingTimeTo: string;
    allowedNoShows: number;
}