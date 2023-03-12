import Hello from "../contracts/Hello.cdc"

pub fun main(): UInt64 {
  Hello.hello()
  return Hello.getCount()
}