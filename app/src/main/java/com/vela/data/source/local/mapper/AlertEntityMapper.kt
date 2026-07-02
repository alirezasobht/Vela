package com.vela.data.source.local.mapper

import com.vela.data.source.local.model.AlertEntity
import com.vela.domain.model.Alert
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType

fun AlertEntity.toDomain(): Alert = Alert(
    id = id,
    coinId = coinId,
    coinName = coinName,
    coinSymbol = coinSymbol,
    type = AlertType.valueOf(type),
    direction = AlertDirection.valueOf(direction),
    targetValue = targetValue,
    isTriggered = isTriggered,
    lastOhlcCheckTimestamp = lastOhlcCheckTimestamp,
    ohlcAnchorTimestamp = ohlcAnchorTimestamp
)

fun Alert.toEntity(): AlertEntity = AlertEntity(
    id = id,
    coinId = coinId,
    coinName = coinName,
    coinSymbol = coinSymbol,
    type = type.name,
    direction = direction.name,
    targetValue = targetValue,
    isTriggered = isTriggered,
    lastOhlcCheckTimestamp = lastOhlcCheckTimestamp,
    ohlcAnchorTimestamp = ohlcAnchorTimestamp
)
