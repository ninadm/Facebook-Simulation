package Helper

/**
  * Created by Ninad on 25-11-2015.
  */
// Define all statistics here. Time for API to be called and percentage wise stats
object Statistics {
  var NumberofUsers: Int = 15000
  var InitialFriendsToSeed: Int = 1
  var NumberOfSuccessfulRequest : Int = 0
  var GenderRatioFemales: Double = 66
  var ageRatio10To20: Double = 33
  var ageRatio21To30: Double = 24
  var ageRatio31To100: Double = 100 - (ageRatio21To30 + ageRatio10To20)
  var portNumber: String = "8181"
}
