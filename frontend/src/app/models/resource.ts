export interface Resource {
    id: number;
    facilityId: number;
    sportId: number;
    name: string;
    type: string;
    capacity: number;
    equipmentDescription: string;
    active: boolean;
}
