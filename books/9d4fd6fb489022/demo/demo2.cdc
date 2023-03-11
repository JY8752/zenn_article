pub resource User {
  pub let name: String
  init(name: String) {
    self.name = name
  }
}

pub fun getUserResource(exist: Bool): @User? {
  if exist {
    return <- create User(name: "user")
  } else {
    return nil
  }
}

pub fun main(): String {
  // var user <- getUserResource(exist: false)
  var user <- getUserResource(exist: true)
  user <-! create User(name: "force user")
  let name = user?.name

  destroy user

  return name ?? "empty"
}

