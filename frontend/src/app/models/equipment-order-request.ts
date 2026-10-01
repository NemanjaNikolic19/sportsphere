export interface EquipmentOrderRequestItem {
    equipmentId: number;
    quantity: number;
}

export interface EquipmentOrderRequest {
    facilityId: number;
    items: EquipmentOrderRequestItem[];
}