package generalknowledge.mywings.com.smartdustbinsystem.models

data class Dustbin(
    var id: Int = 0,
    var name: String = "",
    var local: String = "",
    var latitude: String = "",
    var longitude: String = "",
    var weight: String = "",
    var moisture: String = "",
    var vid: Int = 0,
    var distance: Int = 0

)
