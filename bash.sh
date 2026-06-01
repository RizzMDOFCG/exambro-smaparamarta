# ... (kode copy file sama seperti di atas) ...

  curl -F document=@"Exambro_Paramarta_Terbaru.apk" \
       -F caption="🎓 *Pembaruan Sistem: Exambro SMA Paramarta* 🎓
       
Pembaruan terbaru untuk aplikasi Exambro telah berhasil dikompilasi. 

Mohon berkenan untuk mengunduh dan memasang versi ini guna memastikan stabilitas sistem ujian kita. Umpan balik Anda sangat berarti untuk penyempurnaan aplikasi. Terima kasih! ✨" \
       -F parse_mode="Markdown" \
       "https://api.telegram.org/bot8825507034:AAE6-v3lKkBrfjJJ3lTR7dtaXIwfr1GzHJ8/sendDocument?chat_id=$ID"