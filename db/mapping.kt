package io.taskdata.db

import io.taskmodels.Task
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object TaskTable : IdTable<String>(name="tasks") {
    override val id: Column<EntityID<String>> = varchar("name", 50).entityId()
    val fileName = varchar("filename", 50)
    val minute = integer("minute").nullable()
    val hour = integer("hour").nullable()
}

abstract class TextEntity(id: EntityID<String>) : Entity<String>(id)

abstract class TextEntityClass<out E : TextEntity>(table: IdTable<String>, entityType: Class<E>? = null) : EntityClass<String, E>(table, entityType)


class TaskDAO(id: EntityID<String>) : TextEntity(id) {
    companion object : TextEntityClass<TaskDAO>(TaskTable)

    var name by TaskTable.id
    var fileName by TaskTable.fileName
    var minute by TaskTable.minute
    var hour by TaskTable.hour
}

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)


fun daoToModel(dao: TaskDAO) = Task(
    name=dao.name.value,
    fileName=dao.fileName,
    hour=dao.hour,
    minute=dao.minute,
)
