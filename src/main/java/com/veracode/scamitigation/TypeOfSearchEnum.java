package com.veracode.scamitigation;

public enum TypeOfSearchEnum {
    OPEN_ISSUES,
    REJECTED_ISSUES,
    APPROVED_ISSUES;

    private TypeOfSearchEnum() {
    }

    public static TypeOfSearchEnum fromIndex(int selectedIndex) {
        switch (selectedIndex) {
            case 0:
                return OPEN_ISSUES;
            case 1:
                return REJECTED_ISSUES;
            case 2:
                return APPROVED_ISSUES;
            default:
                return null;
        }
    }
}
