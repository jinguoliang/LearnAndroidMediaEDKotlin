package com.example.jinux.videoencodedecodekotlin

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.intentFor

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val data = getData()
        recycleView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycleView.adapter = MAdapter(this, data)
    }

    private fun getData(): List<Item> {
        val items = ArrayList<Item>()

        items.addItem("编码 GIF") {
            startActivity(intentFor<EncdoeGIFActivity>())
        }

        items.addItem("解码 GIF") {
            startActivity(intentFor<DecoderGIFActivity>())
        }

        items.addItem("录制声音直接播放") {
            startActivity(intentFor<RecordPlayActivity>())
        }

        items.addItem("解码 Mp3/Wave") {
            startActivity(intentFor<DecodeAudioActivity>())
        }

        return items
    }

    fun ArrayList<Item>.addItem(text: String, call: (() -> Unit)) {
        add(Item(text, call))
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }


    data class Item(val text: String, val call: (() -> Unit))

}

class MAdapter(val context: Context, val data: List<MainActivity.Item>) : RecyclerView.Adapter<MAdapter.MHolder>() {
    override fun getItemCount(): Int {
        return data.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MAdapter.MHolder {
        return MHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_entry, parent, false))
    }

    override fun onBindViewHolder(holder: MAdapter.MHolder, position: Int) {
        val item = data[position]
        holder.titleBtn.text = item.text
        holder.titleBtn.setOnClickListener {
            item.call()
        }
    }


    class MHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleBtn = itemView.findViewById<Button>(R.id.title)
    }

}
