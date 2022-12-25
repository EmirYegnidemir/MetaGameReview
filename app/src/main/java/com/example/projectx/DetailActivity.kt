package com.example.projectx

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.projectx.Models.GameModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ms.square.android.expandabletextview.ExpandableTextView
import java.lang.reflect.Type

@Suppress("NAME_SHADOWING")
class DetailActivity : AppCompatActivity() {
    lateinit var imageDetail: ImageView //Views
    lateinit var gameDescr : ExpandableTextView
    lateinit var gameTitle : TextView
    lateinit var redditLinkText : TextView
    lateinit var webLinkText : TextView
    lateinit var gameButton : TextView
    lateinit var favButton :TextView
    lateinit var sp: SharedPreferences
    lateinit var spct: SharedPreferences
    lateinit var list:ArrayList<GameModel>
    lateinit var editor: Editor
    lateinit var cteditor:Editor
    lateinit var gson:Gson
    private var ct: Int = 0
    private var added:Boolean = false
    private var favText:String = "Favourite"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        bindViews()
        val game : GameModel = intent.getSerializableExtra("game") as GameModel
        initViews(game.name,game.background_image,game.description,game.reddit_url,game.website)
        setSharedPrefs()// intent'den verileri alma
        //cteditor.clear().commit()
        //editor.clear().commit()
        checkAdded(game)
        favButton.setOnClickListener {
            checkAdded(game)
            setFavourited(game)
        }

        gameButton.setOnClickListener { // geri dönüş butonu
            finish()
        }
    }
    fun bindViews(){ //bind
        imageDetail = findViewById(R.id.imageDetail)
        gameDescr =findViewById(R.id.gameDesc)
        gameTitle = findViewById(R.id.gameTitle)
        redditLinkText = findViewById(R.id.redditLink)
        webLinkText = findViewById(R.id.webLink)
        gameButton =findViewById(R.id.gameButton)
        favButton = findViewById(R.id.favButton)
    }
    fun initViews(name:String?,imageUrl:String?,gameDesc: String?,redditLink: String?,webLink:String?){
        gameTitle.text =name // oyun adı
        Glide.with(imageDetail).load(imageUrl).into(imageDetail) // oyun resmi url'den
        val regex = "</?.*?>".toRegex()  // to replace tags came from api in description string
        val replacedDesc = gameDesc!!.replace(regex, "")
        gameDescr.text = replacedDesc // oyun açıklaması
        redditLinkText.setOnClickListener{
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(redditLink))
            startActivity(intent)
        }
        webLinkText.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webLink))
            startActivity(intent)
        }
    }
    fun setSharedPrefs(){
        sp = getSharedPreferences("sharedpref", Context.MODE_PRIVATE) //sp oluşturulması
        spct =getSharedPreferences("sharedprefct",Context.MODE_PRIVATE)
        editor = sp.edit() // editor oluşturulması
        cteditor = spct.edit()
        ct = spct.getInt("ct",0) // counterı al
        gson = Gson()
        list = ArrayList()
    }
    fun checkAdded(game:GameModel): Boolean {
        if(ct!=0) { // eğer favori listesi boş değilse o anki listeyi al
            val currjson = sp.getString("favGames", null)
            val type: Type = object : TypeToken<ArrayList<GameModel>>() {}.type
            list = gson.fromJson(currjson, type)
            for(e in list.indices){
                if(game.id == list[e].id){
                    added = true
                    favText = "Favourited"
                    favButton.text = favText
                }
            }
        }
        return added
    }
    fun setFavourited(game:GameModel){
        if(!added){ // favoride yoksa ekle
            list.add(game)
            val json = gson.toJson(list)
            editor.putString("favGames",json)
            ct+=1 // sayaçı arttır
            cteditor.putInt("ct",ct)
            Toast.makeText(this, "Oyun eklendi ", Toast.LENGTH_SHORT).show()
            favText = "Favourited"
            favButton.text = favText
        }
        cteditor.apply()
        editor.apply()
    }
}