# Java単体テストの書き方

- １メソッドごとに@Nestedなインナークラスを作る
- 必ず@DisplayNameを日本語で書く
- @DisplayNameには、「条件」と「アクション」と「期待する挙動」を書く。
  - 例：パラメータを指定しない場合、POST /user はBad Requestを返す
- テストメソッド名はDisplayNameに即した英語名とする。
- AAAパターンのテストとする。// Arrange, // Act, // Assertのコメントを書いてAAAパターンであることを意識しやすくする。
- パスカバレージができるだけ100%になるようにする。
- Mockitoのモックを利用して、以下のようなメソッドのテストでは

  ```
  SomeClass1 someMethod(String arg) {
    var result = service.doSomething(arg.substring(2)); // ←Mockで置き換える
    return doAnotherthing(result);
  }
  ```

  1. 引数argを渡したらserviceのメソッドの引数が～～になる
  2. serviceのメソッドが～～を返したら対象メソッドは～～を返す

  のように、直前の入力に対して出力が適切かを検査する。
