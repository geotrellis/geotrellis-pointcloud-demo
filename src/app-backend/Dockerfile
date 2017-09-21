FROM openjdk:8-jre

COPY server/target/scala-2.11/pointcloud-server-assembly-0.1.0-SNAPSHOT.jar /srv/

EXPOSE 7070

ENTRYPOINT ["java"] 
CMD ["-Xmx1G", "-jar", "/srv/pointcloud-server-assembly-0.1.0-SNAPSHOT.jar"]
