package com.example.payoffline

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener

class ReceiveActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receive)
    }
    override fun onStart() {
        super.onStart()
        startAdvertising()
    }
    override fun onStop() {
        super.onStop()
        Nearby.getConnectionsClient(applicationContext).stopAdvertising()
    }

    private fun startAdvertising() {
        val SERVICE_ID = "com.example.payoffline"
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build()
        Nearby.getConnectionsClient(applicationContext)
            .startAdvertising(
                "Kishan123", SERVICE_ID, connectionLifecycleCallback, advertisingOptions   //change first param to name of Teacher
            )
            .addOnSuccessListener(
                OnSuccessListener { unused: Void? ->

                    Toast.makeText(applicationContext,"Ready to Take Attendance",Toast.LENGTH_SHORT).show()
                    Log.d("Receiver","Advertising success")

                })
            .addOnFailureListener(
                OnFailureListener { e: Exception? ->
                    Log.d("Receiver","Advertising fail: ${e.toString()}")
                    Toast.makeText(applicationContext,"Error Initialising: ${e.toString()}",Toast.LENGTH_SHORT).show()
                })
    }

    private val payloadCallback: PayloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {

            val data_recieved = payload.asBytes()?.let { String(it, Charsets.UTF_8) }
            Log.d("Receiver","Data recieved: ${payload.asBytes()?.let { String(it, Charsets.UTF_8) }}")
            val sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE)
            var b = sharedPreferences.getString("balance","")
            if (b != null) {
                if (data_recieved != null) {
                    b = (b.toInt() + data_recieved.toInt()).toString()
                }
            }
            val editor: SharedPreferences.Editor= sharedPreferences.edit()
            editor.putString("balance", b)
            editor.commit()

            Toast.makeText(applicationContext,"Data recieved: ${payload.asBytes()?.let { String(it, Charsets.UTF_8) }}",Toast.LENGTH_SHORT).show()

            val data = Payload.fromBytes("Success".toByteArray())
            Nearby.getConnectionsClient(applicationContext).sendPayload(endpointId, data)
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {

        }
    }
    private val connectionLifecycleCallback: ConnectionLifecycleCallback =
        object : ConnectionLifecycleCallback() {
            override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
                // Automatically accept the connection on both sides.
                // Toast.makeText(applicationContext,"Name of Students: ${connectionInfo.endpointName}",Toast.LENGTH_SHORT).show()
                Nearby.getConnectionsClient(applicationContext).acceptConnection(endpointId, payloadCallback)
            }

            override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
                when (result.status.statusCode) {
                    ConnectionsStatusCodes.STATUS_OK -> {
                        // Nearby.getConnectionsClient(applicationContext).stopAdvertising()

                        Toast.makeText(applicationContext,"EndID of Sender: $endpointId",Toast.LENGTH_SHORT).show()
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

}