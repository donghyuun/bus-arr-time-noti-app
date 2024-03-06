package com.example.kotlin_crawling.BusAPI

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface BusApiService {
    @GET("BusRouteInfoInqireService/getRouteNoList")
    fun requestType1(
        @Query("numOfRows") numOfRows: Int,
        @Query("pageNo") pageNo: Int,
        @Query("_type") type: String,
        @Query("serviceKey") serviceKey: String,
        @Query("cityCode") cityCode: Int,
        @Query("routeNo") routeNo: String
    ): Call<ResponseBody>

    @GET("BusRouteInfoInqireService/getRouteAcctoThrghSttnList")
    fun requestType2(
        @Query("serviceKey") serviceKey: String,
        @Query("cityCode") cityCode: Int,
        @Query("routeId") routeId: String,
        @Query("numOfRows") numOfRows: Int,
        @Query("pageNo") pageNo: Int,
        @Query("_type") type: String
    ): Call<ResponseBody>

    @GET("ArvlInfoInqireService/getSttnAcctoArvlPrearngeInfoList")
    fun requestType3(
        @Query("serviceKey") serviceKey: String,
        @Query("cityCode") cityCode: Int,
        @Query("nodeId") nodeId: String,
        @Query("numOfRows") numOfRows: Int,
        @Query("pageNo") pageNo: Int,
        @Query("_type") type: String
    ): Call<ResponseBody>
}
