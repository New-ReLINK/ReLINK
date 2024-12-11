package com.my.relink.domain.point;


import com.my.relink.domain.BaseEntity;
import com.my.relink.domain.user.User;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Point extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private Integer amount;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public void deduct(Integer amountToDeduct) {
        if (amount - amountToDeduct < 0) {
            throw new BusinessException(ErrorCode.POINT_SHORTAGE);
        }
        this.amount -= amountToDeduct;
    }

    public void restore(Integer amountToAdd) {
        this.amount += amountToAdd;
    }

    // 테스트용 생성자
    public Point(Integer amount, User user) {
        this.amount = amount;
        this.user = user;
    }
}
