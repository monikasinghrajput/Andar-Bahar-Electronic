package model.common.data

case class Player(clientIp: String = "",
                  nickname: String = "Guest",
                  currency: String = "INR",
                  uid: String = "-1",
                  status: String = "offline",
                  usage: String = "unlocked",
                  balance: Double = 0) {
}
