package model.ab.message

import model.ab.data.ConfigData

case class ConfigUpdateMsg(MessageType: String = "configUpdate", //Important
                           configData: ConfigData,
                           timestamp: String = "")
