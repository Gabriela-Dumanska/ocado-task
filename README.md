# Payment Optimizer 🚀

**Stażowy projekt rekrutacyjny do Ocado**

---

## 📖 Opis projektu
Ten projekt rozwiązuje problem optymalnego przydziału metod płatności do zamówień z różnymi promocjami. Problem ten jest **NP-zupełny**, więc dla dużych danych wybranie rozwiązania "idealnego" staje się nieopłacalne czasowo. Dlatego zdecydowałam się zaimplementować **algorytm zachłanny**, który w praktyce działa bardzo szybko, choć nie gwarantuje absolutnej optymalności rozwiązania.

## ⚙️ Jak uruchomić
W głównym folderze sklonowanego projektu uruchom aplikację:
   ```bash
   java -jar app.jar <ścieżka_do_pliku_orders.json> <ścieżka_do_pliku_paymentmethods.json>
   ```

## 🔍 Opis algorytmu zachłannego

W projekcie zastosowano klasyczne podejście zachłanne do problemu przydziału dostępnych metod płatności do zamówień w taki sposób, aby maksymalizować łączny zysk z promocji, przy jednoczesnym poszanowaniu limitów każdej metody.

### Modelowanie opcji:

Dla każdego zamówienia tworzymy listę możliwych „opcji” przypisania: każda opcja zawiera informację o potencjalnym zysku i odpowiadającym mu koszcie płatności z użyciem danej metody.

### Kryterium zachłanne:

Obliczamy gęstość zysku dla każdej opcji, definiowaną jako stosunek zysku do kosztu.

Sortujemy opcje malejąco wg tej gęstości.

### Przypisanie:

Iterujemy po posortowanych opcjach i przypisujemy je do zamówień, gdy metoda płatnicza ma wystarczający limit, co zapewnia lokalnie optymalny wybór na każdym kroku.

### Obsługa pozostałości:

Niewykorzystane kwoty płatności rozdzielane są kolejno między dostępne metody według ustalonej kolejności priorytetów.

Dzięki temu algorytm działa w czasie:

**O(m·log m + m) ≈ O(m·log m)**,

gdzie m to liczba wszystkich rozważanych opcji (w przybliżeniu iloczyn liczby zamówień i średniej liczby promocji).
