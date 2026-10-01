export interface TeammateAd {
    id: number;
    athleteId: number;
    sportId: number;
    city: string;
    playDate: string;
    startTime: string;
    endTime: string;
    neededPlayers: number;
    acceptedPlayers: number;
    status: string;
    createdAt: string;
}