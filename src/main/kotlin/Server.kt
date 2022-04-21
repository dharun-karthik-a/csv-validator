import metaData.ConfigReaderWriter
import routeHandler.RouteHandler
import java.net.ServerSocket


class Server(
    private val port: Int = 3000,
    metaDataPath: String = "src/main/public/files/csv-config.json"
) {
    private val configReaderWriter = ConfigReaderWriter(metaDataPath)
    private val routeHandler = RouteHandler(configReaderWriter)
    private val serverSocket = ServerSocket(port)

    fun startServer() {
        println("Sever Running in port $port")
        while (true) {
            val clientSocket = serverSocket.accept()
            ClientHandler().handleClient(clientSocket, routeHandler)
        }
    }

    fun stopServer() {
        serverSocket.close()
    }

}
