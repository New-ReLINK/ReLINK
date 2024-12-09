package com.my.relink.domain.point;


import com.my.relink.domain.BaseEntity;
import com.my.relink.domain.user.User;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Point extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private Integer amount;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public void deduct(Integer amountToDeduct){
        if(amount-amountToDeduct<0){
            throw new BusinessException(ErrorCode.POINT_SHORTAGE);
        }
        this.amount -= amountToDeduct;
    }


}
