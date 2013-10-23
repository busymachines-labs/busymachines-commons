DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
java -server -XX:MaxPermSize=250M -Xmx800m -Djava.awt.headless=true -jar $DIR/sbt-launch.jar $*
