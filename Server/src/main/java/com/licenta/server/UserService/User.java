package com.licenta.server.UserService;

import com.licenta.server.CarRepair.CarService;
import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;


@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

   @Column(name="username",nullable = false)
    private String username;

    @Column(name="email",nullable = false)
    private String email;

    @Column(name="first_name",nullable = false)
    private String first_name;

    @Column(name="last_name",nullable = false)
    private String last_name;

    @OneToMany(mappedBy = "serviceManager")
    private Set<CarService> carServices = new LinkedHashSet<>();

    @Column(name = "type")
    private String type;


}
