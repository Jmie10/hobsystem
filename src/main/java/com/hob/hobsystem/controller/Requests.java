package com.hob.hobsystem.controller;

import com.hob.hobsystem.model.OrderLine;
import java.util.List;

/** Shapes of the JSON bodies the web page sends. */
public class Requests {
    public record SignUpReq(String firstName, String middleName, String lastName, String email, String contactNumber, String password) {}
    public record LoginReq(String id, String password) {}
    public record ForgotReq(String email) {}
    public record ResetReq(String email, String code, String newPassword) {}
    public record ChangePwReq(String currentPassword, String newPassword) {}
    public record ChangeEmailReq(String email, String currentPassword) {}
    public record OrderReq(int tableNo, String orderType, String notes, List<OrderLine> items) {}
    public record ReserveReq(int tableNo, String date, String time, int guests) {}
    public record WaitReq(String date, String time, int guests) {}
    public record ItemReq(String name, String category, int price) {}
}
