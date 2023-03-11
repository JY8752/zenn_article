pub contract User {
  pub var name: String

  view pub fun getName(): String {
    return self.name
  }

  pub fun updateName(name: String) {
    self.name = name
  }

  view pub fun updateAndGetName(name: String): String {
    self.updateName(name: name)
    return self.name
  }

  init() {
    self.name = "user"
  }
}