build (without tests)
    ./gradlew build -x test -x spotbugsMain -x spotbugsTest -x spotbugsJmh
publish 
    ./gradlew publishToMavenLocal

publish
    ./gradlew publish -PmavenPassword=XXXX -PmavenUser=roland

reformat
    ./gradlew spotlessApply

git submodule status