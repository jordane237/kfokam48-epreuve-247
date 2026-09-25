import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { ApiServiceBase } from './api-service-base';

/** Une relecture à faire (écran relecteur). */
export interface RelectureEnAttente {
  exerciceId: number;
  lien: string;
  assigneeAt: string;
}

/** Sortie 200 de POST /api/relectures/{id} — contrat (affectations = champ additionnel étape 3). */
export interface RelectureRendue {
  id: number;
  exerciceId: number;
  note: number;
  commentaire: string;
  rendueAt: string;
  statut: 'assignee' | 'rendue';
  affectations?: { relectureId: number; relecteurId: number; statut: string; note: number | null; rendueAt: string | null }[];
}

/** Retour consulté par l'étudiant relu (EF11) — étape 3 : moyenne + indicateur. */
export interface RetourRelecture {
  exerciceId: number;
  note: number;
  provisoire: boolean;
  commentaires: string[];
  derniereRendueAt: string | null;
}

/** Service dédié aux relectures (F3). */
@Injectable({ providedIn: 'root' })
export class RelecturesApiService extends ApiServiceBase {
  private readonly http = inject(HttpClient);

  /** Les relectures à faire de l'étudiant (opération ajoutée GET /api/relectures/en-attente). */
  enAttente(relecteurId: number) {
    return this.envelopper(
      this.http.get<RelectureEnAttente[]>(`${this.apiUrl}/api/relectures/en-attente`, {
        params: { relecteurId: String(relecteurId) }
      })
    );
  }

  /**
   * EF9 : rendre une relecture (id = id de l'exercice). Étape 3 : relecteurId
   * identifie SA propre affectation dès que deux relecteurs sont assignés.
   */
  rendre(exerciceId: number, note: number, commentaire: string, relecteurId?: number) {
    return this.envelopper(
      this.http.post<RelectureRendue>(`${this.apiUrl}/api/relectures/${exerciceId}`,
        relecteurId != null ? { note, commentaire, relecteurId } : { note, commentaire })
    );
  }

  /** EF11 : note (moyenne des relectures rendues) + provisoire — Q8 : sans identité des relecteurs. */
  retour(exerciceId: number) {
    return this.envelopper(
      this.http.get<RetourRelecture>(`${this.apiUrl}/api/exercices/${exerciceId}/retour`)
    );
  }
}
