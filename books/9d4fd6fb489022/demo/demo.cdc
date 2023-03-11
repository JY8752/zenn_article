// コメント
pub let x = 1

/*
コメント1
コメント2
コメント3
 */
pub let y = 1

// add関数
pub fun add(_ num1: Int, _ num2: Int): Int {
  let num3 = 1.11
  return num1 + num2
}

pub var b = 1

pub fun test() {
  let anyValue: AnyStruct? = 1
  let doubleOptional = anyValue as? Int?

  let intValue = (doubleOptional ?? panic("castに失敗している")) ?? panic("値がない")

  let str: Character = "\u{FF}"
}

pub fun main(): String {
  let char: Character = "\u{FF}"
  let utf8 = char.toString().utf8

  let thumbsUpText =
    "This is the first line.\nThis is the second line with an emoji: \u{1F44D}"

  let hello = "Hello"
  let world = "World"

  let helloWorld = hello.concat(world)

  let arr: [Int] = [1, 2]
  arr.append(4)

  let doubleArr = [[1,2], [3,4]]

  let empty: {String:String} = {}
  // {String:Int}
  let stringToInt = {
    "key1": 1,
    "key2": 2
  }

  stringToInt["key1"] // 1
  stringToInt["key2"] // 2
  stringToInt["key3"] // nil


  return helloWorld
  // return String.fromUTF8(utf8)!
}

