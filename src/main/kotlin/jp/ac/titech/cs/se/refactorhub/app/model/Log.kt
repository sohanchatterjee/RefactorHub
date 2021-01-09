package jp.ac.titech.cs.se.refactorhub.app.model

import com.fasterxml.jackson.databind.JsonNode
import jp.ac.titech.cs.se.refactorhub.tool.model.Log
import jp.ac.titech.cs.se.refactorhub.tool.model.LogEvent
import jp.ac.titech.cs.se.refactorhub.tool.model.LogType

data class Log(
    override val event: LogEvent,
    override val type: LogType,
    override val user: Int,
    override val time: Long,
    override val data: JsonNode
) : Log
