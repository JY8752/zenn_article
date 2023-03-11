pub struct interface Add {
  access(contract) let x: Int
  access(contract) let y: Int

  pub fun add(): Int {
    pre {
      self.x > 0; self.y > 0:
        "xとyは正数で指定してください"
    }
    post {
      result <= 100:
        "計算結果は100を超えないようにしてください"
    }
  }

  // デフォルト実装
  pub fun hello() {
    log("Hello World")
  }
}

pub struct Calculator: Add {
    access(contract) let x: Int

    access(contract) let y: Int

    pub fun add(): Int {
      return self.x + self.y
    }

    pub fun substruct(): Int {
      return self.x - self.y
    }

    init(x: Int, y: Int) {
      self.x = x
      self.y = y
    }
}

pub fun main() {
  let calc:{Add} = Calculator(x: 1, y: 2)
  let result = calc.add()

  log(result)
  calc.hello()

  let red = Color.red
  red.rawValue // 0

  let green = Color(rawValue: 1) // Color.Green
  
  let nothing = Color(rawValue: 5) // nil
}

pub enum Color: UInt8 {
    pub case red
    pub case green
    pub case blue
}