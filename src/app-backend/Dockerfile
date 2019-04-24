FROM openjdk:8-jre

COPY server/target/scala-2.11/pointcloud-server.jar /srv/

EXPOSE 7070

ENTRYPOINT ["java"] 
CMD ["-Xmx1G", "-jar", "/srv/pointcloud-server.jar"]
