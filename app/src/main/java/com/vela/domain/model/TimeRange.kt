package com.vela.domain.model

enum class TimeRange(val days: Int, val label: String) {
    ONE_DAY(1, "1D"),
    SEVEN_DAYS(7, "7D"),
    ONE_MONTH(30, "1M"),
    THREE_MONTHS(90, "3M"),
    ONE_YEAR(365, "1Y")
}
