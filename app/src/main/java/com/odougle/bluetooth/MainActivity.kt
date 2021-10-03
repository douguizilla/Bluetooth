package com.odougle.bluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.odougle.bluetooth.communication.BtThread
import com.odougle.bluetooth.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val btAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val remoteDevices = mutableListOf<BluetoothDevice>()
    private var btThread: BtThread? = null
    private var btEventsReceiver: BtEventsReceiver? = null
    private var messagesAdapter: ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        messagesAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        binding.lstMessages.adapter = messagesAdapter

        if(btAdapter != null){
            if(btAdapter.isEnabled){
                checkLocationPermission()
            }else{
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, BT_ACTIVATE)
            }
        }else{
            Toast.makeText(this, R.string.msg_error_bt_not_found, Toast.LENGTH_LONG).show()
            finish()
        }
        registerBluetoothEventReceiver()
        binding.btnSend.setOnClickListener{
            sendButtonClick()
        }
    }

    private fun checkLocationPermission(){
        if(ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), RC_LOCATION_PERMISSION)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == BT_ACTIVATE){
            if(Activity.RESULT_OK == resultCode){
                checkLocationPermission()
            }else{
                Toast.makeText(this, R.string.msg_activate_bluetooth, Toast.LENGTH_SHORT).show()
                finish()
            }
        }else if(requestCode == BT_VISIBLE){
            if(resultCode == BT_DISCOVERY_TIME){
                startServerThread()
            }else{
                hideProgress()
                Toast.makeText(this, R.string.msg_device_invisible, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private inner class BtEventsReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (true) {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)!!
                remoteDevices.add(device)
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == intent.action) {
                showDiscoveredDevices(remoteDevices)
            }

        }
    }

    private fun registerBluetoothEventReceiver(){
        btEventsReceiver = BtEventsReceiver()
        val filter1 = IntentFilter(BluetoothDevice.ACTION_FOUND)
        val filter2 = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(btEventsReceiver, filter1)
        registerReceiver(btEventsReceiver, filter2)
    }

    override fun onDestroy() {
        unregisterBluetoothEventReceiver()
        stopAll()
        super.onDestroy()
    }

    private fun unregisterBluetoothEventReceiver() {
        unregisterReceiver(btEventsReceiver)
    }

    private fun stopAll(){
        btThread?.stopThread()
        btThread = null
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_bluetooth_chat, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_client -> startClient()
            R.id.action_server -> startServer()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startClient() {
        val discoverableIntent= Intent(
            BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE
        )
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, BT_DISCOVERY_TIME)
        startActivityForResult(discoverableIntent, BT_VISIBLE)
    }

    companion object{
        private const val BT_ACTIVATE = 0
        private const val BT_VISIBLE = 1
        private const val BT_DISCOVERY_TIME = 120
        private const val RC_LOCATION_PERMISSION = 2
    }
}