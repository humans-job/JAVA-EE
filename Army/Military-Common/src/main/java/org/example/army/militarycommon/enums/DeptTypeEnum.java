package org.example.army.militarycommon.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DeptTypeEnum {
    CORPS(10, "兵团/总部"),
    DIVISION(20, "师级"),
    REGIMENT(30, "团级"),
    BATTALION(40, "营级"),
    COMPANY(50, "连级");

    private final Integer code;
    private final String desc;
}

