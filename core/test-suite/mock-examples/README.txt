To use these mocked examples, cd to the core directory, and call the gradle wrapper with the given arguments.
Two command lines are needed: one to prepare and serve the mocks and one to execute the tests.
These tests were run on Windows. For Linux, use ./gradlew instead of gradlew.bat and replace '/' with '\'.

# minimal_hello
gradlew.bat run --args="--input=test-suite\mock-examples\minimal_hello.puml --mock="B" --config=test-suite\mock-examples\minimal_hello_config.toml"
gradlew.bat run --args="--input=test-suite\mock-examples\minimal_hello.puml --execute --config=test-suite\mock-examples\minimal_hello_config.toml"

# complex_hello
gradlew.bat run --args="--input=test-suite\mock-examples\complex_hello.puml --mock="B" --config=test-suite\mock-examples\complex_hello_config.toml"
gradlew.bat run --args="--input=test-suite\mock-examples\complex_hello.puml --execute --config=test-suite\mock-examples\complex_hello_config.toml"
Notes:
the condition should be written like "'${condition}' == 'value'", otherwise only booleans will work
the pattern of alts have to be alternative responses and cannot include requests

# rerouting
gradlew.bat run --args="--input=test-suite\mock-examples\rerouting.puml --mock="VM,CRS" --config=test-suite\mock-examples\rerouting_config.toml"
gradlew.bat run --args="--input=test-suite\mock-examples\rerouting.puml --execute --config=test-suite\mock-examples\rerouting_config.toml"
Notes:
you must set voiceEstablished, and you can set it to true, 'true', "true" or to false and its variations.

#xcall
gradlew.bat run --args="--input=test-suite\mock-examples\xcall.puml --mock="DataService,CCC,CRS,VM" --config=test-suite\mock-examples\xcall_config.toml"
gradlew.bat run --args="--input=test-suite\mock-examples\xcall.puml --execute --config=test-suite\mock-examples\xcall_config.toml"
