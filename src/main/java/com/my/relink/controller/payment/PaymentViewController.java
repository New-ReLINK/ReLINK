package com.my.relink.controller.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PaymentViewController {
    @Value("${toss.client-key}")
    private String clientKey;

    @GetMapping("/charge/users/{userId}")
    public String getPointCharge(@PathVariable("userId") Long userId, Model model) {
        model.addAttribute("clientKey", clientKey);
        model.addAttribute("userId", userId);
        return "charge/charge-form";
    }

    @GetMapping("/charge-success")
    public String getPointChargeSuccess(
            @RequestParam("paymentKey") String paymentKey,
            @RequestParam("orderId") String orderId,
            @RequestParam("amount") String amount,
            Model model
    ) {
        model.addAttribute("paymentKey", paymentKey);
        model.addAttribute("orderId", orderId);
        model.addAttribute("amount", amount);
        return "charge/charge-success";
    }

    @GetMapping("/charge-fail")
    public String showFailPage(
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String code) {
        return "charge/charge-fail";
    }
}