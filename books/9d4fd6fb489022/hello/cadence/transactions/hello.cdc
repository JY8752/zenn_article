import Hello from "../contracts/Hello.cdc"

transaction {
  execute {
    Hello.hello()
  }
}