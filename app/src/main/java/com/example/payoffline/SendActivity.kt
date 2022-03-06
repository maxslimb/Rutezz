package com.example.payoffline

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener

class SendActivity : AppCompatActivity() {
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send)
        sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE)
    }
    override fun onStart() {
        super.onStart()
        startDiscovery()
    }

    private fun startDiscovery() {
        val SERVICE_ID = "com.example.payoffline"
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build()
        Nearby.getConnectionsClient(applicationContext)
            .startDiscovery(SERVICE_ID, endpointDiscoveryCallback, discoveryOptions)
            .addOnSuccessListener { unused: Void? ->
                Toast.makeText(applicationContext,"Connecting to peer",Toast.LENGTH_SHORT).show()
                Log.d("Sender","Discovery success")
            }
            .addOnFailureListener { e: Exception? ->
                Toast.makeText(applicationContext,"Error Connecting to teacher: ${e.toString()}",Toast.LENGTH_SHORT).show()
            }
    }

    private val endpointDiscoveryCallback: EndpointDiscoveryCallback =
        object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                // An endpoint was found. We request a connection to it.
                Nearby.getConnectionsClient(applicationContext)
                    .requestConnection("Kishan123", endpointId, connectionLifecycleCallback) //change to faulty name- first param
                    .addOnSuccessListener(
                        OnSuccessListener { unused: Void? -> })
                    .addOnFailureListener(
                        OnFailureListener { e: java.lang.Exception? -> })
            }

            override fun onEndpointLost(endpointId: String) {
                // A previously discovered endpoint has gone away.
            }
        }
    private val payloadCallback: PayloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            val data_recieved = payload.asBytes()?.let { String(it, Charsets.UTF_8) }
            Toast.makeText(applicationContext,"Data received $data_recieved",Toast.LENGTH_SHORT).show()
            if(data_recieved=="Success"){
                val sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE)
                var b = sharedPreferences.getString("balance","")
                if (b != null) {
                    b = (b.toInt() - 200).toString()
                }
                val editor: SharedPreferences.Editor= sharedPreferences.edit()
                editor.putString("balance", b)
                editor.commit()
                Nearby.getConnectionsClient(applicationContext).stopDiscovery()
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {

        }
    }
    private val connectionLifecycleCallback: ConnectionLifecycleCallback =
        object : ConnectionLifecycleCallback() {
            override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
                // Automatically accept the connection on both sides.
                Log.d("Student", "onConnectionInitiated: accepting connection")
                Nearby.getConnectionsClient(applicationContext).acceptConnection(endpointId,payloadCallback)
            }

            override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
                when (result.status.statusCode) {
                    ConnectionsStatusCodes.STATUS_OK -> {
                        // Once you have successfully connected to your friends' devices, you can leave
                        // discovery mode so you can stop discovering other devices

                        // if you were advertising, you can stop as well
                        Toast.makeText(applicationContext,"EndID of Receiver: $endpointId", Toast.LENGTH_SHORT).show()
                        val data = Payload.fromBytes("200".toByteArray())
                        Nearby.getConnectionsClient(applicationContext).sendPayload(endpointId, data)
                        Nearby.getConnectionsClient(applicationContext).stopDiscovery()
                        //  friendEndpointId = endpointId

                    }
                    ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    }
                    ConnectionsStatusCodes.STATUS_ERROR -> {
                    }
                    else -> {
                    }
                }
            }

            override fun onDisconnected(endpointId: String) {
                // We've been disconnected from this endpoint. No more data can be
                // sent or received.
            }
        }
    override fun onStop() {
        super.onStop()
        Nearby.getConnectionsClient(applicationContext).stopDiscovery()
        Log.d("Sender","Discovery stopped")
    }

}
