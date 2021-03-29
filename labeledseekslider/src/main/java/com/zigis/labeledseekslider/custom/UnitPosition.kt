package com.zigis.labeledseekslider.custom

enum class UnitPosition(val value: Int) {
    FRONT(0),
    BACK(1);

    companion object {
        fun parse(value: Int?): UnitPosition {
            return values().firstOrNull { it.value == value } ?: BACK
        }
    }
}