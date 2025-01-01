FROM ubuntu as builder

USER root

RUN apt-get update && apt-get install -y curl locales && rm -rf /var/lib/apt/lists/* \
	&& localedef -i en_US -c -f UTF-8 -A /usr/share/locale/locale.alias en_US.UTF-8

ENV LANG en_US.utf8

RUN curl -O https://download.oracle.com/graalvm/21/latest/graalvm-jdk-21_linux-x64_bin.tar.gz

RUN tar -xvf graalvm-jdk-21_linux-x64_bin.tar.gz -C /opt

ENV GRAALVM_HOME=/opt/graalvm-jdk-21.0.5+9.1

ENV JAVA_HOME=${GRAALVM_HOME}

ENV PATH=${JAVA_HOME}/bin:${PATH}

COPY . .

RUN ./gradlew nativeCompile

CMD ["/build/native/nativeCompile/leone"]