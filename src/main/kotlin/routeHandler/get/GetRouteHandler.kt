package routeHandler.get

import metaData.ConfigFileReaderWriter
import metaData.csv.CsvContentReader
import request.RequestHandler

class GetRouteHandler {
    private val requestHandler = RequestHandler()
    private val fileGetter = FileGetter()

    fun handleGetRequest(request: String): String {
        return when (val path = requestHandler.getPath(request)) {
            "/" -> fileGetter.serveFile("/index.html")
            "/validate" -> {
                val configFileReaderWriter = ConfigFileReaderWriter("src/main/public/files/csv-config.json")
                val csvContentReader = CsvContentReader("src/main/public/files/uploaded.csv")
                CsvValidator(configFileReaderWriter, csvContentReader).handleCsv()
            }
            "/get-config-names" -> {
                val configName = ConfigName()
                configName.getConfigNames()
            }
            "/get-config" -> {
                val getConfigByName = GetConfigByName()
                getConfigByName.handle(request)
            }
            "/get-error" -> {
                val getError = GetError()
                getError.get(request)
            }
            else -> fileGetter.serveFile(path)
        }
    }

}