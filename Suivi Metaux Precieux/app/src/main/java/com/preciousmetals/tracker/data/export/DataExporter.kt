package com.preciousmetals.tracker.data.export

import com.preciousmetals.tracker.data.repository.HoldingRepository
import com.preciousmetals.tracker.domain.model.Holding
import com.preciousmetals.tracker.domain.model.Metal
import com.preciousmetals.tracker.domain.model.ObjectType
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class HoldingExportDto(
    val metal: String,
    val objectType: String,
    val grams: Double,
    val purchaseDateEpochDay: Long,
    val label: String,
    val pricePaidUsd: Double?,
    val notes: String,
)

@Serializable
data class ExportPayload(
    val version: Int = 1,
    val exportedAtEpochMillis: Long,
    val holdings: List<HoldingExportDto>,
)

private fun Holding.toExportDto() = HoldingExportDto(
    metal = metal.name,
    objectType = objectType.name,
    grams = grams,
    purchaseDateEpochDay = purchaseDate.toEpochDay(),
    label = label,
    pricePaidUsd = pricePaidUsd,
    notes = notes,
)

private fun HoldingExportDto.toDomain() = Holding(
    id = 0,
    metal = Metal.valueOf(metal),
    objectType = ObjectType.valueOf(objectType),
    grams = grams,
    purchaseDate = LocalDate.ofEpochDay(purchaseDateEpochDay),
    label = label,
    pricePaidUsd = pricePaidUsd,
    photoUri = null,
    notes = notes,
)

/** Exports/imports all holdings as a portable JSON document, for the manual backup Settings asked for. */
class DataExporter(private val holdingRepository: HoldingRepository) {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    suspend fun exportToStream(outputStream: OutputStream) {
        val payload = ExportPayload(
            exportedAtEpochMillis = System.currentTimeMillis(),
            holdings = holdingRepository.getAllOnce().map { it.toExportDto() },
        )
        outputStream.use { it.write(json.encodeToString(payload).toByteArray(Charsets.UTF_8)) }
    }

    /** Returns the number of holdings imported. Existing holdings are kept; imported ones are added. */
    suspend fun importFromStream(inputStream: InputStream): Int {
        val text = inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
        val payload = json.decodeFromString<ExportPayload>(text)
        payload.holdings.forEach { dto -> holdingRepository.upsert(dto.toDomain()) }
        return payload.holdings.size
    }
}
