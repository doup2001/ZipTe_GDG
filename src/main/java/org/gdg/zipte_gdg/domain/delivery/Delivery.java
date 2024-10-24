package org.gdg.zipte_gdg.domain.delivery;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.gdg.zipte_gdg.domain.member.Address;
import org.gdg.zipte_gdg.domain.order.Order;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter

public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private Long id;

    @OneToOne
    private Order order;

    @Embedded
    private Address address;

    private String orderDesc;

    private String deliveryDesc;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DeliveryStatus status = DeliveryStatus.READY;


    // 로직
    public void update(Address update_address) {
        this.address = update_address;
    }

    public void deliveryIng() {
        this.status = DeliveryStatus.DELIVERING;
    }

    public void deliveryComplete() {
        this.status = DeliveryStatus.DELIVERY;
    }

    public void deliveryCancel() {
        this.status = DeliveryStatus.CANCEL;
    }

    // 비즈니스 로직 위한
    public void setOrder(Order order) {
        this.order = order;
    }


}