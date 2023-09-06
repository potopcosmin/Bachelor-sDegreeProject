package com.licenta.server.CarRepair;

import com.licenta.server.UserService.User;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "car_services")
public class CarService  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "car_service_id", nullable = false)
    private Integer id;

    @Column(name = "service_name", length = 40)
    private String serviceName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_manager_id")
    private User serviceManager;

    @Column(name = "address", length = Integer.MAX_VALUE)
    private String address;

    @Column(name = "city", length = Integer.MAX_VALUE)
    private String city;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

}