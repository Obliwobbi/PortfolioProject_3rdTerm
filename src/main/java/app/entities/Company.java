package app.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "companies", uniqueConstraints = {
        @UniqueConstraint(name = "uq_company_name", columnNames = "name")
})
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Builder.Default
    @Column(name = "public_registration_enabled", nullable = false)
    private boolean publicRegistrationEnabled = true;

    // TODO: Add users collection later for bi-directional relationships.

}
