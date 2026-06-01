#!/bin/bash

# Bikin angka random otomatis
ANGKA_RANDOM=$RANDOM
NAMA_FILE="Exambro_Paramarta_Terbaru_${ANGKA_RANDOM}.apk"

# 1. Gandakan APK aslinya dan beri nama baru dengan angka random di belakangnya
cp app/build/outputs/apk/debug/app-debug.apk "$NAMA_FILE"

# 2. Kirim ke dua ID menggunakan looping
for ID in 6071587883 7520038758; do
  curl -F document=@"$NAMA_FILE" \
         -F caption="Versi terbaru Exambro sudah siap. Silakan diuji coba! (Build ID: $ANGKA_RANDOM)" \
                "https://api.telegram.org/bot8825507034:AAE6-v3lKkBrfjJJ3lTR7dtaXIwfr1GzHJ8/sendDocument?chat_id=$ID"
                done
                