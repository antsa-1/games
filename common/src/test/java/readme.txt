8.2.2022

In order to run TicTacToe JUnit -tests in Eclipse 2021-12
-> Right click src/test/java
-> Select "Run as" -> "Run Configurations..."
-> Arguments tab -> Append to VM arguments text

--add-exports org.junit.platform.commons/org.junit.platform.commons.util=ALL-UNNAMED --add-exports org.junit.platform.commons/org.junit.platform.commons.logging=ALL-UNNAMED


Apply
Right click "src/test/java" -> Run as JUnit test
