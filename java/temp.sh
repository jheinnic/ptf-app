#!/bin/sh

mvn archetype:generate -DarchetypeGroupId=name.jchein.portfolio.maven.archetype -DarchetypeArtifactId=service-gateway -DarchetypeVersion=0.0.1-SNAPSHOT -DgroupId=name.jchein.portfolio.services -DartifactId=paint.gateway -Dversion=0.0.1-SNAPSHOT -Dpackage=name.jchein.portfolio.services.paint.gateway

