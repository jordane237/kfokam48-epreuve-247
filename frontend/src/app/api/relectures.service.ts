import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { ApiServiceBase } from './api-service-base';

/** Une relecture à faire (écran relecteur). */
export interface RelectureEnAttente {
  exerciceId: number;
  lien: string;
  assigneeAt: string;
}

/** Sortie 200 de POST /api/relectures/{id} — contrat. */
export interface RelectureRendue {
  id: number;
  exerciceId: number;
  note: number;
  commentaire: string;
  rendueAt: string;
  statut: 'assignee' | 'rendue';
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

  /** EF9 : rendre une relecture (id = id de l'exercice) ; 403/400/409 normalisés. */
  rendre(exerciceId: number, note: number, commentaire: string) {
    return this.envelopper(
      this.http.post<RelectureRendue>(`${this.apiUrl}/api/relectures/${exerciceId}`,
        { note, commentaire })
    );
  }
}
