#!/usr/bin/env bash
# Genera el keystore de firma release FUERA del repositorio y el archivo de
# propiedades que Gradle lee para firmar el APK. Nada de esto se versiona.
#
#   bash scripts/generar_keystore.sh [alias] [validez_dias]
#
# Por defecto:
#   Keystore : ~/.config/teqruta/teqruta-release.jks
#   Properties: ~/.config/teqruta/keystore.properties
#   Alias    : teqruta-release
#   Validez  : 10000 días (~27 años)
#
# La contraseña es aleatoria y queda solo en el archivo de propiedades (permisos
# 600). Guarda ese archivo y el .jks en un lugar seguro: si los pierdes no podrás
# publicar actualizaciones firmadas con la misma clave.
#
# Alternativa en CI: definir los secretos TEQRUTA_KEYSTORE_BASE64,
# TEQRUTA_KEYSTORE_PASSWORD, TEQRUTA_KEY_ALIAS y TEQRUTA_KEY_PASSWORD.
set -euo pipefail

ALIAS="${1:-teqruta-release}"
DIAS="${2:-10000}"
CONFIG_DIR="${TEQRUTA_KEYSTORE_DIR:-$HOME/.config/teqruta}"
KEYSTORE="$CONFIG_DIR/teqruta-release.jks"
PROPIEDADES="$CONFIG_DIR/keystore.properties"

if ! command -v keytool >/dev/null 2>&1; then
  echo "[keystore] keytool no está disponible. Instala un JDK (por ejemplo: sudo apt install openjdk-21-jdk)." >&2
  exit 1
fi

if [[ -f "$KEYSTORE" || -f "$PROPIEDADES" ]]; then
  echo "[keystore] Ya existen credenciales en $CONFIG_DIR; no se sobreescriben."
  echo "[keystore] Elimina $KEYSTORE y $PROPIEDADES manualmente si quieres regenerarlas."
  exit 0
fi

mkdir -p "$CONFIG_DIR"
chmod 700 "$CONFIG_DIR"

# Contraseña aleatoria sin caracteres problemáticos para el .properties.
PASSWORD="$(head -c 48 /dev/urandom | base64 | tr -dc 'A-Za-z0-9' | head -c 32)"

keytool -genkeypair \
  -keystore "$KEYSTORE" \
  -storetype PKCS12 \
  -alias "$ALIAS" \
  -keyalg RSA \
  -keysize 4096 \
  -validity "$DIAS" \
  -storepass "$PASSWORD" \
  -keypass "$PASSWORD" \
  -dname "CN=TeqRuta, OU=DSY1105, O=TEQMED SpA, L=Concepción, ST=Biobío, C=CL"

cat > "$PROPIEDADES" <<EOF
# Credenciales de firma release de TeqRuta. NO versionar.
# Generado por scripts/generar_keystore.sh
storeFile=$KEYSTORE
storePassword=$PASSWORD
keyAlias=$ALIAS
keyPassword=$PASSWORD
EOF

chmod 600 "$KEYSTORE" "$PROPIEDADES"

cat <<EOF
[keystore] Keystore generado:   $KEYSTORE
[keystore] Propiedades:         $PROPIEDADES
[keystore] Alias:               $ALIAS
[keystore] Validez:             $DIAS días
[keystore] Ya puedes firmar:    ./gradlew :app:assembleRelease
[keystore] Respalda ambos archivos en un lugar seguro y fuera del repositorio.
EOF
