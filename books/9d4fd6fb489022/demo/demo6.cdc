import User from "./contracts/demo5.cdc"

// transaction(name: String) {
//   prepare(acc: AuthAccount) {
//     User.updateAndGetName(name: name)
//   }
// }

pub fun main(): String {
  return User.getName()
}