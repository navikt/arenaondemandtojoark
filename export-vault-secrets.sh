#!/usr/bin/env sh

if test -f /var/run/secrets/nais.io/arenaondemandtojoarkDB/username;
then
    echo "Setting SPRING_DATASOURCE_USERNAME"
    export SPRING_DATASOURCE_USERNAME=$(cat /var/run/secrets/nais.io/arenaondemandtojoarkDB/username)
fi

if test -f /var/run/secrets/nais.io/arenaondemandtojoarkDB/password;
then
    echo "Setting SPRING_DATASOURCE_PASSWORD"
    export SPRING_DATASOURCE_PASSWORD=$(cat /var/run/secrets/nais.io/arenaondemandtojoarkDB/password)
fi