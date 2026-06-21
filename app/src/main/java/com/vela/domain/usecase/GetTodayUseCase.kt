package com.vela.domain.usecase

import java.time.LocalDate
import javax.inject.Inject

class GetTodayUseCase @Inject constructor() {
    operator fun invoke(): LocalDate = LocalDate.now()
}
