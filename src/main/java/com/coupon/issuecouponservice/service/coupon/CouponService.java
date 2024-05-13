package com.coupon.issuecouponservice.service.coupon;

import com.coupon.issuecouponservice.domain.coupon.Coupon;
import com.coupon.issuecouponservice.domain.coupon.UserCoupon;
import com.coupon.issuecouponservice.domain.user.User;
import com.coupon.issuecouponservice.dto.request.coupon.CouponCreationParam;
import com.coupon.issuecouponservice.dto.request.coupon.CouponIssueParam;
import com.coupon.issuecouponservice.dto.request.coupon.CouponModificationParam;
import com.coupon.issuecouponservice.dto.response.coupon.CouponForm;
import com.coupon.issuecouponservice.dto.response.coupon.CouponOneForm;
import com.coupon.issuecouponservice.repository.coupon.CouponRepository;
import com.coupon.issuecouponservice.repository.coupon.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;

    // 쿠폰 생성
    public void createCoupon(CouponCreationParam param) {
        checkForDuplicateCouponName(param.getCouponName()); // 쿠폰 이름 중복 검증

        Coupon coupon = Coupon.CreateCoupon(param);

        couponRepository.save(coupon);
    }

    // 쿠폰 전체 조회
    @Transactional(readOnly = true)
    public List<CouponForm> readAllCoupons() {
        List<Coupon> findCoupons = couponRepository.findAllCoupons();

        return findCoupons.stream()
                .map(CouponForm::new)
                .collect(Collectors.toList());
    }

    // 쿠폰 수정
    public void modifyCoupon(Long couponId, CouponModificationParam param) {
        checkForDuplicateCouponName(param.getCouponName()); // 쿠폰 이름 중복 검증

        Coupon findCoupon = getCoupon(couponId);

        findCoupon.modifyCoupon(param);
    }

    // 쿠폰 삭제
    public void deleteCoupon(Long couponId) {
        Coupon findCoupon = getCoupon(couponId);

        findCoupon.deleteCoupon();
    }


    // 쿠폰 조회
    private Coupon getCoupon(Long couponId) {
        return couponRepository.findOneCouponByCouponId(couponId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));
    }

    // 쿠폰 검증
    private void checkForDuplicateCouponName(String couponName) {
        boolean exists = couponRepository.existsByCouponName(couponName);

        if (exists) {
            throw new IllegalArgumentException("이미 존재하는 쿠폰명입니다.");
        }
    }

    // 쿠폰 상세 조회
    @Transactional(readOnly = true)
    public CouponOneForm selectCoupon(Long couponId) {
        Coupon coupon = getCoupon(couponId);
        return new CouponOneForm(coupon);
    }

    // 쿠폰 발급
    public void issueCoupon(CouponIssueParam couponIssueParam, User user) {
        Coupon coupon = getCoupon(couponIssueParam.getCouponId());
        coupon.validateCoupon(couponIssueParam.getCouponId());
        UserCoupon userCoupon = UserCoupon.CreateUserCoupon(coupon, user);
        userCouponRepository.save(userCoupon);
    }

    // 사용자 쿠폰 전체 조회
/*    @Transactional(readOnly = true)
    public List<CouponForm> readAllUserCoupons(Long userId) {
        System.out.println("======================= 유저 쿠폰 =======================");
        List<UserCoupon> findUserCoupons = userCouponRepository.findByUserId(userId);

        System.out.println("======================= 쿠폰 조회 =======================");
        List<Coupon> findCoupons = findUserCoupons.stream()
                .map(UserCoupon::getCoupon).toList();

        System.out.println("====================== 쿠폰 FORM 반환 =======================");
        return findCoupons.stream()
                .map(CouponForm::new).toList();
    }*/

    // 사용자 쿠폰 전체 조회 V1 => where ~ in
    @Transactional(readOnly = true)
    public List<CouponForm> readAllUserCouponsV1(Long userId) {
        System.out.println("======================= 유저 쿠폰 =======================");
        List<UserCoupon> findUserCoupons = userCouponRepository.findByUserId(userId);

        System.out.println("======================= CouponIds =======================");
        List<Long> couponIds = findUserCoupons.stream()
                .map(uc -> uc.getCoupon().getId()).toList();

        System.out.println("======================= 쿠폰 =======================");
        List<Coupon> findAllCoupons = couponRepository.findAllByIdIn(couponIds);

        System.out.println("======================= 쿠폰 반환 =======================");
        return findAllCoupons.stream().map(CouponForm::new).toList();
    }

    // 사용자 쿠폰 전체 조회 V2 => batch size
/*    @Transactional(readOnly = true)
    public List<CouponForm> readAllUserCouponsV2(Long userId) {
       List<UserCoupon> findUserCoupons = userCouponRepository.findByUserId(userId);

        return findUserCoupons.stream()
                .map(UserCoupon::getCoupon)
                .map(CouponForm::new)
                .collect(Collectors.toList());
    }*/

    // 사용자 쿠폰 전체 조회 V3 => fetch join
 /*   @Transactional(readOnly = true)
    public List<CouponForm> readAllUserCouponsV3(Long userId) {
        System.out.println("======================= 유저 쿠폰 =======================");
        List<UserCoupon> findUserCoupons = userCouponRepository.findUserCouponsByUserId(userId);

        System.out.println("======================= 쿠폰 =======================");
        return findUserCoupons.stream()
                .map(uc -> new CouponForm(uc.getCoupon()))
                .toList();
    }*/

}
