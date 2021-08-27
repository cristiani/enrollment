Test School Registration System.

This spring boot application was developed with Intellij Community Edition.
The project structure is the one recommended in [Spring docs](https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.structuring-your-code).
It is by no means complete, there are three ToDos that mark the missing logic. 
Also, it is not tested completely, and tests are at a very minimal level, just few 
for the controllers, no tests for service nor repository. This is due to lack of time.
However, I think the current status shows my level of knowledge and expertise. Hopefully
you will agree with me. Anyway, I have enjoyed working on this, and see what means
now to build a SpringBoot app.
To execute this service locally, you need access to build and run either via
maven executing:
```
mvn clean
mvn install
mvn spring-boot:run
```
or via Intellij using the Run/Debug configuration included as png.

![Configuration](https://github.com/cristiani/enrollment/blob/master/IntelliJ%20Run-Debug%20Configuration.png)

There is a swagger information via:
```
http://localhost:8085/v3/api-docs
localhost:8085/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config#/
```

In root folder of the project there is also a postman collection to use with the respective tool, file
```
Enrollment.postman_collection.json
```
To build the docker image:
```
docker build -t spring-enrollment-test .
```
and then using the local docker image execute the docker-compose file
```
docker-compose up
```

Now browser and/or postman invocations should work.
