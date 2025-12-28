package org.example.army.militarycommon.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DeptTypeEnum {
    CORPS(1, "兵团/总部"),
    DIVISION(2, "师级"),
    REGIMENT(3, "团级"),
    BATTALION(4, "营级"),
    COMPANY(5, "连级");

    private final Integer code;
    private final String desc;
}

