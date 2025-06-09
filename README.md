# lt-filter

Programa per filtrar frases en català amb LanguageTool.  Separa les frases correctes de les que contenen possibles errors gramaticals o ortogràfics.

## Instal·lació i ús (requereix JRE 17+)

```bash
npm install @pccd/lt-filter
```

```bash
# Comportament per defecte
npx lt-filter input.txt > correct.txt 2> flagged.txt

# Només frases correctes
npx lt-filter --correct input.txt > correct.txt

# Només frases potencialment incorrectes
npx lt-filter --flagged input.txt > flagged.txt
```

## Opcions de la línia d'ordres

| Opció           | Descripció                                           |
|-----------------|------------------------------------------------------|
| `-c, --correct` | Envia les frases correctes a stdout                  |
| `-f, --flagged` | Envia les frases marcades per LanguageTool a stdout  |
| `-h, --help`    | Mostra el missatge d'ajuda                           |

## Compilació (requereix Maven i JDK 17+)

```bash
mvn package
```

## Crèdits

Originalment pensat per incoporar les frases de la [PCCD](https://pccd.dites.cat/) a
[Common Voice](https://github.com/common-voice/common-voice). Basat en el codi de
https://github.com/Softcatala/filter-wiki-corpus-lt
