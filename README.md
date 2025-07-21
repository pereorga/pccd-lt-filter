# lt-filter

Programa per filtrar frases en català amb LanguageTool. Separa les frases correctes de les que contenen possibles errors gramaticals o ortogràfics.

## Instal·lació i ús (requereix npm i JRE 17+)

```bash
npm install @pccd/lt-filter
```

### Ús com a eina de línia d'ordres (CLI)

```bash
# Per defecte, les frases correctes van a stdout i les incorrectes a stderr
npx lt-filter input.txt > correct.txt 2> flagged.txt

# Si no s'especifica un fitxer, es llegeix l'entrada estàndard
cat input.txt | npx lt-filter > correct.txt 2> flagged.txt

# Només frases correctes
npx lt-filter --correct input.txt > correct.txt

# Només frases potencialment incorrectes
npx lt-filter --flagged input.txt > flagged.txt

# Inclou els noms de les regles després de les frases marcades
npx lt-filter --flagged --rule-names input.txt

# Desactiva regles addicionals (s'afegeixen a les regles per defecte)
npx lt-filter --disable-rules REGLA1,REGLA2 input.txt

# Substitueix completament les regles per defecte
npx lt-filter --disable-rules-replace SER_ESSER input.txt
```

Si no, es pot baixar directament el [fitxer JAR](bin/lt-filter.jar):

```bash
wget https://github.com/pereorga/pccd-lt-filter/raw/refs/heads/master/bin/lt-filter.jar
java -jar lt-filter.jar --help
```

### Ús com a servidor REST

Per evitar la sobrecàrrega d'iniciar l'aplicació per a cada comprovació, es pot executar com a servidor REST.

**Inicia el servidor:**

```bash
java -jar bin/lt-filter.jar --port 8080
```

**Envia text per a la seva anàlisi:**

Fes una petició `POST` a l'arrel (`/`) amb el text (una o més frases separades per salts de línia) en el cos de la petició.

```bash
curl -X POST -d "Això és una frase correcta.
Això és una altre frase." "http://localhost:8080/"
```

**Resposta JSON:**

La resposta és un objecte JSON que separa les frases correctes de les incorrectes.

```json
{
  "correct": ["Això és una frase correcta."],
  "flagged": [
    {
      "sentence": "Això és una altre frase."
    }
  ]
}
```

**Paràmetres opcionals:**

Podeu passar opcions de la línia d'ordres com a paràmetres de consulta a la URL.

- `rule-names=true`: Inclou els identificadors de les regles a la sortida.
- `disable-rules=REGLA1,REGLA2`: Desactiva regles addicionals.
- `disable-rules-replace=REGLA1,REGLA2`: Substitueix les regles desactivades per defecte.

**Exemple amb `rule-names`:**

```bash
curl -X POST -d "A acaba-set" "http://localhost:8080/?rule-names=true"
```

**Resposta JSON amb regles:**

```json
{
  "correct": [],
  "flagged": [
    {
      "sentence": "A acaba-set",
      "rules": ["PREP_VERB_CONJUGAT"]
    }
  ]
}
```

## Opcions de la línia d'ordres

| Opció                           | Descripció                                                         |
| ------------------------------- | ------------------------------------------------------------------ |
| `--port PORT`                   | Executa com a servidor REST en el port especificat                 |
| `-c, --correct`                 | Envia les frases correctes a stdout                                |
| `-f, --flagged`                 | Envia les frases marcades per LanguageTool a stdout                |
| `-r, --rule-names`              | Inclou els noms de les regles després de les frases marcades       |
| `-d, --disable-rules RULES`     | Llista de regles addicionals a desactivar (separades per comes)    |
| `--disable-rules-replace RULES` | Llista de regles a desactivar (substitueix les regles per defecte) |
| `-h, --help`                    | Mostra el missatge d'ajuda                                         |
| `-v, --version`                 | Mostra la versió                                                   |

## Exemple de sortida (CLI) amb noms de regles

Quan s'utilitza l'opció `--rule-names`, els noms de les regles que han detectat errors apareixen entre claudàtors després de la frase:

```bash
$ echo "A acaba-set" | npx lt-filter --flagged --rule-names
A acaba-set [PREP_VERB_CONJUGAT]

$ echo "A aferrapilla" | npx lt-filter --flagged --rule-names
A aferrapilla [MORFOLOGIK_RULE_CA_ES]
```

## Regles desactivades per defecte

Per defecte, es desactiven les següents regles de LanguageTool:

- `EXIGEIX_VERBS_CENTRAL`
- `EXIGEIX_ACCENTUACIO_GENERAL`
- `EXIGEIX_POSSESSIUS_V`
- `EVITA_PRONOMS_VALENCIANS`
- `EVITA_DEMOSTRATIUS_EIXE`
- `VOCABULARI_VALENCIA`
- `EXIGEIX_US`
- `SER_ESSER`
- `WHITESPACE_RULE`
- `CA_UNPAIRED_BRACKETS`
- `ESPAIS_SOBRANTS`
- `MAJ_DESPRES_INTERROGANT`
- `UPPERCASE_SENTENCE_START`

## Compilació (requereix Maven i JDK 17+)

```bash
mvn package
```

## Crèdits

Originalment pensat per incoporar les frases de la [PCCD](https://pccd.dites.cat/) a
[Common Voice](https://github.com/common-voice/common-voice). Basat en el codi de
https://github.com/Softcatala/filter-wiki-corpus-lt
