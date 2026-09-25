# frontend — KFOKAM48 (Angular)

**Framework : Angular** — choisi pour sa séparation stricte des couches (composants / services / couche d'appel API dédiée `HttpClient`), exigée par le sujet (F3).

## Installation

```bash
npm install
```

## Démarrage

```bash
npm start
# http://localhost:4200
```

## Build

```bash
npm run build
# sortie : dist/frontend
```

## Structure

- `src/app/api/` — **couche d'appel API dédiée** (F3) : classe de base `ApiServiceBase`, normalisation des erreurs `{ code, message }` du contrat. Les services métier l'étendent.
- `src/environments/environment.ts` — URL de l'API (voir `.env.example`, aucun secret).
