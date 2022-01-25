### Matching Engine
An implementation in Kotlin of a matching engine continuously
trying to match the incoming BUY and SELL orders. Produces
crossing trades on any match

ANDOB stands for Andrey's Order Book

### Project structure
- `kotlin` contains the source code
- `resources` input files to test the engine
- `build` a pre-compiled library

### Description
- The engine reads orders from the `stdin`
- It prints any matched trades on `stdout` on every input
- After all inputs are read, it prints the state of the book

### Build
These instructions are for a Windows environment:
- Include Kotlin `bin` and Java `bin` in the PATH
- Compile, i.e. create a runnable jar 

```
set Path=%Path%;C:\jdk\kotlinc\bin;;C:\jdk\openjdk-17.0.1\bin
kotlinc kotlin\Main.kt kotlin\andob -include-runtime -d build\andob.jar
```

### Batch Run
```
kotlin -cp build\andob.jar MainKt < resources\michael_orders.txt

TRADE   BTCUSD  abe14   12345   5       10000.0
TRADE   BTCUSD  abe14   13471   2       9971.0
TRADE   ETHUSD  plu401  11431   5       175.0
zod42   SELL    BTCUSD  2       10001.0
13471   BUY     BTCUSD  4       9971.0
45691   BUY     ETHUSD  3       180.0
11431   BUY     ETHUSD  4       175.0
```

### Interactive Run
```
kotlin -cp build\andob.jar MainKt
```
Start typing orders 


