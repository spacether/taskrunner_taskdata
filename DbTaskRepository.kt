package io.taskdata

import io.taskdata.db.TaskDAO
import io.taskdata.db.TaskTable
import io.taskdata.db.daoToModel
import io.taskdata.db.suspendTransaction
import io.taskmodels.Task
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class DbTaskRepository: TaskRepository {
    override suspend fun allTasks(): List<Task> = suspendTransaction {
        TaskDAO.all().map(::daoToModel)
    }

    override suspend fun addTask(task: Task): Unit = suspendTransaction {
        TaskDAO.new {
            name = EntityID(task.name, TaskTable)
            fileName = task.fileName
            hour = task.hour
            minute = task.minute
        }
    }

    override suspend fun removeTask(name: String): Boolean = suspendTransaction {
        val rowsDeleted = TaskTable.deleteWhere {
            TaskTable.id eq name
        }
        rowsDeleted == 1
    }

    override suspend fun queryTasks(hour: Int, minute: Int): List<Task> {
        return suspendTransaction {
            val condition = Op.build {
                ((TaskTable.hour eq null) or (TaskTable.hour eq hour))
                    .and((TaskTable.minute eq null) or (TaskTable.minute eq minute))
            }
            val query = TaskTable.select(condition)
            val taskDaos = query.map{ TaskDAO.wrapRow(it) }
            taskDaos.map { daoToModel(it) }
        }
    }
}