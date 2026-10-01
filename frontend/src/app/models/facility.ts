export interface Facility {
    id: number;
    organizationId: number;
    name: string;
    city: string;
    address: string;
    description: string;
    pricePerHour: number;
    workingTimeFrom: string;
    workingTimeTo: string;
    allowedNoShows: number;
    status: string;
    likeCount: number;
    dislikeCount: number;
    createdAt: string;
}
