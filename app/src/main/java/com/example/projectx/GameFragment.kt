package com.example.projectx

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectx.Models.GameModel
import com.example.projectx.Models.GamesApiResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class GameFragment() : Fragment()  ,onClickListener {
    lateinit var rvGames : RecyclerView
    lateinit var gameAdapter: GameAdapter
    //lateinit var layoutManager : LinearLayoutManager
    lateinit var gridLayoutManager: GridLayoutManager  //!!!!!!
    lateinit var searchView: SearchView


    //var page = 1
    private val BASE_URL = "https://api.rawg.io/api/"
    var totalItemCount: Int = 0
    var loading = true
    var searchQuery : String? = ""
    lateinit var call : Call<GamesApiResponse>
    var searching : Boolean = false
    var lastVisible = 0

    //private var page: Int = 1
    //private var currentSearchQuery : String? = null

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        //önce bu şekildeydi direkt string olarak almak istediğimiz için
        //.addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl(BASE_URL)
        .build()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_game, container , false)
        // Adding the games to the list:

        searchView = view.findViewById(R.id.idSV)
        rvGames = view.findViewById(R.id.rvGames)
        //Done inside onCreateView because state should be restored when we switch between favorites and games tabs
        if(resources.configuration.orientation.equals(Configuration.ORIENTATION_LANDSCAPE))
            gridLayoutManager = GridLayoutManager(view.context, 2)
        else
            gridLayoutManager = GridLayoutManager(view.context, 1)

        //layoutManager = LinearLayoutManager(view.context)
        gameAdapter = GameAdapter(this)
        rvGames.setHasFixedSize(true)
        rvGames.layoutManager = gridLayoutManager

        rvGames.adapter = gameAdapter

        val searchPlateId = searchView.context.resources.getIdentifier("android:id/search_plate", null, null)
        val searchPlate = searchView.findViewById<View>(searchPlateId)
        searchPlate?.setBackgroundColor(Color.TRANSPARENT)
        callApi(searchQuery)
        // page++


       searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{

            override fun onQueryTextSubmit(query: String?): Boolean {
                    callApi(searchQuery)
                    return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
            //    pae = 1
                call.cancel()
                gameAdapter.games.clear()
                totalItemCount = 0
                lastVisible = 0

               if(newText!!.length > 3)
               {

                   Log.e("MESS", "CHANGED")
                   searchQuery = newText
                   callApi(searchQuery)
               }

                else{

                   Log.e("MESS", "CHANGED")
                   searchQuery = null
                   callApi(searchQuery)
                }

                return false
            }
        })

        rvGames.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                // CHANGE TO layoutManager!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                gridLayoutManager = GridLayoutManager::class.java.cast(recyclerView.layoutManager) as GridLayoutManager
                lastVisible = gridLayoutManager.findLastCompletelyVisibleItemPosition()
                totalItemCount = gridLayoutManager.itemCount
                var firstVisible = gridLayoutManager.findFirstCompletelyVisibleItemPosition()
               // Log.e("MESS", lastVisible.toString())
               Log.e("LAST VISIBLE IN LAST", lastVisible.toString())
              //  Log.e("FIRST VISIBLE IN LAST", firstVisible.toString())
               // Log.e("TOTAL ITEM COUNT", totalItemCount.toString())
                if (lastVisible == totalItemCount - 1 && loading) {
                    callApi(searchQuery)

                    loading = false
                }
            }

        })

        return view
    }

    // TO SAVE THE STATE WHEN ROTATED*****************************************************************
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        (activity as MainActivity).recreate()
    }
    // TO SAVE THE STATE WHEN ROTATED ENDS*****************************************************************


    fun callApi(query : String?){

        val apiService = retrofit.create(GameApiService::class.java)

        var page : Int = 1
        if(totalItemCount % 10 == 0 && lastVisible != 0){
            page = totalItemCount/10 + 1
        }
        Log.e("LAST VISIBLE", lastVisible.toString())
        Log.e("PAGE", page.toString())
        call = apiService.getGames(key = "f11caa5ea61840b99a7be5ae1f243d15", page, page_size = 10,query,null,null)

        // call objesini async bir şekilde çağırmak için enqueue kullanırız
        call.enqueue(object: Callback<GamesApiResponse> {
            override fun onResponse(call: Call<GamesApiResponse>, response: Response<GamesApiResponse>) {
                // başarılı bir şekilde cevap aldıysak

                if(response.code() == 200){
                    // httpde body'de veri döner
                    val userList = response.body()!!.results
                    gameAdapter.addGame(userList)
                 //   totalItemCount += 9
                    loading = true
                }
                else{
                    Log.e("API","REQUEST FAIL")
                }

            }

            override fun onFailure(call: Call<GamesApiResponse>, t: Throwable) {

                Log.e("API","NO GAME FOUND")
            }

        })

    }

    // ON CLİCK METHOT OGUZ
    override fun onGameClickListener(position: Int, v: View?) {
        val actv = v?.context as AppCompatActivity // viewin activitisini döndür
        //v.setBackgroundColor(0xE0E0E0)

        val intent = Intent(actv,DetailActivity::class.java) // intent ile o positiona göre activitye data gönder

        val apiService = retrofit.create(GameApiService::class.java)
        val call : Call<GameModel> = apiService.getGameById(gameAdapter.games[position].id,"f11caa5ea61840b99a7be5ae1f243d15")
        call.enqueue(object: Callback<GameModel> {
            override fun onResponse(call: Call<GameModel>, response: Response<GameModel>) {
                // başarılı bir şekilde cevap aldıysak

                if(response.code() == 200){
                    val game = response.body()
                    intent.putExtra("game",game as GameModel)
                    startActivity(intent)
                }
                else{
                    Log.e("API","REQUEST FAIL")
                }
            }

            override fun onFailure(call: Call<GameModel>, t: Throwable) {
                Log.e("API","NO GAME FOUND")
            }

        })
       // Log.e("IDDD" ,gameAdapter.games[position].id.toString())
       // intent.putExtra("name",gameAdapter.games[position].name)

    } // on click end
}