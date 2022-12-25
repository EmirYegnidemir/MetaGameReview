package com.example.projectx

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectx.Models.GameModel
import com.google.android.material.snackbar.Snackbar

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class FavoritesFragment : Fragment(), onClickListener {
    lateinit var rvFav : RecyclerView
    lateinit var gameAdapter: GameAdapter
    lateinit var favoritedList: ArrayList<GameModel>
    lateinit var tempfavoritedList: ArrayList<GameModel>
    lateinit var favouritesText: TextView
    lateinit var sp: SharedPreferences
    lateinit var spct:SharedPreferences
    lateinit var editor:Editor
    lateinit var cteditor:Editor
    lateinit var gson: Gson
    private var ct:Int = 0
    private var dataSetChanged:Boolean = false
    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_favorites, container , false)
        rvFav = view.findViewById(R.id.rvFav)
        favouritesText = view.findViewById(R.id.favouritesText)
        sp = this.requireActivity().getSharedPreferences("sharedpref", Context.MODE_PRIVATE)
        spct = this.requireActivity().getSharedPreferences("sharedprefct", Context.MODE_PRIVATE)
        editor = sp.edit()
        cteditor = spct.edit()
        ct =spct.getInt("ct",0)
        favoritedList = ArrayList()
        gson = Gson()
        if (ct>0) { // fav listesine eklenecek varsa
            val json = sp.getString("favGames", null)
            val type: Type = object : TypeToken<ArrayList<GameModel>>() {}.type
            tempfavoritedList = ArrayList()
            tempfavoritedList = gson.fromJson(json,type)
            for (games in tempfavoritedList){
                favoritedList.add(games) // ekleme
            }
        }
        favouritesText.text = "Favorites(${favoritedList.count()})"
        rvFav.setHasFixedSize(true)
        rvFav.layoutManager = LinearLayoutManager(view.context)
        gameAdapter = GameAdapter(this)
        gameAdapter.addGame(favoritedList)
        rvFav.adapter = gameAdapter
        swipeToDelete()
        return view
    }



    override fun onGameClickListener(position: Int, v: View?) { // TAMAMLANMADI
        val actv = v?.context as AppCompatActivity
        val intent = Intent(actv,DetailActivity::class.java)
        intent.putExtra("game", gameAdapter.games[position])
      //  intent.putExtra("type",gameAdapter.games[position])
        startActivity(intent)
    }
    @SuppressLint("SetTextI18n")
    fun addFavourite(){
        val json = gson.toJson(favoritedList)
        editor.putString("favGames",json)
        ct +=1
        cteditor.putInt("ct",ct)
        //gameAdapter.addGame(favoritedList)
        cteditor.apply()
        editor.apply()
        favouritesText.text = "Favorites(${favoritedList.count()})"
    }
    @SuppressLint("SetTextI18n")
    fun deleteFavourite(){
        ct = ct.dec()
        cteditor.putInt("ct", ct)
        if(ct==0){
            editor.clear().commit()
        }
        if(ct!=0) {
            val json = gson.toJson(favoritedList)
            editor.putString("favGames", json)
        }
        //gameAdapter.addGame(favoritedList)
        cteditor.apply()
        editor.apply()
        favouritesText.text = "Favorites(${favoritedList.count()})"
    }
    fun swipeToDelete(){
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // this method is called
                // when the item is moved.
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // this method is called when we swipe our item to right direction.
                // on below line we are getting the item at a particular position.
                val deletedGame: GameModel =
                    favoritedList.get(viewHolder.adapterPosition)

                // below line is to get the position
                // of the item at that position.
                val position = viewHolder.adapterPosition

                // this method is called when item is swiped.
                // below line is to remove item from our array list.
                favoritedList.removeAt(viewHolder.adapterPosition)
                // below line is to notify our item is removed from adapter.
                gameAdapter.notifyItemRemoved(viewHolder.adapterPosition)
                deleteFavourite()
                dataSetChanged = true
                // below line is to display our snackbar with action.
                Snackbar.make(rvFav, "Deleted " + deletedGame.name, Snackbar.LENGTH_LONG)
                    .setAction(
                        "Undo",
                        View.OnClickListener {
                            // adding on click listener to our action of snack bar.
                            // below line is to add our item to array list with a position.
                            favoritedList.add(position, deletedGame)
                            addFavourite()
                            // below line is to notify item is
                            // added to our adapter class.
                            gameAdapter.notifyItemInserted(position)
                            dataSetChanged = true
                        }).show()
            }
            // at last we are adding this
            // to our recycler view.
        }).attachToRecyclerView(rvFav)

    }
}