when using bintray please include your username and key via

bintray_username= '_ _ _'

bintray_api_key=  '_ _ _'

in a gradle.properties file, dont commit the gradle.properties file!!

if there is a problem uploading a project due to the same rev number, comment out the bintray code from the gradle build of that project so it will be ignored
