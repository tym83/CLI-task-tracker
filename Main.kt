package tasklist

import kotlinx.datetime.*
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

fun main() {
    val tasks = TaskList()
    tasks.doMainMenu()
}

class TaskList {
    private val taskList = mutableListOf<Task>()

    private val jsonFileName = File("tasklist.json")

    private val priorityColors = mutableMapOf(
        "C" to "\u001B[101m \u001B[0m",
        "H" to "\u001B[103m \u001B[0m",
        "N" to "\u001B[102m \u001B[0m",
        "L" to "\u001B[104m \u001B[0m",
    )

    private val dueTagColors = mutableMapOf(
        "T" to "\u001B[103m \u001B[0m",
        "I" to "\u001B[102m \u001B[0m",
        "O" to "\u001B[101m \u001B[0m",
    )

    data class Task(
        var number: Int,
        var dateTime: LocalDateTime,
        var taskPriority: String,
        var task: MutableList<String>
    )

    data class TaskForJson(
        val number: Int,
        val dateTime: String,
        val taskPriority: String,
        val task: MutableList<String>
    )

    fun doMainMenu() {
        readDataFromJson()
        while (true) {
            println("Input an action (add, print, edit, delete, end):")
            when (readln()) {
                "add" -> buildTask()
                "print" -> printTaskList()
                "edit" -> editTask()
                "delete" -> deleteTask()
                "end" -> saveTasksToJson().also { return }
                else -> println("The input action is invalid")
            }
        }
    }

    private fun buildTask() {
        val taskPriority = setTaskPriority()
        val date = buildTimeAndDate(inputDate())
        val taskNumber = (taskList.size + 1)
        val task = addTask()

        if (task.isEmpty()) return else taskList.add(Task(taskNumber, date, taskPriority, task))
    }

    private fun setTaskPriority(): String {
        while (true) {
            println("Input the task priority (C, H, N, L):")
            when (readln().uppercase()) {
                "H" -> {
                    return "H"
                }
                "C" -> {
                    return "C"
                }
                "N" -> {
                    return "N"
                }
                "L" -> {
                    return "L"
                }
            }
        }
    }

    private fun inputDate(): LocalDate {
        while (true) {
            println("Input the date (yyyy-mm-dd):")
            val inputDate = readln()
            try {
                val (year, month, day) = inputDate.split("-").map { it.toInt() }
                return LocalDate(year, month, day)
            } catch (e: Exception) {
                println("The input date is invalid")
            }
        }
    }

