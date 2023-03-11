pub fun main() {
  let a = 1 + 1
  let b = 2 - 1
  let c = 1 * 2
  let d = 10 / 2
  let e = 6 % 5

  log("add: 1 + 1 = ".concat(a.toString()))
  log("subtract: 2 - 1 = ".concat(b.toString()))
  log("multiple: 1 * 2 = ".concat(c.toString()))
  log("divide: 10 / 2 = ".concat(d.toString()))
  log("over: 6 % 5 = ".concat(e.toString()))

  // NG
  // let user = User(name: "user")

  // OK
  // let user <- create User(name: "user")

  var x = 1
  var y = 2
  var z = 3

  x <-> y

  log("x = ".concat(x.toString()))
  log("y = ".concat(y.toString()))
  log("z = ".concat(z.toString()))

  x <-> y
  y <-> z

  log("x = ".concat(x.toString()))
  log("y = ".concat(y.toString()))
  log("z = ".concat(z.toString()))

  x > y
  x < y
  x >= y
  x <= y

  let num: Int? = nil
  let num2 = num ?? 3

  let num3 = num!

}
 
pub resource User {
  pub let name: String
  init(name: String) {
    self.name = name
  }
}

