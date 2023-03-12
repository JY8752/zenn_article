pub contract Hello {
  pub var count: UInt64
  pub var id: UInt64

  pub let helloStoragePath: StoragePath
  pub let helloPublicPath: PublicPath

  pub fun hello(): String {
    self.count = self.count + 1
    return "Hello World!!"
  }

  pub fun helloName(name: String): String {
    return "Hello, ".concat(name).concat("!!")
  }

  view pub fun getCount(): UInt64 {
    return self.count
  }

  pub resource Token {
    pub let id: UInt64
    init(id: UInt64) {
      self.id = id
    }
  }

  pub fun mintHelloToken(): @Token {
    let token <- create Token(id: self.id)
    self.id = self.id + 1
    return <- token
  }

  init() {
    self.count = 0
    self.id = 0

    self.helloStoragePath = /storage/hello
    self.helloPublicPath = /public/hello

    self.account.save(<- self.mintHelloToken(), to: self.helloStoragePath)
    self.account.link<&Token>(self.helloPublicPath, target: self.helloStoragePath)
  }
}