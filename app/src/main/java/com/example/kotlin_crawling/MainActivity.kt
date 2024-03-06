package com.example.kotlin_crawling

import android.os.Bundle
import android.os.AsyncTask
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlin_crawling.BusAPI.BusApiService
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var routeId: String
    private lateinit var nodeId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 전역으로 관리될 변수들(api를 통해 추출되어 저장됨)
        var routeId = "" // api 호출하여 얻은 routeId 저장용 변수
        var nodeId = "" // api 호출하여 얻은 nodeId 저장용 변수

        // Retrofit 객체 생성
        val retrofit = Retrofit.Builder()
            .baseUrl("http://apis.data.go.kr/1613000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Retrofit 인터페이스 구현체 생성
        val service = retrofit.create(BusApiService::class.java)

        // 네트워크 요청을 백그라운드 스레드에서 실행
        // CoroutineScope를 확장하여 코루틴을 사용할 수 있도록 함
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. 노선 정보 조회(노선ID: routeId 얻기 위함)
                var url1 = "http://apis.data.go.kr/1613000/BusRouteInfoInqireService/getRouteNoList?numOfRows=10&pageNo=1&_type=xml&serviceKey=(인증키)&cityCode=(도시코드)&routeNo=(노선번호)"
                var daegu = 22 // 대구 도시 코드
                var routeNo = 937
                var routeId937 = "DGB3000937000" // 937 노선 routeId

                val response1 = service.requestType1(
                    numOfRows = 10,
                    pageNo = 1,
                    type = "json",
                    serviceKey = BuildConfig.KEY1,
                    cityCode = 22,
                    routeNo = "937"
                ).execute()

                val responseBody1 = response1.body()
                if (responseBody1 != null) {
                    val jsonResponseBodyString = responseBody1.string()
                    val jsonResponse = Gson().fromJson(jsonResponseBodyString, Map::class.java) as Map<String, Any>

                    // "routeid"를 추출
                    val response = jsonResponse["response"] as Map<String, Any>
                    val body = response["body"] as Map<String, Any>
                    val items = body["items"] as Map<String, Any>
                    val item = items["item"] as Map<String, Any>
                    routeId = item["routeid"] as String // 전역 변수에 저장

                    Log.d("dev", "routeId: $routeId")

                } else {
                    Log.e("MainActivity", "Response Body is null")
                }

                // 2. 노선별 경유정류소 목록 조회(정류소ID: nodeId을 얻기 위함, 정류소명: nodenm 으로 일치하는 정류소 선택)
                var url2 = "http://apis.data.go.kr/1613000/BusRouteInfoInqireService/getRouteAcctoThrghSttnList?serviceKey=(인증키)&cityCode=(도시코드)&routeId=(라우트ID)&numOfRows=10&pageNo=1&_type=xml"
                var nodeNo = "00317" // 경북대 경상대학 건너
                var sampleNodeId = "DGB7011010200" // 결과값 예시, "경북대 경상대학 건너" 정류장의 nodeId

                val response2 = service.requestType2(
                    serviceKey = BuildConfig.KEY2,
                    cityCode = 22,
                    routeId = routeId,
                    numOfRows = 10,
                    pageNo = 4,
                    type = "json"
                ).execute()

                val responseBody2 = response2.body()
                if (responseBody2 != null) {
                    val stationName = "경북대학교정문건너"
                    val jsonResponseBodyString = responseBody2.string()
//                    Log.d("dev", "[response2] jsonResponseBodyString: $jsonResponseBodyString")
                    val jsonResponse = Gson().fromJson(jsonResponseBodyString, Map::class.java) as Map<String, Any>
                    val items = (jsonResponse["response"] as Map<String, Any>)["body"] as Map<String, Any>
                    val nodeList = (items["items"] as Map<String, Any>)["item"] as List<Map<String, Any>>

                    val nodeIdsContainingStationName = nodeList
                        .filter { it["nodenm"].toString().lowercase(Locale.getDefault()).contains(
                            stationName.lowercase(Locale.ROOT)
                        ) }
                        .map { it["nodeid"] }

                    nodeId = nodeIdsContainingStationName[0].toString()
                    Log.d("dev", "nodeId: $nodeId")

                } else {
                    Log.e("MainActivity", "Response Body is null")
                }

                // 3. 버스 도착 정보 목록 조회(해당 정류소에 도착하는 버스들 정보 얻기 위함, 1에서 구한 routeId이용)
                var url3 = "http://apis.data.go.kr/1613000/ArvlInfoInqireService/getSttnAcctoArvlPrearngeInfoList?serviceKey=(인증키)&cityCode=(도시코드)&nodeId=(정류소ID)&numOfRows=10&pageNo=1&_type=xml"

                val response3 = service.requestType3(
                    serviceKey = BuildConfig.KEY3,
                    cityCode = 22,
                    nodeId = nodeId,
                    numOfRows = 10,
                    pageNo = 1,
                    type = "json"
                ).execute()

                val responseBody3 = response3.body()
                if (responseBody3 != null) {
                    val jsonResponseBodyString = responseBody3.string()
                    Log.d("dev", "[response3] jsonResponseBodyString: $jsonResponseBodyString")
                    val jsonResponse = Gson().fromJson(jsonResponseBodyString, Map::class.java) as Map<String, Any>
                    val items = ((jsonResponse["response"] as Map<String, Any>)["body"] as Map<String, Any>)["items"] as Map<String, Any>
                    val item = (items["item"] as List<Map<String, Any>>)[0]

                    val arrTime = (item["arrtime"] as Double).toInt()
                    Log.d("dev", "arrTime: $arrTime")
                    val minutes = arrTime / 60
                    val seconds = arrTime % 60
                    Log.d("dev", "arrTime: $minutes 분 $seconds 초")

                    // UI 업데이트를 위해 메인 스레드에서 실행
                    withContext(Dispatchers.Main) {
                        // UI 업데이트 코드 작성
                    }

                } else {
                    Log.e("MainActivity", "Response Body is null")
                }
            } catch (e: IOException) {
                Log.e("MainActivity", "IOException: ${e.message}")
            }
        }
    }
}
