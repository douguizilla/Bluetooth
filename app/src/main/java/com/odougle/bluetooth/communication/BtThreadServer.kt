package com.odougle.bluetooth.communication

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.os.Handler
import com.odougle.bluetooth.util.Constants
import java.io.IOException

class BtThreadServer(
    private val btAdapter: BluetoothAdapter?,
    uiHandler: Handler
) : BtThread(uiHandler) {
    var serverSocket : BluetoothServerSocket? = null

    override fun run() {
        try {
            serverSocket = btAdapter?.listenUsingRfcommWithServiceRecord(
                Constants.SERVICE_NAME, Constants.SERVICE_UUID
            )
            socket = serverSocket?.accept()
            threadCommunication.handleConnection(socket!!)
        }catch (e:Exception){
            uiHandler.obtainMessage(
                BtThreadCommunication.MSG_DISCONNECTED,
                "${e.message} #1"
            ).sendToTarget()
            e.printStackTrace()
        }
    }

    override fun stopThread() {
        super.stopThread()
        try {
            serverSocket?.close()
        }catch (e: IOException){
            e.printStackTrace()
        }
    }
}