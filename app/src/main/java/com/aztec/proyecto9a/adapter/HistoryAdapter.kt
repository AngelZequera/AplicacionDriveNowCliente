package com.aztec.proyecto9a.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aztec.proyecto9a.R
import com.aztec.proyecto9a.activities.HistoriesDetailActivity
import com.aztec.proyecto9a.models.History
import com.aztec.proyecto9a.utils.RelativeTime

class HistoryAdapter(val context: Activity, val histories: ArrayList<History>): RecyclerView.Adapter<HistoryAdapter.HistoryAdapterViewHolder>() {

    // Instancia la vista
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryAdapterViewHolder {
       val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_history, parent, false)
        return HistoryAdapterViewHolder(view)
    }

    // El tamaño de la lista a mostrar
    override fun getItemCount(): Int {
        return histories.size
    }

    //Establecer la información de cada TextView
    override fun onBindViewHolder(holder: HistoryAdapterViewHolder, position: Int) {
        val history = histories[position] // un solo historial
        holder.txtOrigin.text = history.origin
        holder.txtDestination.text = history.destination
        if(history.timestamp != null){
            holder.txtDate.text = RelativeTime.getTimeAgo(history.timestamp!!, context)
        }
        holder.itemView.setOnClickListener { goToDetail(history?.id!!) }
    }

    private fun goToDetail(idHistory: String){
        val i = Intent(context, HistoriesDetailActivity::class.java)
        i.putExtra("id", idHistory)
        context.startActivity(i)
    }


    class HistoryAdapterViewHolder(view: View): RecyclerView.ViewHolder(view){
        val txtOrigin: TextView
        val txtDestination: TextView
        val txtDate: TextView

        init {
            txtOrigin = view.findViewById(R.id.txtOrigin)
            txtDestination = view.findViewById(R.id.txtDestination)
            txtDate = view.findViewById(R.id.txtDate)
        }
    }


}