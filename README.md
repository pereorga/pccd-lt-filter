# lt-filter

Programa per filtrar frases en català amb LanguageTool. Separa les frases correctes de les que contenen possibles errors gramaticals o ortogràfics.

## Instal·lació i ús (requereix npm i JRE 17+)

```bash
npm install @pccd/lt-filter
```

```bash
# Per defecte, les frases correctes van a stdout i les incorrectes a stderr
npx lt-filter input.txt > correct.txt 2> flagged.txt

# Si no s'especifica un fitxer, es llegeix l'entrada estàndard
cat input.txt | npx lt-filter > correct.txt 2> flagged.txt

# Només frases correctes
npx lt-filter --correct input.txt > correct.txt

# Només frases potencialment incorrectes
npx lt-filter --flagged input.txt > flagged.txt
```

Si no, es pot baixar directament el [fitxer JAR](bin/lt-filter.jar):

```bash
wget https://github.com/pereorga/pccd-lt-filter/raw/refs/heads/master/bin/lt-filter.jar
java -jar lt-filter.jar --help
```

## Opcions de la línia d'ordres

| Opció           | Descripció                                          |
| --------------- | --------------------------------------------------- |
| `-c, --correct` | Envia les frases correctes a stdout                 |
| `-f, --flagged` | Envia les frases marcades per LanguageTool a stdout |
| `-h, --help`    | Mostra el missatge d'ajuda                          |
| `-v, --version` | Mostra la versió                                    |

## Compilació (requereix Maven i JDK 17+)

```bash
mvn package
```

## Crèdits

Originalment pensat per incoporar les frases de la [PCCD](https://pccd.dites.cat/) a
[Common Voice](https://github.com/common-voice/common-voice). Basat en el codi de
https://github.com/Softcatala/filter-wiki-corpus-lt
