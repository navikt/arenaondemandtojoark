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

# sftp
if test -f /var/run/secrets/nais.io/vault/privatekeyfile;
then
    echo "Setting ARENAONDEMANDTOJOARK_SFTP_PRIVATEKEYFILE"
    export ARENAONDEMANDTOJOARK_SFTP_PRIVATEKEYFILE=/var/run/secrets/nais.io/vault/privatekeyfile
fi
if test -f /var/run/secrets/nais.io/vault/privatekeypassphrase;
then
    echo "Setting ARENAONDEMANDTOJOARK_SFTP_PRIVATEKEYPASSPHRASE"
    export ARENAONDEMANDTOJOARK_SFTP_PRIVATEKEYPASSPHRASE=$(cat /var/run/secrets/nais.io/vault/privatekeypassphrase)
fi