    private fun buildTimeAndDate(date: LocalDate): LocalDateTime {
        while (true) {
            println("Input the time (hh:mm):")
            val inputTime = readln()
            try {
                val (hours, minute) = inputTime.split(":").map { it.toInt() }
                return LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, hours, minute)
            } catch (e: Exception) {
                println("The input time is invalid")
            }
        }
    }

    private fun addTask(): MutableList<String> {
        println("Input a new task (enter a blank line to end):")
        val task = mutableListOf<String>()

        while (true) {
            val input = readln().trim()
            when {
                (input.isEmpty() || input.isBlank()) && task.isEmpty() -> {
                    println("The task is blank")
                    break
                }
                input.isEmpty() || input.isBlank() -> {
                    break
                }
                else -> {
                    task.add(input)
                }
            }
        }
        return task
    }

    private fun editTask() {
        printTaskList()
        if (taskList.size == 0) {
            return
        } else {
            while (true) {
                println("Input the task number (1-${taskList.size}):")
                val taskNumber = readln()
                if (taskNumber.matches("\\d*".toRegex()) && taskNumber.toInt() in 1..taskList.size) {
                    while (true) {
                        val editedTask = taskList[taskNumber.toInt() - 1]
                        println("Input a field to edit (priority, date, time, task):")
                        when (readln()) {
                            "priority" -> {
                                editedTask.taskPriority = setTaskPriority()
                                break
                            }
                            "date" -> {
                                val newDate = inputDate()
                                editedTask.dateTime = LocalDateTime(
                                    newDate.year,
                                    newDate.monthNumber,
                                    newDate.dayOfMonth,
                                    editedTask.dateTime.hour,
                                    editedTask.dateTime.minute
                                )
                                break
                            }
                            "time" -> {
                                editedTask.dateTime = buildTimeAndDate(
                                    LocalDate(editedTask.dateTime.year,
                                        editedTask.dateTime.monthNumber,
                                        editedTask.dateTime.dayOfMonth
                                    )
                                )
                                break
                            }
                            "task" -> {
                                editedTask.task = addTask()
                                break
                            }
                            else -> {
                                println("Invalid field")
                            }
                        }
                    }
                    println("The task is changed")
                    return
                } else {
                    println("Invalid task number")
                }
            }
        }
    }

    private fun deleteTask() {
        printTaskList()
        if (taskList.size == 0) {
            return
        } else {
            while (true) {
                println("Input the task number (1-${taskList.size}):")
                val taskNumber = readln()
                if (taskNumber.matches("\\d*".toRegex()) && taskNumber.toInt() in 1..taskList.size) {
                    taskList.removeAt(taskNumber.toInt() - 1)
                    for (i in taskNumber.toInt() - 1.. taskList.lastIndex) {
                        taskList[i].number = i + 1
                    }
                    println("The task is deleted")
                    break
                } else {
                    println("Invalid task number")
                }
            }
        }
    }

    private fun printTaskList() {
        if (taskList.size == 0) {
            println("No tasks have been input")
            return
        }
        println("+----+------------+-------+---+---+--------------------------------------------+\n" +
                "| N  |    Date    | Time  | P | D |                   Task                     |\n" +
                "+----+------------+-------+---+---+--------------------------------------------+")
        taskList.forEach { println(convertTaskToPrint(it)) }
    }

    private fun convertTaskToPrint(eachTask: Task): String {
        val taskAsShortString = mutableListOf<String>()
        eachTask.task.forEach { taskAsShortString.addAll(it.chunked(44)) }

        var taskInString = ""
        for (i in taskAsShortString.indices) {
            taskInString += if (i == 0) {
                "| ${eachTask.number}${" ".repeat(3 - eachTask.number.toString().length)}" +
                        "| ${eachTask.dateTime.toString().replace("T", " | ")} " +
                        "| ${priorityColors[eachTask.taskPriority]} " + "| ${dueTagColors[makeDueTag(eachTask)]} " +
                        "|${taskAsShortString[i]}${" ".repeat(44 - taskAsShortString[i].length)}|\n"
            } else {
                "|${" ".repeat(4)}|${" ".repeat(12)}|${" ".repeat(7)}|${" ".repeat(3)}" +
                        "|${" ".repeat(3)}|${taskAsShortString[i]}" +
                        "${" ".repeat(44 - taskAsShortString[i].length)}|\n"
            }
        }
        taskInString += "+----+------------+-------+---+---+--------------------------------------------+"

        return taskInString
    }

    private fun makeDueTag(task: Task): String {
        val taskDate = LocalDate(task.dateTime.year, task.dateTime.monthNumber, task.dateTime.dayOfMonth)
        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0")).date
        return when {
            currentDate.daysUntil(taskDate) == 0 -> "T"
            currentDate.daysUntil(taskDate) > 0 -> "I"
            else -> "O"
        }
    }

    private fun readDataFromJson() {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val taskListFromJson = mutableListOf<TaskForJson>()

        if (jsonFileName.exists()) {
            val type = Types.newParameterizedType(
                MutableList::class.java,
                TaskForJson::class.java
            )

            val taskListAdapter = moshi.adapter<MutableList<TaskForJson?>?>(type)
            val jsonTasks = taskListAdapter.fromJson(jsonFileName.readText())

            if (!jsonTasks.isNullOrEmpty()) jsonTasks.forEach { if (it != null) taskListFromJson.add(it) }

            convertDataFromJson(taskListFromJson)
        }
    }

    private fun convertDataFromJson(taskListFromJson: MutableList<TaskForJson>) {
        for (task in taskListFromJson) {
            val taskPriority = task.taskPriority
            val (year, month, day, hour, minutes) = task.dateTime.split("([-:T])".toRegex()).map { it.toInt() }
            val date = LocalDateTime(year, month, day, hour, minutes)
            val taskText = task.task
            val number = task.number

            taskList.add(Task(number, date, taskPriority, taskText))
        }
    }

    private fun saveTasksToJson() {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val type = Types.newParameterizedType(
            MutableList::class.java,
            TaskForJson::class.java,
        )

        val taskAdapter = moshi.adapter<MutableList<TaskForJson>>(type)
        if (!jsonFileName.exists()) jsonFileName.createNewFile()

        val saveList = mutableListOf<TaskForJson>()
        taskList.forEach { saveList.add(TaskForJson(it.number, it.dateTime.toString(), it.taskPriority, it.task)) }
        jsonFileName.writeText(taskAdapter.toJson(saveList))

        println("Tasklist exiting!")
    }
}