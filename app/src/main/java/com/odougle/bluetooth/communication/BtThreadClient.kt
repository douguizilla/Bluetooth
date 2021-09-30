package com.odougle.bluetooth.communication

import android.bluetooth.BluetoothDevice
import android.os.Handler
import com.odougle.bluetooth.util.Constants

class BtThreadClient(
    private val device: BluetoothDevice,
    uiHandler: Handler
) : BtThread(uiHandler){

    override fun run() {
        try {
            socket = device.createRfcommSocketToServiceRecord(Constants.SERVICE_UUID)
            socket?.connect()
            threadCommunication.handleConnection(socket!!)
        }catch (e: Exception){
            e.printStackTrace()
            uiHandler.obtainMessage(BtThreadCommunication.MSG_DISCONNECTED,
            "${e.message}[2]")?.sendToTarget()
        }
    }
}