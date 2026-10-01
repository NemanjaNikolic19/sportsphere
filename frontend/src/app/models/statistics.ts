export interface Statistics {
  playedBySport: Record<string, number>;
  reservedBySport: Record<string, number>;
  monthlyActivity: Record<string, number>;

  equipmentSpendingByAthlete:
    Record<string, number>;

  totalEquipmentSpendingAllAthletes: number;
}