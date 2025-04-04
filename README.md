## Testing commands
1) Unit tests: ./gradlew clean testDebugUnitTest jacocoTestReport
2) Android tests: ./gradlew clean connectedDebugAndroidTest jacocoAndroidTestReport
3) All test report: ./gradlew clean jacocoFullReport
Look for any line in that says "Skipping task ':app:jacocoTestReport' as task onlyIf 'Any of the execution data files exists' is false."
If it exists, meaning the jacocoTestReport did not find the test exec file needed to generate the code coverage report
