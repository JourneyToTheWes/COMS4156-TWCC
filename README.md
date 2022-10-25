# COMS4156-TWCC

## 1. Build, Run, and Test Server
*Ensure that Maven is installed before these commands can be run*
> Docker setup (without having to setup Maven on your host machine):
> 1. Install Docker
> 2. Clone repo & cd to repo root directory
> 3. docker pull maven:latest
> 4. docker run -it v $(pwd):/{root-dir-folder-name} --name {container-name} -p 8080:8080 maven:latest bash
>       - Run maven image while mounting repo on host machine to Docker container and exposing port
> 5. (Optional) if exited container, start back up: docker start {container-name} -i
- Build: To build the app run `mvn install`
- Run: To run the app run `mvn spring-boot:run`
- Test (locally - requires MySQL configured with database set up): After running the app, we can access our app through the endpoints at localhost:{exposed-port} (localhost:8080) and appending any route specified in documentation (i.e. localhost:8080/events).
    - `src/main/resources/application.properties` database properties are required to run the app successfully connecting to the database. Make sure to replace placeholder values.

## 2. Documentation