pub fun test(bool: Bool, bool2: Bool) {
  // ()はなくても大丈夫
  if bool {
    log("hello")
  } else {
    log("world")
  }

  // ()をつけてもコンパイルエラーにはならない。
  if(bool) {
    log("hello")
  } else if(bool2) {
    log("world")
  } else {
    log("!!")
  }

  let maybeNumber: Int? = 1

  if let number = maybeNumber {
    log(number)
  } else {
    log("値がありません")
  }

  var counter = 0
  while counter < 5 {
    counter = counter + 1
  }

  let array = ["Hello", "World", "Foo", "Bar"]

  for element in array {
      log(element)
  }

  // The loop would log:
  // "Hello"
  // "World"
  // "Foo"
  // "Bar"

  for index, element in array {
      log(index)
  }

  // The loop would log:
  // 0
  // 1
  // 2
  // 3

  let dictionary = {"one": 1, "two": 2}
  for key in dictionary.keys {
      let value = dictionary[key]!
      log(key)
      log(value)
  }

  // The loop would log:
  // "one"
  // 1
  // "two"
  // 2

  var i = 0
  var x = 0
  while i < 10 {
      i = i + 1
      if i < 3 {
          continue
      }
      x = x + 1
  }
  // `x` is `8`


  let arr = [2, 2, 3]
  var sum = 0
  for element in arr {
      if element == 2 {
          continue
      }
      sum = sum + element
  }

}