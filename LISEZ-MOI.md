# LISEZ-MOI — Épreuve finale fullstack KFOKAM48

**Ouvre ce fichier en premier. Cinq minutes de lecture, elles te feront gagner une heure.**

---

## 1. Ce que contient ce dossier

| Fichier | À quoi il sert |
|---|---|
| `SUJET.pdf` et `SUJET.md` | **Le sujet complet.** Même contenu, deux formats. Lis-le en entier avant de toucher au clavier |
| `CLIENT.md` | Les 16 questions déjà posées au client. Tu n'auras aucune autre réponse |
| `api/contrat.yaml` | Le contrat d'API imposé : 5 opérations que ton backend doit exposer exactement ainsi |
| `modeles/CAHIER_DES_CHARGES.md` | Le squelette à remplir — les dix sections sont imposées |
| `modeles/JOURNAL.md` | Ton journal de bord, une entrée par étape |
| `modeles/SOUMISSION.md` | Le fichier que tu téléverseras sur la plateforme à la fin |

**Deux éléments ne sont pas dans ce dossier** et te seront remis pendant l'épreuve :

- **l'enveloppe de l'étape 3**, que tu recevras une fois ton jalon `v0.1` poussé ;
- **`git-lab.bundle`**, le dépôt de l'épreuve Git de l'étape 5.

---

## 2. À faire avant de commencer — 15 minutes

1. **Vérifie ton environnement.** Dans un terminal :
   ```bash
   git --version        # attendu : 2.x
   java -version        # attendu : 17 ou plus
   node --version       # attendu : 18 ou plus
   ```
   Si l'un des trois manque, préviens le surveillant tout de suite.

2. **Connecte-toi à ton compte GitHub.** Si tu n'en as pas, crée-le maintenant.

3. **Crée ton dépôt**, avec le nom exact imposé :
   `kfokam48-epreuve-<ton-matricule>` — exemple : `kfokam48-epreuve-KF48-YAO-042`
   **Visibilité : publique.** Un dépôt privé ne sera pas corrigé.

4. **Vérifie que tu peux pousser :**
   ```bash
   git clone https://github.com/<ton-compte>/kfokam48-epreuve-<matricule>.git
   cd kfokam48-epreuve-<matricule>
   git commit --allow-empty -m "[JALON] depart"
   git push
   ```
   Si ça ne passe pas, préviens le surveillant. C'est un incident matériel, il note l'heure et le temps t'est rendu.

5. **Lis le sujet en entier**, puis `CLIENT.md`. Ne saute pas cette étape : 38 points sur 100 se jouent avant ta première ligne de code.

---

## 3. Les cinq choses à retenir

1. **Ton dépôt est ta copie d'examen.** Le correcteur lit ton historique Git comme une rédaction. Un seul gros commit à la fin te coûte plus que tout le reste.
2. **L'analyse d'abord.** Cahier des charges, trois diagrammes, backlog en issues, contrat d'API — tout ça avant `spring init`. Le commit `[JALON] analyse` doit précéder ton premier commit de code.
3. **L'IA est totalement libre.** Aucune restriction, aucune trace à fournir. On te demande seulement, dans ton journal, **comment tu as vérifié** ce qu'elle t'a rendu.
4. **Pousse au fur et à mesure.** Un travail excellent resté sur ton disque vaut zéro.
5. **Tu travailles à ton rythme**, mais ta soumission doit être **sur la plateforme avant 18h00**. Ne vise pas 17h58.

---

## 4. Comment tu rends ton travail

À la fin, tu remplis `modeles/SOUMISSION.md` et tu le **téléverses sur la plateforme**. Il contient l'adresse de tes deux dépôts publics et le hash complet de ton dernier commit sur chacun.

**La correction porte exactement sur les commits que tu déclares.** Ce que tu pousses après est ignoré. Termine, pousse, relève tes hash, puis soumets.

> Sans ce dépôt sur la plateforme, tu n'as rien rendu — même si ton code est parfait sur GitHub.

---

## 5. Ce qui fera la différence

L'IA peut écrire cette application en deux heures. Ce n'est pas ce qu'on évalue.

Ce qu'on évalue, c'est si tu sais **analyser un besoin flou**, **le spécifier**, **le découper**, **le livrer par étapes** et **encaisser un changement** en cours de route — en laissant derrière toi un historique que quelqu'un d'autre peut lire.

Le candidat qui code sans analyse ni ticket plafonne autour de 40 sur 100, même avec un produit parfait. Celui qui livre les trois quarts du produit avec une démarche propre dépasse 80.

Bon courage.
