# lt-filter

Programa simple per filtrar frases amb LanguageTool.

## Compilació (requereix Maven i JDK 17+)

```
mvn package
```

## Ús

```
java -jar target/lt-filter-jar-with-dependencies.jar frases.txt > ok.txt 2> excluded.txt
```

## Instal·lació i ús amb NPM (requereix JRE 17+)

```bash
npm install @pccd/lt-filter
```

```bash
npx lt-filter input.txt > ok.txt 2> excluded.txt
```

## Crèdits

Originalment pensat per incoporar les frases de la [PCCD](https://pccd.dites.cat/) a
[Common Voice](https://github.com/common-voice/common-voice). Basat en el codi de
https://github.com/Softcatala/filter-wiki-corpus-lt
