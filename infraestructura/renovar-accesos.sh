#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
umask 077

archivo=.accesos-evaluacion
if [[ -f $archivo ]]; then
    printf 'Los accesos de evaluación ya están configurados.\n'
    exit 0
fi

temporal=$(mktemp .accesos-evaluacion.XXXXXX)
trap 'rm -f -- "$temporal"' EXIT
variables=()
for cuenta in jugador invitado organizador; do
    clave=$(openssl rand -hex 18)
    hash=$(printf '%s\n' "$clave" | docker run --rm -i httpd:2.4-alpine htpasswd -nBiC 12 "$cuenta" | cut -d: -f2)
    [[ $hash =~ ^\$2[ayb]\$12\$[A-Za-z0-9./]{53}$ ]]
    variables+=("SET @hash_$cuenta = '$hash';")
    printf '%s@nexogamer.test\t%s\n' "$cuenta" "$clave" >> "$temporal"
done
{
    printf '%s\n' "${variables[@]}"
    cat base-datos/03_credenciales_azure.sql
} | docker compose -f compose.yaml -f compose.azure.yaml exec -T base-datos \
    sh -c 'MYSQL_PWD="$MYSQL_PASSWORD" mysql -u "$MYSQL_USER" "$MYSQL_DATABASE"'
mv -- "$temporal" "$archivo"
printf 'Accesos de evaluación configurados.\n'
