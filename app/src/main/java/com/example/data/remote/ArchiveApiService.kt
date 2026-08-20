package com.example.data.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

interface ArchiveApiService {

    @GET("advancedsearch.php")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("fl[]") fields: List<String> = listOf("identifier", "title", "creator", "year", "description"),
        @Query("output") output: String = "json",
        @Query("rows") rows: Int = 30,
        @Query("page") page: Int = 1
    ): ArchiveSearchResponse

    @GET("metadata/{identifier}")
    suspend fun getMetadata(
        @Path("identifier") identifier: String
    ): ArchiveMetadataResponse

    @Streaming
    @GET
    suspend fun downloadFileStream(
        @Url fileUrl: String
    ): Response<ResponseBody>
}
