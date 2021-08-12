To build the docker image:

1. build the app

```
cd ..
mvn clean package
```

2. copy the JAR package to this folder and rename it to `sample-output-generator.jar`

```
cp jars/sample-output-generator-1.0-SNAPSHOT-jar-with-dependencies.jar docker/sample-output-generator.jar 
cd docker
```

3. Build the docker image

docker build -t hacking-hockey/sample-output-generator .

4. Run it
docker run --name sample-output-generator -p 5701:5701 hacking-hockey/sample-output-generator
