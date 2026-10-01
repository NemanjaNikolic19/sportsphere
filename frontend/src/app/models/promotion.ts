export interface Promotion {
    id: number;
    facilityId: number;
    sportId: number | null;
    name: string;
    validFrom: string;
    validTo: string;
    discountType: string;
    discountValue: number;
    active: boolean;
    createdAt: string;
}
