
pub fun add(_ x: Int, _ y: Int): Int {
  pre {
    x > 0; y > 0:
      "xとyは正数で指定してください"
  }

  post {
    result <= 100:
      "計算結果が100を超えないように指定してください"
    
  }

  return x + y
}

pub var n = 0

pub fun incrementN() {
    post {
        // Require the new value of `n` to be the old value of `n`, plus one.
        //
        n == before(n) + 2:
            "n must be incremented by 1"
    }

    n = n + 1
}

// pub fun main(x: Int, y: Int): Int {
//   return add(x, y)
// }

pub fun main() {
  incrementN()
}