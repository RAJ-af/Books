package com.example.data.remote

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

data class ArchiveDoc(
    val identifier: String,
    val title: String? = null,
    val creator: String? = null,
    val year: String? = null,
    val description: String? = null
) {
    val coverThumbnailUrl: String
        get() = "https://archive.org/services/img/$identifier"
}

data class ArchiveSearchResponse(
    val response: ArchiveSearchBody? = null
)

data class ArchiveSearchBody(
    val numFound: Int = 0,
    val start: Int = 0,
    val docs: List<ArchiveDoc> = emptyList()
)

data class ArchiveFile(
    val name: String = "",
    val format: String? = null,
    val size: String? = null,
    val source: String? = null
)

data class ArchiveMetadataResponse(
    val server: String? = null,
    val dir: String? = null,
    val files: List<ArchiveFile> = emptyList(),
    val metadata: ArchiveMetadataInfo? = null
)

data class ArchiveMetadataInfo(
    val identifier: String? = null,
    val title: String? = null,
    val creator: String? = null,
    val year: String? = null,
    val description: String? = null
)

/**
 * Custom Moshi Adapter to parse ArchiveDoc safely whether fields are strings, arrays, or numbers.
 */
class ArchiveDocJsonAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): ArchiveDoc {
        var identifier = ""
        var title: String? = null
        var creator: String? = null
        var year: String? = null
        var description: String? = null

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "identifier" -> identifier = readStringOrArray(reader) ?: ""
                "title" -> title = readStringOrArray(reader)
                "creator" -> creator = readStringOrArray(reader)
                "year" -> year = readStringOrNumberOrArray(reader)
                "description" -> description = readStringOrArray(reader)
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        return ArchiveDoc(
            identifier = identifier,
            title = title,
            creator = creator,
            year = year,
            description = description
        )
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: ArchiveDoc?) {
        if (value == null) {
            writer.nullValue()
            return
        }
        writer.beginObject()
        writer.name("identifier").value(value.identifier)
        writer.name("title").value(value.title)
        writer.name("creator").value(value.creator)
        writer.name("year").value(value.year)
        writer.name("description").value(value.description)
        writer.endObject()
    }

    private fun readStringOrArray(reader: JsonReader): String? {
        return when (reader.peek()) {
            JsonReader.Token.STRING -> reader.nextString()
            JsonReader.Token.BEGIN_ARRAY -> {
                val list = mutableListOf<String>()
                reader.beginArray()
                while (reader.hasNext()) {
                    if (reader.peek() == JsonReader.Token.STRING) {
                        list.add(reader.nextString())
                    } else {
                        reader.skipValue()
                    }
                }
                reader.endArray()
                if (list.isEmpty()) null else list.joinToString(", ")
            }
            JsonReader.Token.NULL -> reader.nextNull()
            else -> {
                reader.skipValue()
                null
            }
        }
    }

    private fun readStringOrNumberOrArray(reader: JsonReader): String? {
        return when (reader.peek()) {
            JsonReader.Token.STRING -> reader.nextString()
            JsonReader.Token.NUMBER -> reader.nextString()
            JsonReader.Token.BEGIN_ARRAY -> {
                val list = mutableListOf<String>()
                reader.beginArray()
                while (reader.hasNext()) {
                    if (reader.peek() == JsonReader.Token.STRING || reader.peek() == JsonReader.Token.NUMBER) {
                        list.add(reader.nextString())
                    } else {
                        reader.skipValue()
                    }
                }
                reader.endArray()
                if (list.isEmpty()) null else list.firstOrNull()
            }
            JsonReader.Token.NULL -> reader.nextNull()
            else -> {
                reader.skipValue()
                null
            }
        }
    }
}

class ArchiveMetadataInfoJsonAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): ArchiveMetadataInfo {
        var identifier: String? = null
        var title: String? = null
        var creator: String? = null
        var year: String? = null
        var description: String? = null

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "identifier" -> identifier = readStringOrArray(reader)
                "title" -> title = readStringOrArray(reader)
                "creator" -> creator = readStringOrArray(reader)
                "year" -> year = readStringOrArray(reader)
                "description" -> description = readStringOrArray(reader)
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        return ArchiveMetadataInfo(
            identifier = identifier,
            title = title,
            creator = creator,
            year = year,
            description = description
        )
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: ArchiveMetadataInfo?) {
        if (value == null) {
            writer.nullValue()
            return
        }
        writer.beginObject()
        writer.name("identifier").value(value.identifier)
        writer.name("title").value(value.title)
        writer.name("creator").value(value.creator)
        writer.name("year").value(value.year)
        writer.name("description").value(value.description)
        writer.endObject()
    }

    private fun readStringOrArray(reader: JsonReader): String? {
        return when (reader.peek()) {
            JsonReader.Token.STRING -> reader.nextString()
            JsonReader.Token.NUMBER -> reader.nextString()
            JsonReader.Token.BEGIN_ARRAY -> {
                val list = mutableListOf<String>()
                reader.beginArray()
                while (reader.hasNext()) {
                    if (reader.peek() == JsonReader.Token.STRING || reader.peek() == JsonReader.Token.NUMBER) {
                        list.add(reader.nextString())
                    } else {
                        reader.skipValue()
                    }
                }
                reader.endArray()
                if (list.isEmpty()) null else list.joinToString(", ")
            }
            JsonReader.Token.NULL -> reader.nextNull()
            else -> {
                reader.skipValue()
                null
            }
        }
    }
}
