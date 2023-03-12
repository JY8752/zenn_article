import Hello from "../contracts/Hello.cdc"

pub fun main(): String {
  log("start script!!")
  return Hello.hello()
}