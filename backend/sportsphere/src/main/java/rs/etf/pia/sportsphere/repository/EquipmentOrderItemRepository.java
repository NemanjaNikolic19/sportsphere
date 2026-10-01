package rs.etf.pia.sportsphere.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import rs.etf.pia.sportsphere.entity.EquipmentOrderItem;
import java.util.List;


public interface EquipmentOrderItemRepository extends JpaRepository<EquipmentOrderItem, Long> {
    
    List<EquipmentOrderItem> findByOrderId(Long orderId);

    List<EquipmentOrderItem> findByOrderIdIn(List<Long> orderIds);
}
