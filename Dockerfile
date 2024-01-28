FROM maven:3.9.4-eclipse-temurin-21-alpine as build
WORKDIR /build
COPY src src
COPY pom.xml pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn clean package -Dmaven.test.skip=true

# При помощи ключевого слова FROM необходимо указать исходный образ,
# который мы будем использовать для создания своего.

FROM eclipse-temurin:21-alpine

# Желательно запускать приложения не от имени суперпользователя, который
# используется по умолчанию, поэтому нужно создать пользователя и группу
# для запуска приложения.

RUN addgroup spring-boot-group && adduser --ingroup spring-boot-group --disabled-password spring-boot
USER spring-boot

# Иногда требуется получить доступ к файлам, генерирующимся в процессе выполнения,
# для этого зарегистрируем том /tmp

VOLUME /tmp

# Со временем у проекта будет изменяться версия, и чтобы не изменять всякий раз
# этот Dockerfile имя jar-файла вынесем в аргумент. Альтернативно можно указать
# постоянное имя jar-файла в Maven при помощи finalName.

ARG JAR_FILE=food-observer-bot-0.0.1-SNAPSHOT.jar

# Создадим рабочую директорию проекта

WORKDIR /application

# Скопируем в рабочую директорию проекта JAR-файл проекта и его зависимости

COPY --from=build /build/target/${JAR_FILE} application.jar

# В конце укажем точку входа. Выбран вариант с использованием exec для того, чтобы
# можно было передать в строку запуска дополнительные параметры запуска - JAVA_OPTS, а так же
# ${0} и ${@} для передачи аргументов запуска.

CMD exec java ${JAVA_OPTS} -jar application.jar ${0} ${@}