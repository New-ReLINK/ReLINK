package com.my.relink.domain.user;

import com.my.relink.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Where;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
@Getter
@SQLRestriction("is_deleted = false")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20)
    private String name;

    @Column(length = 20, unique = true)
    private String nickname;

    @Column(length = 30, unique = true)
    private String email;

    @Column(length = 60)
    private String password;

    @Column(length = 20)
    private String contact;

    private boolean isDeleted = false;

    @Enumerated(value = EnumType.STRING)
    private Role role;

    @Embedded
    private Address address;

}
