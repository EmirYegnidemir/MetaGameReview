package com.example.projectx

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projectx.Models.GameModel
import com.example.projectx.databinding.ItemGameBinding
class GameAdapter (var onClickListener : onClickListener) : RecyclerView.Adapter<GameAdapter.GameViewHolder>()
{
    var games =  ArrayList<GameModel>()
    inner class GameViewHolder(val binding: ItemGameBinding) : RecyclerView.ViewHolder(binding.root)

    // if the user scrolled little bit and another item was recycled and new item needs to be viewed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        // it will crash if we wouldn't have set last parameter as false in rv
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemGameBinding.inflate(layoutInflater, parent, false)
        return GameViewHolder(binding)

    }
    // sets the text and other viewed stuff initially
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        holder.binding.apply {
            tvGame.text = games[position].name
            tvScore.text = games[position].metacritic.toString()
            var genres= " "

            for ((index, value) in games[position].genres.withIndex()) {
                if(index != games[position].genres.size - 1){
                    genres += value.name.toString() + ", "
                }
                else{
                    genres += value.name.toString()
                }
            }
            tvType.text = genres
            Glide.with(this.ivImage).load(games[position].background_image).into(ivImage)
        }
        holder.itemView.setOnClickListener (
            object : View.OnClickListener {
            override fun onClick(v: View?) {
                onClickListener.onGameClickListener(holder.adapterPosition , v )
            }
            })

    }

    fun addGame(newGames : List<GameModel>){
        games.addAll(newGames)
        notifyDataSetChanged()
    }

    // how many items we have currently in recyclerview,
    override fun getItemCount(): Int {
        return games.size
    }
}