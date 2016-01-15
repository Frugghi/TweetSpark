#!/bin/bash
source "/home/vagrant/resources/common.sh"

while getopts j: option
do
	case "${option}"
	in
		j) JAVA_VERSION=${OPTARG};;
	esac
done

function installLocalJava {
	echo "Installing JDK $JAVA_MAJOR_VERSION update $JAVA_BUILD_VERSION from local archive"
	FILE=$VAGRANT_RES_DIR/$JAVA_ARCHIVE
	tar -xzf $FILE -C /usr/local
}

function installRemoteJava {
	echo "Installing JDK $JAVA_MAJOR_VERSION update $JAVA_BUILD_VERSION from remote source"
	yum install -y `echo $JAVA_ARCHIVE | cut -d '.' -f 1`
}

function setupJava {
	echo "Setting up Java"
	if resourceExists $JAVA_ARCHIVE; then
		ln -s /usr/local/jdk1.$JAVA_MAJOR_VERSION.0_$JAVA_BUILD_VERSION /usr/local/java
	else
		ln -s /usr/lib/jvm/jre /usr/local/java
	fi
}

function setupEnvVars {
	echo "Creating Java environment variables"
	echo export JAVA_HOME=/usr/local/java >> /etc/profile.d/java.sh
	echo export PATH=\${JAVA_HOME}/bin:\${PATH} >> /etc/profile.d/java.sh
}

function installJava {
	if resourceExists $JAVA_ARCHIVE; then
		installLocalJava
	else
		installRemoteJava
	fi
}

echo "Setup Java"

setupJavaVariables "$JAVA_VERSION"
installJava
setupJava
setupEnvVars
