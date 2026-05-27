# Check - Instalacja i Konfiguracja
# 1. Opis projektu
Aplikacja śledząca nawyki napisana w Kotlinie z użyciem Jetpack Compose i MVVM oraz Firebase.

# 2. Wymagania
* **Android Studio:** Panda lub nowszy
* **JDK:** 17 lub nowszy
* **Android SDK:** 36
* **Gradle:** 9.3.x
* **Kotlin:** 2.3.x
* **AGP:** 9.1.1

# 3. Konfiguracja Środowiska (Android)

```bash
git clone https://github.com/Szpont-Company/NawykUZ_App.git
cd NawykUZ_App
```

### Otwórz projekt (NawykUZ_App) w Android Studio i poczekaj, aż wszystkie zależności zostaną pobrane.

Wejdź do [konsoli Firebase](https://console.firebase.google.com), utwórz nowy projekt, następnie dodaj do niego aplikację (Android)
wpisując com.SzpontCompany.check w polu **Android package name** lub skorzystaj z gotowego projektu i pobierz wygenerowany plik *google-services.json*,
następnie umieść go w folderze app w strukturze projektu.

#### ! Do obsługi map wymagany jest także klucz API Google Maps !
Aby uzyskać klucz należy wejść do [konsoli Google Cloud](https://console.cloud.google.com),
zaloguj się tym samym kontem co do Firebase i wybierz utworzony przez Firebase projekt, w górnym pasku wyszukiwania wyszukaj
**Maps SDK for Android** oraz **Maps Static API** i włącz obie usługi,
następnie wyszukaj interfejsy API i usługi, przejdź do zakładki Dane logowania.

W menu Utwórz dane logowania wybierz *Klucz interfejsu API*, w ograniczeniach interfejsu wybierz wcześniej wspomniane usługi map.\
Dodaj ograniczenia aplikacji na Androida podając nazwę pakietu oraz odcisk cyfrowy SHA-1 wygenerowany według instrukcji na stronie.

Klucz powinien zostać umieszczony w *local.properties*
```bash
MAPS_API_KEY=AIza...TwójKlucz
```

# 4. Konfiguracja Backendu (Firebase functions)

### Wymagania
- Node.js 20+
- npm
- Firebase CLI
- Dostęp do projektu Firebase wraz z uprawnieniami

Globalna instalacja Firebase CLI:
```bash
npm install -g firebase-tools
```

Klonowanie projektu i instalacja zależności
```bash
git clone https://github.com/Szpont-Company/NawykUZ_Backend.git
cd NawykUZ_Backend
npm install
```

### Projekt backendu można otworzyć w dowolnym środowisku wspierającym TypeScript, na przykład JetBrains WebStorm lub Visual Studio Code.

Po zainstalowaniu wszystkich zależności należy w terminalu zalogować się do firebase przy pomocy polecenia:
```bash
firebase login
firebase use --add 
```
Następnie do deployu funkcji należy użyć:
```bash
firebase deploy
```


# 5. Testowanie

### Do uruchomienia testów należy użyć polecenia

```bash
./gradlew test
```

### Lub skorzystać z dostępnych w Android Studio narzędzi

# 6. Dobre praktyki

## W przypadku aktualizowania projektu lub rozwoju w nowym repozytorium NIE wrzucaj:
- Kluczy API
- Tokenów
- Haseł
- **google-services.json**