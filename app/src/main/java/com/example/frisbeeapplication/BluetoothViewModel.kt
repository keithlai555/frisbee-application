import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class BluetoothViewModel : ViewModel() {

    private var bluetoothSocket: BluetoothSocket? = null
    private var bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    private val _connectionStatus = MutableLiveData<Boolean>()
    val connectionStatus: LiveData<Boolean> get() = _connectionStatus

    private val _connectionMessage = MutableLiveData<String>()
    val connectionMessage: LiveData<String> get() = _connectionMessage

    private var _userHeight: String? = null
    val userHeight: String?
        get() = _userHeight

    fun setUserHeight(height: String) {
        _userHeight = height
    }

    @SuppressLint("MissingPermission")
    fun connectToManuallyPairedDevice(deviceName: String) {
        val device = getPairedDevice(deviceName)
        if (device != null) {
            try {
                bluetoothAdapter?.cancelDiscovery()

                // Use RFCOMM UUID for Bluetooth Classic
                val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard Serial Port Profile UUID
                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)

                bluetoothSocket?.connect()

                inputStream = bluetoothSocket?.inputStream
                outputStream = bluetoothSocket?.outputStream

                _connectionStatus.postValue(true)
                _connectionMessage.postValue("Connected to $deviceName")

            } catch (e: IOException) {
                _connectionStatus.postValue(false)
                _connectionMessage.postValue("Connection failed: ${e.message}")
            }
        } else {
            _connectionMessage.postValue("Device not found")
        }
    }

    @SuppressLint("MissingPermission")
    private fun getPairedDevice(deviceName: String): BluetoothDevice? {
        val pairedDevices = bluetoothAdapter?.bondedDevices
        return pairedDevices?.find { it.name == deviceName }
    }

    @SuppressLint("MissingPermission")
    fun sendDataToBluetooth(data: String) {
        if (bluetoothSocket?.isConnected == true) {
            try {
                outputStream?.write(data.toByteArray())
                _connectionMessage.postValue("Sent: $data")
            } catch (e: IOException) {
                _connectionMessage.postValue("Error sending data: ${e.message}")
                _connectionStatus.postValue(false)
            }
        } else {
            _connectionMessage.postValue("Not connected to a device")
            _connectionStatus.postValue(false)
        }
    }

    fun disconnect() {
        try {
            inputStream?.close()
            outputStream?.close()
            bluetoothSocket?.close()
            _connectionStatus.postValue(false)
            _connectionMessage.postValue("Disconnected")
        } catch (e: IOException) {
            _connectionMessage.postValue("Error disconnecting: ${e.message}")
        }
    }
}
