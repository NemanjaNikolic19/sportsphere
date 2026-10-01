export interface EquipmentRequest {
    facilityId: number;
    sportId: number;
    name: string;
    imagePath: string | null;
    price: number;
    stock: number;
    active: boolean;
}