# lt-filter

Script simple per filtrar frases amb LanguageTool.

## Compilació

```
mvn package
```

## Ús

```
java -jar target/lt-filter-0.0.1-jar-with-dependencies.jar frases.txt > sortida.txt 2> excloses.txt
```

## Crèdits

Originalment pensat per incoporar les frases de la [PCCD](https://pccd.dites.cat/) a
[Common Voice](https://github.com/common-voice/common-voice). Basat en el codi de
https://github.com/Softcatala/filter-wiki-corpus-lt
