import Hello from "../contracts/Hello.cdc"

pub fun main(name: String): String {
  return Hello.helloName(name: name)
}