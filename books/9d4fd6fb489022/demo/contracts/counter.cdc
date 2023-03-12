pub contract CounterContract {
  pub resource interface HasCount {
    pub var count: Int
  }

  pub resource Counter: HasCount {
    pub var count: Int
    init(count: Int) {
      self.count = count
    }
    pub fun increment() {
      self.count = self.count + 1
    }
  }

  pub fun createCounter(): @Counter {
    return <- create Counter(count: 1)
  }
}

