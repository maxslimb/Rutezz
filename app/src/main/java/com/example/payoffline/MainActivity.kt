package com.example.payoffline

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val bt_send = findViewById<Button>(R.id.button_send)
        val bt_receive = findViewById<Button>(R.id.button_receive)
        val balance = findViewById<TextView>(R.id.balance)
        val sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor= sharedPreferences.edit()
        if(!sharedPreferences.getBoolean("updateb_once",false)) {
            editor.putBoolean("updateb_once", true)
            editor.putString("balance", "2000")
            editor.commit()
        }
        balance.text = sharedPreferences.getString("balance","0")
        bt_receive.setOnClickListener {
            val intent = Intent(this, ReceiveActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        bt_send.setOnClickListener {
            val intent = Intent(this, SendActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

    }
}