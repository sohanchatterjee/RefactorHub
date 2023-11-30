package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.extension.jsonb
import jp.ac.titech.cs.se.refactorhub.app.model.*
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.File
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.FileMapping
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Table
import org.koin.java.KoinJavaComponent
import java.util.UUID

object Snapshots : UUIDTable("snapshots") {
    val files = jsonb("files", ::stringifyFileList, ::parseFileList)
    val fileMappings = jsonb("file_mappings", ::stringifyFileMappings, ::parseFileMappings)
    val patch = text("patch")
}

object SnapshotToChanges : Table("snapshot_to_changes") {
    val snapshot = reference("snapshot", Snapshots)
    val change = reference("change", Changes)
    override val primaryKey = PrimaryKey(snapshot, change)
}

class SnapshotDao(id: EntityID<UUID>) : UUIDEntity(id), ModelConverter<Snapshot> {
    companion object : UUIDEntityClass<SnapshotDao>(Snapshots)

    var files by Snapshots.files
    var fileMappings by Snapshots.fileMappings
    var patch by Snapshots.patch
    var changes by ChangeDao via SnapshotToChanges

    override fun asModel() = Snapshot(
        this.id.value,
        this.files,
        this.fileMappings,
        this.patch,
        this.changes.map { it.asModel() }
    )
}

private fun stringifyFileList(data: List<File>): String {
    val mapper by KoinJavaComponent.inject(ObjectMapper::class.java)
    return mapper.writeValueAsString(data)
}
private fun parseFileList(src: String): List<File> {
    val mapper by KoinJavaComponent.inject(ObjectMapper::class.java)
    return mapper.readValue(src)
}

private fun stringifyFileMappings(data: List<FileMapping>): String {
    val mapper by KoinJavaComponent.inject(ObjectMapper::class.java)
    return mapper.writeValueAsString(data)
}
private fun parseFileMappings(src: String): List<FileMapping> {
    val mapper by KoinJavaComponent.inject(ObjectMapper::class.java)
    return mapper.readValue(src)
}
