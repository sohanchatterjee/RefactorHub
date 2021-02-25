package jp.ac.titech.cs.se.refactorhub.core.dataset

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.RefactoringMiner
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.RefactoringOracle
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.refactoring.convertRefactoring
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.model.Refactoring
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

const val OUTPUTS_PATH = "outputs"

fun main() {
    generateDataset(
        types = listOf(
            "Extract Method",
            "Move Attribute",
            "Move Class",
            "Rename Variable"
        ),
        size = 4,
        sizePerCommit = 1..5,
        validation = "TP",
        tools = listOf("RefactoringMiner")
    )
}

private fun generateDataset(
    types: List<String>,
    size: Int,
    sizePerCommit: IntRange,
    validation: String,
    tools: List<String>
) {
    val refactorings =
        types.map { RefactoringOracle.getRefactorings(it, Int.MAX_VALUE, sizePerCommit, validation, tools) }
            .flatten().withIndex()
    writeToCsv("refactorings.csv", refactorings)

    val experiment = types.map { type ->
        refactorings
            .filter { it.value.type == type }
            .shuffled().take(size).sortedBy { it.index }
    }.flatten()
    writeToCsv("experiment-1.csv", experiment)
    writeToNdJson("experiment-1.ndjson", experiment, withDescription = true)
}

private fun getOutputFile(name: String): File {
    val file = File("$OUTPUTS_PATH/$name")
    file.parentFile.mkdirs()
    if (!file.exists()) file.createNewFile()
    return file
}

private fun writeToCsv(name: String, refactorings: Iterable<IndexedValue<RefactoringOracle.Refactoring>>) {
    val csv = getOutputFile(name)
    BufferedWriter(FileWriter(csv, true)).use { out ->
        out.appendLine(
            listOf(
                "ID",
                "Type",
                "Description",
                "Commit",
                "Size/Commit",
                "Others"
            ).joinToString("\",\"", "\"", "\"")
        )
        for ((i, refactoring) in refactorings) {
            out.appendLine(
                listOf(
                    "$i",
                    refactoring.type,
                    refactoring.description.replace("\"", "\"\""),
                    "https://github.com/${refactoring.commit.owner}/${refactoring.commit.repository}/commit/${refactoring.commit.sha}",
                    refactoring.others.size,
                    refactoring.others.joinToString("\n") { it.description.replace("\"", "\"\"") }
                ).joinToString("\",\"", "\"", "\"")
            )
        }
    }
}

private fun writeToNdJson(
    name: String,
    refactorings: Iterable<IndexedValue<RefactoringOracle.Refactoring>>,
    withDescription: Boolean = false,
    withReDetection: Boolean = false
) {
    val json = getOutputFile(name)

    for ((_, refactoring) in refactorings) {
        if (withReDetection) {
            try {
                RefactoringMiner.reDetect(refactoring) {
                    BufferedWriter(FileWriter(json, true)).use { out ->
                        out.appendLine(
                            jacksonObjectMapper().writeValueAsString(
                                convertRefactoring(it, refactoring)
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            BufferedWriter(FileWriter(json, true)).use { out ->
                out.appendLine(
                    jacksonObjectMapper().writeValueAsString(
                        Refactoring(
                            refactoring.type,
                            refactoring.commit,
                            description = if (withDescription) refactoring.description else ""
                        )
                    )
                )
            }
        }
    }
}
