# CryptoRefsFinder
Finds crypto references in project. just for my internal project. It searches refs in all go files.

It needs Java8 to compile and run.

### Build:
`mvn clean assembly:assembly`

### Run:
`java -jar -Dpath=<path of the go project>  crypto-1.0-SNAPSHOT-jar-with-dependencies.jar`

> If path variable is not provided, it starts searching within the current directory
