package rs.etf.pia.sportsphere.dto;

import java.util.List;

public class EquipmentOrderRequest {

    private Long facilityId;
    private List<Item> items;

    public EquipmentOrderRequest() {
    }

    public Long getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(Long facilityId) {
        this.facilityId = facilityId;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }


    public static class Item {
        private Long equipmentId;
        private Integer quantity;
        
        public Item() {
        }

        public Long getEquipmentId() {
            return equipmentId;
        }

        public void setEquipmentId(Long equipmentId) {
            this.equipmentId = equipmentId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        
    }
}
